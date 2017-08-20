import java.util.Date

import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits._
import scala.util.{Failure, Success}

object Main {
  def main(args: Array[String]): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)
    //        new StockFetcher().fetchAllStocks().map {
    //          stocks => stocks.foreach(println)
    //        } andThen {
    //          case _ => Http.terminate()
    //        }

    //    val financeFetcher = new FinanceFetcher()
    //    financeFetcher.getRealTimePrice("2330").flatMap {
    //      price =>
    //        logger.info(price.toString)
    //        financeFetcher.getHistoryPrice("2330", 2016, 9)
    //    } map {
    //      response =>
    //        response.foreach(historyPrice => logger.info(historyPrice.toString))
    //    } andThen {
    //      case _ => Http.terminate()
    //    }

    val financeFetcher = new FinanceFetcher()
    financeFetcher.getHistoryPrice("2330", 2016, 9) map {
      response =>
        response.foreach(historyPrice => logger.info(historyPrice.toString))
    } andThen {
      case _ => Http.terminate()
    } onComplete {
      case Success(_) =>
      case Failure(t) => t.printStackTrace()
    }
  }
}
