import javax.inject.Inject

class StockFetcher @Inject()(httpClient: HttpClient) {
  def fetchStocks(): Unit = {
    println(httpClient)
  }
}
