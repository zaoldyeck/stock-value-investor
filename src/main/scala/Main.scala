import java.util.concurrent.Executors

import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Main extends App {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  //implicit val ec = ExecutionContext.fromExecutorService(Executors.newWorkStealingPool(1))
  //implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  implicit val ec = ExecutionContext.fromExecutor(Executors.newSingleThreadScheduledExecutor)
  private val stockFetcher = new StockFetcher
  private val financeFetcher = new FinanceFetcher

  //Magic Formula
  {
    for {
      stocks <- stockFetcher.getAllStocks
      finances <- Future.sequence(stocks.map {
        stock => financeFetcher.getFinance(stock.id, 2016)
      })
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
