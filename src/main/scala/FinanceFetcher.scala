import Extension._
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.ws.ahc.AhcCurlRequestLogger

import scala.concurrent.{ExecutionContext, Future}

class FinanceFetcher(implicit ec: ExecutionContext) {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  //  def getFinanceReport(id: String, fromYear: Int, fromSeason: Int, toYear: Int, toSeason: Int): Future[FinanceReport] = {
  //    Http.client.url(s"https://statementdog.com/api/v1/fundamentals/$id/$fromYear/$fromSeason/$toYear/$toSeason").get.map {
  //
  //    }
  //  }

  def getFinanceFromGoodinfo(id: String, duration: TimeLimit = TimeLimit.OneYear): Future[Finance] = {
    Thread.sleep(2000)
    Http.client.url(s"https://goodinfo.tw/StockInfo/StockBzPerformance.asp?STOCK_ID=$id&YEAR_PERIOD=${duration.year}&RPT_CAT=M_YEAR")
      .addHttpHeaders("user-agent" -> "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36")
      .get.map {
      response =>
        //logger.error(response.body)
        val doc: Browser#DocumentType = JsoupBrowser().parseString(response.body)
        val PER: Double = doc >?> text("body > table:nth-child(3) > tbody > tr > td:nth-child(3) > table > tbody > tr > td > table:nth-child(1) > tbody > tr > td > table > tbody > tr:nth-child(5) > td:nth-child(6)") toDigit
        val ROA: Double = doc >?> text("body > table:nth-child(3) > tbody > tr > td:nth-child(3) > table > tbody > tr > td > table.solid_1_padding_3_0_tbl > tbody > tr:nth-child(8) > td:nth-child(4)") toDigit
        val EPS: Double = doc >?> text("body > table:nth-child(3) > tbody > tr > td:nth-child(3) > table > tbody > tr > td > table.solid_1_padding_3_0_tbl > tbody > tr:nth-child(8) > td:nth-child(6)") toDigit

        Finance(id, ROA, EPS)
    } recoverWith {
      case e: Exception =>
        logger.error("stock id: " + id)
        e.printStackTrace()
        getFinanceFromGoodinfo(id, duration)
    }
  }

  def getFinance(id: String, year: Int = 0): Future[Finance] = {
    Thread.sleep(5000)
    Http.client.url("http://mops.twse.com.tw/mops/web/t05st22_q1").get().flatMap {
      response =>
        Http.client.url("http://mops.twse.com.tw/mops/web/ajax_t05st22").addCookies(response.cookies: _*)
          //.withRequestFilter(AhcCurlRequestLogger())
          .post(
          Map(
            "encodeURIComponent" -> "1",
            "run" -> "Y",
            "step" -> "1",
            "TYPEK" -> "sii",
            "year" -> (if (year == 0) "" else (year - 1911).toString),
            "isnew" -> (if (year == 0) "true" else "false"),
            "co_id" -> id.toString,
            "firstin" -> "1",
            "off" -> "1",
            "ifrs" -> "Y"))
    } map {
      response =>
        def average(value: Double*): Double = value.sum / value.length

        val doc: Browser#DocumentType = JsoupBrowser().parseString(response.body)
        val ROA: Double = doc >?> text("body > center:nth-child(6) > table > tbody > tr:nth-child(13) > td:nth-child(5)") toDigit
        val EPS: Double = doc >?> text("body > center:nth-child(6) > table > tbody > tr:nth-child(17) > td:nth-child(4)") toDigit

        //val PER: Double = price / EPS
        /*
        val lastYearROA: Double = doc >> text("body > center:nth-child(6) > table > tbody > tr:nth-child(13) > td:nth-child(4)") toDigit
        val theYearBeforeLastROA: Double = doc >> text("body > center:nth-child(6) > table > tbody > tr:nth-child(13) > td:nth-child(3)") toDigit
        val meanROA: Double = average(ROA, lastYearROA, theYearBeforeLastROA)
        */
        Finance(id, ROA, EPS)
    }
  } recoverWith {
    case e: Exception =>
      logger.error("stock id: " + id)
      e.printStackTrace()
      getFinance(id, year)
  }
}

case class Finance(id: String, ROA: Double, EPS: Double)
