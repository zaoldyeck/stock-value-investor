import play.api.libs.json._
import play.api.libs.ws.JsonBodyReadables._

import scala.concurrent.{ExecutionContext, Future}

class StockFetcher(implicit ec: ExecutionContext) {
  def getAllStocks: Future[List[Stock]] = {
    val stockFilters: Seq[String] = 1 to 31 map {
      number =>
        if (number < 10) "0" + number else number.toString
    }

    Future.traverse(stockFilters) {
      number =>
        getStock(number)
    } map {
      stocks => stocks.reduce(_ ::: _)
    }
  }

  private def getStock(filter: String): Future[List[Stock]] = {
    Http.client.url(s"http://www.tse.com.tw/zh/api/codeFilters?filter=$filter").get.map {
      response =>
        response.body[JsValue].as[ResStocks].resualt.map {
          string => Stock(string.split("\t")(0), string.split("\t")(1))
        }
    }
  }

  private case class ResStocks(filter: String, resualt: List[String])

  implicit private val resStocksReads: Reads[ResStocks] = Json.reads[ResStocks]

  def getPublicStocks: Future[List[Stock]] = {
    Http.client.url("http://quality.data.gov.tw/dq_download_json.php?nid=18419&md5_url=4932a781923479c4c782e8a07078d9e9").get.map {
      response =>
        response.body[JsValue].as[List[JsValue]].map {
          jsValue =>
            val id = (jsValue \ "公司代號").as[String]
            val name = (jsValue \ "公司名稱").as[String]
            Stock(id, name)
        }
    }
  }
}

case class Stock(id: String, name: String)
