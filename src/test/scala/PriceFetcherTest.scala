import org.scalatest.FunSuite

import scala.concurrent.ExecutionContext.Implicits.global

class PriceFetcherTest extends FunSuite {
  private val priceFetcher = new PriceFetcher()

  test("getRealTimePrice") {
    priceFetcher.getRealTimePrice("2330").map {
      price => assert(price.isInstanceOf[Double] && price != 0)
    }
  }

  test("getHistoryPrice") {
    priceFetcher.getHistoryPrice("2330", 2017, 12).map {
      historyPrice => assert(historyPrice.nonEmpty)
    }
  }
}
