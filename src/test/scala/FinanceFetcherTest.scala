import org.scalatest.FunSuite

import scala.concurrent.ExecutionContext.Implicits.global

class FinanceFetcherTest extends FunSuite {
  private val financeFetcher = new FinanceFetcher
  private val stockId = "2330"

  test("getFinanceFromGoodinfo") {
    financeFetcher.getFinanceFromGoodinfo(stockId, Duration.ThreeYear).map(testResponse)
  }

  test("getFinance") {
    financeFetcher.getFinance(stockId, 2016).map(testResponse)
  }

  private def testResponse(finance: Finance) = {
    println(finance.toString)
    assert(finance.id == stockId)
    assert(finance.PER.isInstanceOf[Double] && finance.PER != 0)
    assert(finance.meanROA.isInstanceOf[Double] && finance.meanROA != 0)
  }
}
