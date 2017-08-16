//import org.joda.time.DateTime
import java.util.Date

import play.api.libs.json.JsValue
import play.api.libs.json._
import play.api.libs.ws.DefaultWSCookie

import scala.concurrent.Future
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.ahc.AhcCurlRequestLogger

import scala.concurrent.ExecutionContext.Implicits._

class FinanceFetcher {
  // http://www.twse.com.tw/exchangeReport/STOCK_DAY?response=json&date=20170801&stockNo=2330
  def getPrice(id: String, date: Date): Future[Double] = {
    Http.client.url(s"http://mis.twse.com.tw/stock/fibest.jsp?stock=$id").get.flatMap {
      response =>
        response.
        Http.client.url(s"http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_$id.tw&json=1&delay=0&_=${date.getTime}")
          .withRequestFilter(AhcCurlRequestLogger())
          .get
    } map {
      response =>
        println("come in")
        println(response.body)
        (response.body[JsValue].apply("msgArray")(0) \ "z").as[String].toDouble
    }
  }

  //  def getFinanceReport(id: String, fromYear: Int, fromSeason: Int, toYear: Int, toSeason: Int): Future[FinanceReport] = {
  //    Http.client.url(s"https://statementdog.com/api/v1/fundamentals/$id/$fromYear/$fromSeason/$toYear/$toSeason").get.map {
  //
  //    }
  //  }

  case class FinanceReport(meanPER: Double, meanROA: Double)

}
