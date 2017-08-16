import scala.concurrent.ExecutionContext.Implicits._

object Main {
  def main(args: Array[String]): Unit = {
        new StockFetcher().fetchAllStocks().map {
          stocks => stocks.foreach(println)
        } andThen {
          case _ => Http.terminate()
        }
  }
}
