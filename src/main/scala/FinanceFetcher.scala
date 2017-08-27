import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class FinanceFetcher {
  implicit def stringToDouble(s: String): Double = java.lang.Double.parseDouble(s.filter(char => Character.isDigit(char) || char == '.'))

  //  def getFinanceReport(id: String, fromYear: Int, fromSeason: Int, toYear: Int, toSeason: Int): Future[FinanceReport] = {
  //    Http.client.url(s"https://statementdog.com/api/v1/fundamentals/$id/$fromYear/$fromSeason/$toYear/$toSeason").get.map {
  //
  //    }
  //  }

  def getFinance(id: String, duration: Duration): Future[Finance] = Future {
    val doc = JsoupBrowser().get(s"https://goodinfo.tw/StockInfo/StockBzPerformance.asp?STOCK_ID=$id&YEAR_PERIOD=${duration.year}&RPT_CAT=M_YEAR")
    val PER: String = doc >> text("body > table:nth-child(2) > tbody > tr > td:nth-child(3) > table > tbody > tr > td > table:nth-child(1) > tbody > tr > td > table > tbody > tr:nth-child(5) > td:nth-child(6)")
    val minROA: String = doc >> text("body > table:nth-child(2) > tbody > tr > td:nth-child(3) > table > tbody > tr > td > table.solid_1_padding_3_0_tbl > tbody > tr:nth-child(8) > td:nth-child(4)")
    Finance(id, PER, minROA)
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
      price <- new PriceFetcher().getRealTimePriceFromGoogleFinance(id)
    } yield {
      def average(value: Double*): Double = value.sum / value.length

      val doc: Browser#DocumentType = JsoupBrowser().parseString(response.body)
      val EPS: String = doc >> text("body > center:nth-child(6) > table > tbody > tr:nth-child(17) > td:nth-child(4)")
      val PER: Double = price / EPS

      val ROA: String = doc >> text("body > center:nth-child(6) > table > tbody > tr:nth-child(13) > td:nth-child(5)")
      val lastYearROA: String = doc >> text("body > center:nth-child(6) > table > tbody > tr:nth-child(13) > td:nth-child(4)")
      val theYearBeforeLastROA: String = doc >> text("body > center:nth-child(6) > table > tbody > tr:nth-child(13) > td:nth-child(3)")
      val meanROA: Double = average(ROA, lastYearROA, theYearBeforeLastROA)

      Finance(id, PER, meanROA)
    }
  } recover {
    case e: Exception =>
      e.printStackTrace()
      Finance(id, 0, 0)
  }

  case class Finance(id: String, PER: Double, meanROA: Double)

}
