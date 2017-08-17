import play.api.libs.json._
import play.api.libs.ws.JsonBodyReadables._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class StockFetcher {
  def fetchAllStocks(): Future[List[Stock]] = {
    val stockFilters: Seq[String] = 1 to 31 map { number =>
      if (number < 10) "0" + number else number.toString
    }

    Future.traverse(stockFilters) {
      number => fetchStock(number)
    } map {
      stocks => stocks.reduce(_ ::: _)
    }
  }

  private def fetchStock(filter: String): Future[List[Stock]] = {
    Http.client.url(s"http://www.tse.com.tw/zh/api/codeFilters?filter=$filter").get.map {
      response =>
        response.body[JsValue].as[ResStocks].resualt.map {
          string =>
            Stock(string.split("\t")(0), string.split("\t")(1))
        }
    }
  }

  case class ResStocks(filter: String, resualt: List[String])

  implicit val resStocksReads: Reads[ResStocks] = Json.reads[ResStocks]

  case class Stock(id: String, name: String)

}
