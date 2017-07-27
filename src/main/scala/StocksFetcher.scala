class StocksFetcher {

  import scala.concurrent.ExecutionContext.Implicits._

  def fetchStocks() {
    HttpClient.ws(ws => ws.url("http://www.google.com").get() map { response ⇒
      val statusText: String = response.statusText
      val body = response.body[String]
      println(s"Got a response $statusText")
      HttpClient.ws(ws => ws.url("http://www.google.com").get() map { response =>
        val statusText: String = response.statusText
        val body = response.body[String]
        println(s"Got a response $statusText")
      })
    })
  }
}
