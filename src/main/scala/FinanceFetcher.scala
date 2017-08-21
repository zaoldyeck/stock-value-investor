import java.text.{DecimalFormat, SimpleDateFormat}
import java.util.Date

import play.api.libs.json.JsValue
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.ahc.AhcCurlRequestLogger

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class FinanceFetcher {
  def getRealTimePrice(id: String): Future[Double] = {
    Http.client.url(s"http://mis.twse.com.tw/stock/fibest.jsp?stock=$id").get.flatMap {
      response =>
        Http.client.url(s"http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_$id.tw&json=1&delay=0&_=${new Date().getTime}")
          //.withRequestFilter(AhcCurlRequestLogger())
          .addCookies(response.cookies: _*)
          .get
    } map {
      response =>
        (response.body[JsValue].apply("msgArray")(0) \ "z").as[String].toDouble
    }
  }

  def getHistoryPrice(id: String, year: Int, month: Int): Future[List[HistoryPrice]] = {
    Http.client.url("http://www.twse.com.tw").get.flatMap {
      response =>
        val monthString: String = if (month < 10) "0" + month else month.toString
        Http.client.url(s"http://www.twse.com.tw/en/exchangeReport/STOCK_DAY?response=json&date=$year${monthString}01&stockNo=$id")
          .addCookies(response.cookies: _*)
          .get
    } map {
      response =>
        response.body[JsValue].apply("data").as[List[ResHistoryPrice]].map(HistoryPrice(_))
    }
  }

  //  def getFinanceReport(id: String, fromYear: Int, fromSeason: Int, toYear: Int, toSeason: Int): Future[FinanceReport] = {
  //    Http.client.url(s"https://statementdog.com/api/v1/fundamentals/$id/$fromYear/$fromSeason/$toYear/$toSeason").get.map {
  //
  //    }
  //  }

  case class FinanceReport(meanPER: Double, meanROA: Double)

  type ResHistoryPrice = (String, String, String, String, String, String, String, String, String)

  case class HistoryPrice(date: Date,
                          tradeVolume: Int,
                          tradeValue: Double,
                          openingPrice: Double,
                          highestPrice: Double,
                          lowestPrice: Double,
                          closingPrice: Double,
                          change: Double,
                          transaction: Int)

  object HistoryPrice {
    def apply(resHistoryPrice: ResHistoryPrice): HistoryPrice = {
      val (
        date: String,
        tradeVolume: String,
        tradeValue: String,
        openingPrice: String,
        highestPrice: String,
        lowestPrice: String,
        closingPrice: String,
        change: String,
        transaction: String) = resHistoryPrice

      val decimalFormat = new DecimalFormat()
      HistoryPrice(
        new SimpleDateFormat("yyyy/MM/dd").parse(date),
        decimalFormat.parse(tradeVolume).intValue,
        decimalFormat.parse(tradeValue).doubleValue,
        decimalFormat.parse(openingPrice).doubleValue,
        decimalFormat.parse(highestPrice).doubleValue,
        decimalFormat.parse(lowestPrice).doubleValue,
        decimalFormat.parse(closingPrice).doubleValue,
        decimalFormat.parse(change.trim.replace("+", "")).doubleValue,
        decimalFormat.parse(transaction).intValue)
    }
  }
}
