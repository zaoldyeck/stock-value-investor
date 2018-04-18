import org.scalatest.AsyncFunSuite

class PriceFetcherTest extends AsyncFunSuite {
  private val priceFetcher = new PriceFetcher

  test("getRealTimePriceById") {
    priceFetcher.getRealTimePrice("2330").map {
      price => assert(price.isInstanceOf[Double] && price != 0)
    }
  }

  test("getRealTimePriceByList") {
    priceFetcher.getRealTimePrice(List("2330","3008")).map {
      stockPrices =>
        println(stockPrices)
        assert(stockPrices.nonEmpty)
    }
  }

  test("getHistoryPrice") {
    priceFetcher.getHistoryPrice("2330", 2017, 12).map {
      historyPrice => assert(historyPrice.nonEmpty)
    }
  }
}
