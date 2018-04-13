import java.text.{DecimalFormat, SimpleDateFormat}
import java.util.Date

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.JsValue
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.ahc.StandaloneAhcWSRequest

import scala.concurrent.{ExecutionContext, Future}

class PriceFetcher(implicit ec: ExecutionContext) {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def getRealTimePrice(id: String): Future[Double] = {
    Http.client.url(s"http://mis.twse.com.tw/stock/fibest.jsp?stock=$id").get.flatMap {
      response =>
        Http.client.url(s"http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=tse_$id.tw&json=1&delay=0&_=${new Date().getTime}")
          //.withRequestFilter(AhcCurlRequestLogger())
          .addCookies(response.cookies: _*)
          .get
    } map {
      response =>
        ((response.body[JsValue] \ "msgArray") (0) \ "z").asOpt[String] match {
          case Some(price) => price.toDouble
          case None => 0
        }
    } recoverWith {
      case e: Exception =>
        e.printStackTrace()
        getRealTimePrice(id)
    }
  }

  def getRealTimePrice(ids: List[String]): Future[List[StockPrice]] = {
    val parameter = ids.map(id => s"tse_$id.tw").mkString("|")
    Http.client.url(s"http://mis.twse.com.tw/stock/fibest.jsp").get.flatMap {
      response =>
        StandaloneAhcWSRequest(
          Http.client,
          s"http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=$parameter&json=1&delay=0&_=${new Date().getTime}",
          disableUrlEncoding = Some(true))(Http.materializer)
          //.withRequestFilter(AhcCurlRequestLogger())
          .addCookies(response.cookies: _*)
          .get
    } map {
      response =>
        (response.body[JsValue] \ "msgArray").as[List[JsValue]].map {
          jsValue =>
            val id = (jsValue \ "c").as[String]
            val price = (jsValue \ "z").asOpt[String] match {
              case Some(p) => p.toDouble
              case None => 0
            }
            StockPrice(id, price)
        }
    } recoverWith {
      case e: Exception =>
        e.printStackTrace()
        getRealTimePrice(ids)
    }
  }

  def getRealTimePriceFromGoogleFinance(id: String): Future[Double] = {
    Http.client.url("https://www.google.com/finance?q=TPE%3A2330").get.map {
      response =>
        val doc: Browser#DocumentType = JsoupBrowser().parseString(response.body)
        (doc >> text("#ref_674465_l")).toDouble
    } recoverWith {
      case e: Exception =>
        e.printStackTrace()
        getRealTimePriceFromGoogleFinance(id)
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
        (response.body[JsValue] \ "data").as[List[ResHistoryPrice]].map(HistoryPrice(_))
    }
  }

  private type ResHistoryPrice = (String, String, String, String, String, String, String, String, String)

  case class HistoryPrice(date: Date,
                          tradeVolume: Int,
                          tradeValue: Double,
                          openingPrice: Double,
                          highestPrice: Double,
                          lowestPrice: Double,
                          closingPrice: Double,
                          change: Double,
                          transaction: Int)

  private object HistoryPrice {
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

  case class StockPrice(id: String, price: Double)

}
