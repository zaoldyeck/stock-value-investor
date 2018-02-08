import java.util.Date
import java.util.concurrent.Executors

import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object Main {
  def main(args: Array[String]): Unit = {
    val logger: Logger = LoggerFactory.getLogger(this.getClass)
    implicit val ec = ExecutionContext.fromExecutorService(Executors.newWorkStealingPool(1))

    //Magic Formula
    /*
    {
      for {
        stocks <- new StockFetcher().getAllStocks
        finances <- Future.traverse(stocks.take(1)) {
          stock => new FinanceFetcher().getFinance("2330", 2017)
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
    */

    /*
    new PriceFetcher().getRealTimePrice("2330").map {
      price => logger.info(price.toString)
    }
    */

    new FinanceFetcher().getFinance("2330", Duration.ThreeYear).map {
      finance => println(finance)
    } andThen {
      case _ => Http.terminate()
    } onComplete {
      case Success(_) =>
      case Failure(t) => t.printStackTrace()
    }
  }
}
