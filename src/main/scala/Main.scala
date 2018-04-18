import java.util.concurrent.Executors

import org.slf4j.{Logger, LoggerFactory}

//import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Main extends App {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  //implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
  implicit val ec = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
  implicit val timeInterval = 20000

  new Job().magicFormula andThen {
    case _ => Http.terminate()
  } onComplete {
    case Success(_) =>
    case Failure(t) => t.printStackTrace()
  }
}
