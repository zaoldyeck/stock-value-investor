import java.util.concurrent.Executors

import org.slf4j.{Logger, LoggerFactory}

//import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Main extends App {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  //implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
  implicit val ec = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  private val stockFetcher = new StockFetcher
  private val financeFetcher = new FinanceFetcher
  private val priceFetcher = new PriceFetcher()

  //Magic Formula
  {
    for {
      stocks <- stockFetcher.getAllStocks
      prices <-
        Future.sequence(stocks.take(100).grouped(100).map {
          stocks => priceFetcher.getRealTimePrice(stocks.map(_.id))
        }) map (_.reduce(_ ::: _))
      finances <- Future.sequence(stocks.take(100).map {
        stock => financeFetcher.getFinanceFromGoodinfo(stock.id)
      })
    } yield {
      case class StockFinance(id: String, id2: String, ROA: Double, PER: Double)
      case class StockPoint(id: String, id2: String, ROA: Double, PER: Double, point: Int)

      val stockFinances = prices zip finances map {
        pzf => StockFinance(pzf._1.id, pzf._2.id, pzf._2.ROA, pzf._1.price / pzf._2.EPS)
      }
      val pointByROA = stockFinances.sortBy(_.ROA).zipWithIndex.sortBy(_._1.id)
      val pointByPER = stockFinances.sortBy(-_.PER).zipWithIndex.sortBy(_._1.id)
      pointByROA zip pointByPER map {
        rzp =>
          StockPoint(rzp._1._1.id, rzp._2._1.id, rzp._1._1.ROA, rzp._1._1.PER, rzp._1._2 + rzp._2._2)
      } sortBy (-_.point) foreach println
    }
  } andThen {
    case _ => Http.terminate()
  } onComplete {
    case Success(_) =>
    case Failure(t) => t.printStackTrace()
  }
}
