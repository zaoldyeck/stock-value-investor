import org.scalatest.AsyncFunSuite

class StockFetcherTest extends AsyncFunSuite {
  private val stockFetcher = new StockFetcher

  test("getAllStocks") {
    stockFetcher.getAllStocks.map {
      stocks =>
        println(stocks.size)
        assert(stocks.nonEmpty)
    }
  }

  test("getPublicStocks") {
    stockFetcher.getPublicStocks.map {
      stocks =>
        println(stocks.size)
        assert(stocks.nonEmpty)
    }
  }
}
