import org.scalacheck.Prop
import org.scalatest.{Assertion, FunSuite, Matchers}
import org.scalatest.prop.Checkers

abstract class BaseTest extends FunSuite with Matchers with Checkers {

  val error: Throwable = new RuntimeException("Test Exception")

  implicit def checkClassToProp(checkClass: CheckClass)(implicit P: Boolean => Prop): Prop =
    checkClass.check
}

trait CheckClass {

  def assertCheck: Assertion

  def check: Boolean = {
    assertCheck
    true
  }

}