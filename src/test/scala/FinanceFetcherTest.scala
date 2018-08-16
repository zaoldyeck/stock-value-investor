import org.scalatest.AsyncFunSuite

class FinanceFetcherTest extends AsyncFunSuite {
  implicit val timeInterval = TimeInterval(10000, 60000)
  private val financeFetcher = new FinanceFetcher
  private val stockId = "4414"

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
