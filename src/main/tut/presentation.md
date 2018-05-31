autoscale: true
build-lists: true
slidenumbers: true
footer: @raulraja @47deg

# [fit] Better Types => Fewer Tests

![filtered](romanesco.jpg)

---

# [fit] More tests = Better Software?

![filtered](romanesco.jpg)

---

# [fit] Are tests a factor in correctness?

![filtered](romanesco.jpg)

---

# [fit] What are we testing?

---

# [fit] What are we testing?

Programs

```tut
class Counter(var amount: Int) {
  require(amount >= 0, s"($amount seed value) must be a positive integer") 
  def increase(): Unit =
    amount += 1
}
```

---

# [fit] What are we testing?

Input values are in range of acceptance

```tut
class CounterSpec extends BaseTest {
  test("Can't be constructed with negative numbers") {
    the [IllegalArgumentException] thrownBy {
      new Counter(-1)
    } should have message "requirement failed: (-1 seed value) must be a positive integer"
  }
}
(new CounterSpec).execute
```

---

# [fit] What are we testing?

Side effects caused by programs.

```tut
class CounterSpec extends BaseTest {
  test("`Counter#amount` is mutated after `Counter#increase` is invoked") {
    val counter = new Counter(0)
    counter.increase()
    counter.amount shouldBe 1
  }
}
(new CounterSpec).execute
```

---

# [fit] What are we testing?

Programs produce an expected output value given an accepted input value.

```tut
class CounterSpec extends BaseTest {
  test("`Counter#amount` is properly initialized") {
    new Counter(0).amount shouldBe 0
  }
}
(new CounterSpec).execute
```

---

# [fit] What are we testing?

Changes in requirements.

```tut
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.concurrent.atomic.AtomicInteger

class FutureCounter(val amount: AtomicInteger) {
  require(amount.get >= 0, s"($amount seed value) must be a positive atomic integer") 
  def increase(): Future[Int] =
    Future { 
      Thread.sleep(1000) // force latency
      amount.incrementAndGet
    } 
}
```

---

# [fit] What are we testing?

Changes in requirements

```tut
class FutureCounterSpec extends BaseTest {
  test("`FutureCounter#amount` is mutated after `FutureCounterv#increase` is invoked") {
    val counter = new FutureCounter(new AtomicInteger(0))
    counter.increase()
    counter.amount.get shouldBe 1
  }
}
(new FutureCounterSpec).execute
```

---

# [fit] What are we testing?

Changes in requirements

```tut
import scala.concurrent.duration._

class FutureCounterSpec extends BaseTest {
  test("`FutureCounter#amount` is mutated after `FutureCounterv#increase` is invoked") {
    val counter = new FutureCounter(new AtomicInteger(0))
    val result = counter.increase() map { _ => counter.amount.get shouldBe 1 }
    Await.result(result, 10.seconds)
  }
}
(new FutureCounterSpec).execute
```

---

# [fit] What are we testing?

- Input values are in range of acceptance
- Side effects caused by programs
- Programs produce an expected output value given an accepted input value.
- Changes in requirements

---

# [fit] The Dark Path

> Now, ask yourself why these defects happen too often. 
> If your answer is that our languages don’t prevent them, 
> then I strongly suggest that you quit your job and never 
> think about being a programmer again; 
> because *defects are never the fault of our languages.* 

> *Defects are the fault of programmers.* 

> It is programmers who create defects – not languages.

― Robert C. Martin (Uncle Bob) [The Dark Path](https://blog.cleancoder.com/uncle-bob/2017/01/11/TheDarkPath.html)

---

# [fit] The Dark Path

> And what is it that programmers are supposed to do to prevent defects? 
> I’ll give you one guess. Here are some hints. 
> It’s a verb. It starts with a “T”. Yeah. 
> You got it. __TEST!__

― Robert C. Martin (Uncle Bob) [The Dark Path](https://blog.cleancoder.com/uncle-bob/2017/01/11/TheDarkPath.html)

---

# [fit] The Dark Path

I disagree

> And what is it that programmers are supposed to do to prevent defects? 
> I’ll give you one guess. Here are some hints. 
> It’s a verb. It starts with a “T”. Yeah. 
> You got it. ~~TEST!~~, 

---

# [fit] The Dark Path

And so does the compiler

> And what is it that programmers are supposed to do to prevent defects? 
> I’ll give you one guess. Here are some hints. 
> It’s a verb. It starts with a “T”. Yeah. 
> You got it. ~~TEST!~~, 

__TYPES & FP!__

-- Mr Compiler. 

---

## What is Functional Programming ##

> In computer science, functional programming
> is a programming paradigm.

> A style of building the structure and elements
> of computer programs that treats computation
> as the evaluation of mathematical functions
> and avoids changing-state and mutable data.

-- Wikipedia

---

## Common traits of Functional Programming ##

- Higher-order functions
- Immutable data
- Referential transparency
- Lazy evaluation
- Recursion
- Abstractions

---

# [fit] What are we testing?

Back to our original concerns

- __Input values are in range of acceptance__
- Programs produce an expected output value given an accepted input value.
- Side effects caused by programs
- Changes in requirements

---

# [fit] What are we testing?

Back to our original concerns

- __Input values are in range of acceptance__
- Programs produce an expected output value given an accepted input value.
- Side effects caused by programs
- Changes in requirements

---

# [fit] What are we testing?

Input values are in range of acceptance.

The issue here is that `Int` is a poorly chosen type. Let's fix that!

```tut
class CounterSpec extends BaseTest {
  test("Can't be constructed with negative numbers") {
    the [IllegalArgumentException] thrownBy {
      new Counter(-1)
    } should have message "requirement failed: (-1 seed value) must be a positive integer"
  }
}
(new CounterSpec).execute
```

---

# [fit] What are we testing?

Input values are in range of acceptance.

```tut
import eu.timepit.refined.W
import eu.timepit.refined.cats.syntax._
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.numeric._

type Amount = Int Refined GreaterEqual[W.`0`.T]
object Amount extends RefinedTypeOps[Amount, Int]

class Counter(var amount: Amount) {
  def increase(): Unit = 
    Amount.from(amount.value + 1).foreach(v => amount = v)
}
```

---

# [fit] What are we testing?

Input values are in range of acceptance.

The compiler can verify the range and we can properly type `amount`

```diff
+ import eu.timepit.refined.api.{Refined, RefinedTypeOps}
+ import eu.timepit.refined.numeric._

+ type Amount = Int Refined GreaterEqual[W.`0`.T]
+ object Amount extends RefinedTypeOps[Amount, Int]

- class Counter(var amount: Int) {
+ class Counter(var amount: Amount) {
- require(amount >= 0, s"($amount seed value) must be a positive integer") 
  def increase(): Unit =
-   amount += 1
+   Amount.from(amount.value + 1).foreach(v => amount = v)
}
```

---

# [fit] What are we testing?

Input values are in range of acceptance.

The compiler can verify the range and we can properly type `amount`

```tut
class CounterSpec extends BaseTest {
  // Testing an invariant xD
  test("Can't compile with literal negative number") {
    "new Counter(-1)" shouldNot compile
  }
}
(new CounterSpec).execute
```

---

# [fit] What are we testing?

Input values are in range of acceptance.

The compiler can verify the range and we can properly type `amount`

```diff
class CounterSpec extends BaseTest {
-  test("Can't be constructed with negative numbers") {
-    the [IllegalArgumentException] thrownBy {
-      new Counter(-1)
-    } should have message "requirement failed: (-1 seed value) must be a positive integer"
-  }
}
```

---

# [fit] What are we testing?

Back to our original concerns

- ~~Input values are in range of acceptance~~
- __Side effects caused by programs__
- Programs produce an expected output value given an accepted input value.
- Changes in requirements

---

# [fit] What are we testing?

Side effects caused by programs.

```tut
class CounterSpec extends BaseTest {
  test("`Counter#amount` is mutated after `Counter#increase` is invoked") {
    val counter = new Counter(Amount(0))
    counter.increase()
    counter.amount.value shouldBe 1
  }
}
(new CounterSpec).execute
```

---

# [fit] What are we testing?

Side effects caused by programs.

```tut
class Counter(var amount: Amount) { // mutable
  def increase(): Unit = // Unit does not return anything useful
    Amount.from(amount.value + 1).foreach(v => amount = v) // mutates the external scope
}
```

---

# [fit] What are we testing?

No need to test side Effects if functions are __PURE!__

```tut
class Counter(val amount: Amount) { // values are immutable
  def increase(): Counter = // Every operation returns an immutable copy
    Amount.validate(amount.value + 1).fold( // Amount.validate does not need to be tested
     { _ => new Counter(amount) }, // potential failures are also contemplated
     { a => new Counter(a) }
    )
}
```

---

# [fit] What are we testing?

Side effects caused by programs.

```diff
- class Counter(var amount: Amount) { // mutable
+ class Counter(val amount: Amount) { // values are immutable
-  def increase(): Unit = // Unit does not return anything useful
+  def increase(): Counter = // Every operation returns an immutable copy
-    Amount.from(amount.value + 1).foreach(v => amount = v) // mutates the external scope
+    Amount.validate(amount.value + 1).fold( // Amount.validate does not need to be tested
+     { _ => new Counter(amount) }, // potential failures are also contemplated
+     { a => new Counter(a) }
+    )
}
```

---

# [fit] What are we testing?

Back to our original concerns

- ~~Input values are in range of acceptance~~
- ~~Side effects caused by programs~~
- __Programs produce an expected output value given an accepted input value__
- Changes in requirements

---

# [fit] What are we testing?

Programs produce an expected output value given an accepted input value.

```tut
class CounterSpec extends BaseTest {
  test("`Counter#amount` is immutable and pure") { // The actual test case
    new Counter(Amount(0)).increase().amount shouldBe Amount(1)
  }
}
(new CounterSpec).execute
```

---

# [fit] What are we testing?

Back to our original concerns

- ~~Input values are in range of acceptance~~
- ~~Side effects caused by programs~~
- Programs produce an expected output value given an accepted input value
- __Changes in requirements__

---

# [fit] What are we testing?

Changes in requirements made us realize our component needed to support also async computations

```tut
class FutureCounter(val amount: Amount) { // values are immutable
  def increase(): Future[Counter] = // Every operation returns an immutable copy
    Future {
      Amount.validate(amount.value + 1).fold( // Amount.validate does not need to be tested
       { _ => new Counter(amount) }, // potential failures are also contemplated
       { a => new Counter(a) }
      )
    }
}
```

---

# [fit] What are we testing?

We are forcing call sites to block even those that did not want to be async

```tut
class CounterSpec extends BaseTest {
  test("`FutureCounter#amount` is immutable and pure") { // The actual test case
    val asyncResult = new FutureCounter(Amount(0)).increase()
    Await.result(asyncResult, 10.seconds).amount shouldBe Amount(1)
  }
}
(new CounterSpec).execute
```

---

# [fit] What are we testing?

Most specialized implementations denote insufficient polymorphism

```tut
class FutureCounter(val amount: Amount) { 
  def increase: Future[Counter] = // increase returns immediately and starts its computation async
    Future {
      Amount.validate(amount.value + 1).fold( // Amount.validate does not need to be tested
       { _ => new Counter(amount) }, // potential failures are also contemplated
       { a => new Counter(a) }
      )
    }
}
```

---

# [fit] What are we testing?

We reduce the possibility of bugs and increase flexibility by working with abstractions such as type classes

```tut
import cats.effect.Sync

class Counter[F[_]: Sync](val amount: Amount) { // F[_] can be any box for which `Sync` instance is available
  def increase(): F[Counter[F]] = // A counter is returned in a generic box 
    Sync[F].delay { //delay defers execution of the contained block
      Amount.validate(amount.value + 1).fold( 
       { _ => new Counter(amount) }, 
       { a => new Counter(a) }
      )
    }
}
```

---

# [fit] What are we testing?

Our program is now polymorphic and the same code supports many different data types.

```tut:silent
import monix.eval.Task
import cats.effect.IO

import cats.effect.implicits._

val amount = Amount(0)
```
```tut
new Counter[IO](amount).increase
```
```tut
new Counter[Task](amount).increase
```

---

# [fit] What are we NOT testing?

---

# [fit] What are we NOT testing?

## Invariants

---

# [fit] What are we NOT testing?

## Invariants

> In computer science, an invariant is a condition that can be relied upon to be true during execution of a program

― Wikipedia Invariant_(computer_science)

- Compilation: We trust the compiler says our values will be constrained by properties
- Math Laws: (identity, associativity, commutativity, ...)
- 3rd party dependencies

---

## Questions? & Thanks! ##

@raulraja
@47deg
http://github.com/47deg/typeclasses-in-fp-architecture
https://speakerdeck.com/raulraja/typeclasses-in-fp-architecture