import Extension._
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FinanceFetcher {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  //  def getFinanceReport(id: String, fromYear: Int, fromSeason: Int, toYear: Int, toSeason: Int): Future[FinanceReport] = {
  //    Http.client.url(s"https://statementdog.com/api/v1/fundamentals/$id/$fromYear/$fromSeason/$toYear/$toSeason").get.map {
  //
  //    }
  //  }

  def getFinanceFromGoodinfo(id: String, duration: Duration): Future[Finance] = {
    Http.client.url(s"https://goodinfo.tw/StockInfo/StockBzPerformance.asp?STOCK_ID=$id&YEAR_PERIOD=${duration.year}&RPT_CAT=M_YEAR")
      .addHttpHeaders("user-agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36")
      .get.map {
      response =>
        Thread.sleep(2000)
        val doc: Browser#DocumentType = JsoupBrowser().parseString(response.body)
        val PER: Double = doc >> text("body > table:nth-child(3) > tbody > tr > td:nth-child(3) > table > tbody > tr > td > table:nth-child(1) > tbody > tr > td > table > tbody > tr:nth-child(5) > td:nth-child(6)") toDigit
        val minROA: Double = (doc >> text("body > table:nth-child(3) > tbody > tr > td:nth-child(3) > table > tbody > tr > td > table.solid_1_padding_3_0_tbl > tbody > tr:nth-child(8) > td:nth-child(4)")).toDigit
        Finance(id, PER, minROA)
    } recover {
      case e: Exception =>
        e.printStackTrace()
        //getFinanceFromGoodinfo(id, duration)
        Finance(id, 0, 0)
    }
  }

  def getFinance(id: String, year: Int): Future[Finance] = {
    for {
      response <- Http.client.url("http://mops.twse.com.tw/mops/web/t05st22_q1").get().flatMap {
        response =>
          Http.client.url("http://mops.twse.com.tw/mops/web/ajax_t05st22").addCookies(response.cookies: _*).post(
            Map(
              "encodeURIComponent" -> "1",
              "run" -> "Y",
              "step" -> "1",
              "TYPEK" -> "sii",
              "year" -> (year - 1911).toString,
              "isnew" -> "false",
              "co_id" -> id.toString,
              "firstin" -> "1",
              "off" -> "1",
              "ifrs" -> "Y"))
      }
      price <- new PriceFetcher().getRealTimePrice(id)
    } yield {
      def average(value: Double*): Double = value.sum / value.length

      val doc: Browser#DocumentType = JsoupBrowser().parseString(response.body)
      val EPS: Double = doc >> text("body > center:nth-child(6) > table > tbody > tr:nth-child(17) > td:nth-child(4)") toDigit
      val PER: Double = price / EPS

      val ROA: Double = doc >> text("body > center:nth-child(6) > table > tbody > tr:nth-child(13) > td:nth-child(5)") toDigit
      val lastYearROA: Double = doc >> text("body > center:nth-child(6) > table > tbody > tr:nth-child(13) > td:nth-child(4)") toDigit
      val theYearBeforeLastROA: Double = doc >> text("body > center:nth-child(6) > table > tbody > tr:nth-child(13) > td:nth-child(3)") toDigit
      val meanROA: Double = average(ROA, lastYearROA, theYearBeforeLastROA)

      Finance(id, PER, meanROA)
    }
  } recoverWith {
    case e: Exception =>
      e.printStackTrace()
      getFinance(id, year)
  }

}

case class Finance(id: String, PER: Double, meanROA: Double)
