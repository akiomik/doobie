package doobie.util

import scala.util.{ Left => -\/, Right => \/- }
import fs2.interop.cats._
import doobie.imports._
import org.specs2.mutable.Specification
import Predef._


object queryspec extends Specification {

  val xa = DriverManagerTransactor[IOLite](
    "org.h2.Driver",
    "jdbc:h2:mem:queryspec;DB_CLOSE_DELAY=-1",
    "sa", ""
  )

  val q = Query[String,Int]("select 123 where ? = 'foo'", None)

  "Query (non-empty)" >> {
    "to" in {
      q.to[List]("foo").transact(xa).unsafePerformIO must_=== List(123)
    }
    "unique" in {
      q.unique("foo").transact(xa).unsafePerformIO must_=== 123
    }
    "option" in {
      q.option("foo").transact(xa).unsafePerformIO must_=== Some(123)
    }
    "map" in {
      q.map("x" * _).to[List]("foo").transact(xa).unsafePerformIO must_=== List("x" * 123)
    }
    "contramap" in {
      q.contramap[Int](n => "foo" * n).to[List](1).transact(xa).unsafePerformIO must_=== List(123)
    }
  }

  "Query (empty)" >> {
    "to" in {
      q.to[List]("bar").transact(xa).unsafePerformIO must_=== Nil
    }
    "unique" in {
      q.unique("bar").transact(xa).attempt.unsafePerformIO must_=== -\/(invariant.UnexpectedEnd)
    }
    "option" in {
      q.option("bar").transact(xa).unsafePerformIO must_=== None
    }
    "map" in {
      q.map("x" * _).to[List]("bar").transact(xa).unsafePerformIO must_=== Nil
    }
    "contramap" in {
      q.contramap[Int](n => "bar" * n).to[List](1).transact(xa).unsafePerformIO must_=== Nil
    }
  }

  "Query0 from Query (non-empty)" >> {
    "to" in {
      q.toQuery0("foo").to[List].transact(xa).unsafePerformIO must_=== List(123)
    }
    "unique" in {
      q.toQuery0("foo").unique.transact(xa).unsafePerformIO must_=== 123
    }
    "option" in {
      q.toQuery0("foo").option.transact(xa).unsafePerformIO must_=== Some(123)
    }
    "map" in {
      q.toQuery0("foo").map(_ * 2).list.transact(xa).unsafePerformIO must_=== List(246)
    }
  }

  "Query0 from Query (empty)" >> {
    "to" in {
      q.toQuery0("bar").to[List].transact(xa).unsafePerformIO must_=== Nil
    }
    "unique" in {
      q.toQuery0("bar").unique.transact(xa).attempt.unsafePerformIO must_=== -\/(invariant.UnexpectedEnd)
    }
    "option" in {
      q.toQuery0("bar").option.transact(xa).unsafePerformIO must_=== None
    }
    "map" in {
      q.toQuery0("bar").map(_ * 2).list.transact(xa).unsafePerformIO must_=== Nil
    }
  }

  val q0n = Query0[Int]("select 123 where 'foo' = 'foo'", None)

  "Query0 via constructor (non-empty)" >> {
    "to" in {
      q0n.to[List].transact(xa).unsafePerformIO must_=== List(123)
    }
    "unique" in {
      q0n.unique.transact(xa).unsafePerformIO must_=== 123
    }
    "option" in {
      q0n.option.transact(xa).unsafePerformIO must_=== Some(123)
    }
    "map" in {
      q0n.map(_ * 2).list.transact(xa).unsafePerformIO must_=== List(246)
    }
  }

  val q0e = Query0[Int]("select 123 where 'bar' = 'foo'", None)

  "Query0 via constructor (empty)" >> {
    "to" in {
      q0e.to[List].transact(xa).unsafePerformIO must_=== Nil
    }
    "unique" in {
      q0e.unique.transact(xa).attempt.unsafePerformIO must_=== -\/(invariant.UnexpectedEnd)
    }
    "option" in {
      q0e.option.transact(xa).unsafePerformIO must_=== None
    }
    "map" in {
      q0e.map(_ * 2).list.transact(xa).unsafePerformIO must_=== Nil
    }
  }

}