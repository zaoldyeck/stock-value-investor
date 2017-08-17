import java.util.Date

import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits._

object Main {
  def main(args: Array[String]): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)
    //        new StockFetcher().fetchAllStocks().map {
    //          stocks => stocks.foreach(println)
    //        } andThen {
    //          case _ => Http.terminate()
    //        }

    new FinanceFetcher().getPrice("2330", new Date).map {
        price => logger.info(price.toString)
    } andThen {
      case _ => Http.terminate()
    }
  }
}
