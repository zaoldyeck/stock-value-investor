import org.scalatest.FunSuite

import scala.concurrent.ExecutionContext.Implicits.global

class FinanceFetcherTest extends FunSuite {
  private val financeFetcher = new FinanceFetcher
  private val stockId = "1311"

  test("getFinanceFromGoodinfo") {
    financeFetcher.getFinanceFromGoodinfo(stockId).map(testResponse)
  }

  test("getFinance") {
    financeFetcher.getFinance(stockId).map(testResponse)
  }

  private def testResponse(finance: Finance) = {
    println(finance.toString)
    assert(finance.id == stockId)
    assert(finance.ROA.isInstanceOf[Double] && finance.ROA != 0)
    assert(finance.EPS.isInstanceOf[Double] && finance.EPS != 0)
  }
}
