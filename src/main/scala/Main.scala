import java.util.Date

import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Main {
  def main(args: Array[String]): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)
    /*
    new StockFetcher().getAllStocks.map {
      stocks => stocks.foreach(println)
    } andThen {
      case _ => Http.terminate()
    }

    val priceFetcher = new PriceFetcher()
    priceFetcher.getRealTimePrice("2330").flatMap {
      price =>
        logger.info(price.toString)
        priceFetcher.getHistoryPrice("2330", 2016, 9)
    } map {
      response =>
        response.foreach(historyPrice => logger.info(historyPrice.toString))
    } andThen {
      case _ => Http.terminate()
    }
    */

    //val financeFetcher = new FinanceFetcher()
    //    financeFetcher.getHistoryPrice("2330", 2016, 9) map {
    //      response =>
    //        response.foreach(historyPrice => logger.info(historyPrice.toString))
    //    } andThen {
    //      case _ => Http.terminate()
    //    } onComplete {
    //      case Success(_) =>
    //      case Failure(t) => t.printStackTrace()
    //    }

    //    new FinanceFetcher().getFinance("2330", 2016).map {
    //      finance => logger.info(finance.toString)
    //    } andThen {
    //      case _ => Http.terminate()
    //    } onComplete {
    //      case Success(_) =>
    //      case Failure(t) => t.printStackTrace()
    //    }

    //Magic Formula
    {
      for {
        stocks <- new StockFetcher().getAllStocks
        finances <- Future.traverse(stocks) {
          stock => new FinanceFetcher().getFinance(stock.id, 2016)
        }
      } yield {
        finances.sortBy(_.id).foreach(f => logger.info(f.toString))
      }
    } andThen {
      case _ => Http.terminate()
    } onComplete {
      case Success(_) =>
      case Failure(t) => t.printStackTrace()
    }
  }
}
