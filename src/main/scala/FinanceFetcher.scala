import java.util.Date

import play.api.libs.json.JsValue
import play.api.libs.ws.JsonBodyReadables._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class FinanceFetcher {
  // http://www.twse.com.tw/exchangeReport/STOCK_DAY?response=json&date=20170801&stockNo=2330
  def getPrice(id: String, date: Date): Future[Double] = {
    Http.client.url(s"http://mis.twse.com.tw/stock/fibest.jsp?stock=$id").get.flatMap {
      response =>
        Http.client.url(s"http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_$id.tw&json=1&delay=0&_=${date.getTime}")
          .addCookies(response.cookies: _*)
          .get
    } map {
      response =>
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
