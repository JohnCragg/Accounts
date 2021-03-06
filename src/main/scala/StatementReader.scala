import java.io.File

import domain.transaction.{RegisterTransaction, Transaction, TransactionType}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import sun.security.provider.PolicyParser.ParsingException

object StatementReader {
  val formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")

  def createTransaction(statementLine: String): Transaction = {
    val transaction: Array[String] = statementLine.split(",")
    transaction match {
      case Array(date, transactionType, sortCode, accountNumber, description, debitAmount, creditAmount, balanceAfter) =>
        Transaction(
          formatter.parseDateTime(date),
          TransactionType(transactionType),
          sortCode,
          accountNumber,
          description,
          createAmount(debitAmount),
          createAmount(creditAmount),
          createAmount(balanceAfter)
        )
      case _ => throw new ParsingException("incorrect number of fields")
    }
  }

  def parseFile(file: File) = {
    val bufferedSource = io.Source.fromFile(file)
    val transactions = for {
      line <- bufferedSource.getLines().drop(1).toList
      transaction = createTransaction(line)
    } yield RegisterTransaction(transaction)
    //@TODO: do close in a finally block
    bufferedSource.close
    transactions
  }

  private val createAmount: String => BigDecimal = {
    case s if s == "" => BigDecimal(0)
    case t => BigDecimal(t)
  }
}