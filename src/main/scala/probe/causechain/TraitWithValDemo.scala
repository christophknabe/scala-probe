package probe.causechain

import java.io.FileNotFoundException

import scala.annotation.tailrec

/** Demonstrates the definition and reporting of a chain of reportable errors unifying Scala and Java worlds.
  *
  * @author Christoph Knabe
  * @since 2021-03-31 */
object TraitWithValDemo {

  /** Reportable error with causal chain. */
  trait Reportable {
    val message: String
    val cause: Option[Reportable] = None
  }

  def toReportableOption(throwable: Throwable): Option[Reportable] = {
    Option(throwable).map(FromThrowable)
  }

  final case class FromThrowable(throwable: Throwable) extends Reportable {

    val message: String = Option(throwable).map(_.toString).getOrElse("(null: Throwable)")

    override val cause: Option[Reportable] = toReportableOption(throwable.getCause)

  }

  final case class FileNotFound(filename: String, causedBy: Throwable) extends Reportable {
    val message = s"${getClass.getName}: filename=$filename"
    override val cause: Option[Reportable] = toReportableOption(causedBy)
  }

  final case class FileCopyFailure(source: String, dest: String, causedBy: Reportable) extends Reportable {
    val message = s"${getClass.getName}: File $source could not be copied to $dest"
    override val cause: Option[Reportable] = Some(causedBy)
  }

  def report(reportable: Reportable): String = {
    toReverseCauseList(reportable, Nil).reverseIterator.map(_.message).mkString("\nCaused by ")
  }

  /** Builds a reverse List of the [[Reportable]] and its causal chain by following the `.cause` link until `None` is reached. */
  @tailrec
  private def toReverseCauseList(causalChain: Reportable, accu: List[Reportable]): List[Reportable] = {
    val newAccu = causalChain :: accu
    causalChain.cause match {
      case None => newAccu
      case Some(tailCausalChain) => toReverseCauseList(tailCausalChain, newAccu)
    }
  }

  def main(args: Array[String]): Unit = {
    val fnfe = new FileNotFoundException
    val rte = new RuntimeException("Could not open file", fnfe)
    val fnf = FileNotFound("/home/knabe/abc.txt", rte)
    val fcf = FileCopyFailure("abc.txt", "def.txt", fnf)
    println(report(fcf))
    fcf match {
      case FileCopyFailure(source, dest, causedBy) => println(s"FileCopyFailure matched with source=$source, dest=$dest, causedBy=$causedBy")
    }
  }

}
