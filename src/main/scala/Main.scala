import java.util.Date

import scala.concurrent.ExecutionContext.Implicits._

object Main {
  def main(args: Array[String]): Unit = {
    //        new StockFetcher().fetchAllStocks().map {
    //          stocks => stocks.foreach(println)
    //        } andThen {
    //          case _ => Http.terminate()
    //        }

    new FinanceFetcher().getPrice("2330", new Date).map {
      price => println(price)
    } andThen {
      case _ => Http.terminate()
    }
  }
}
