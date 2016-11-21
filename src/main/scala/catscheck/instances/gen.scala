package org.scalacheck
package instances

import scala.language.higherKinds
import scala.util.{Failure, Success, Try}

import cats._
import cats.implicits._

import Gen.Parameters
import rng.Seed

object gen extends GenInstances {

  def sampledEq[A: Eq](trials: Int): Eq[Gen[A]] =
    new Eq[Gen[A]] {
      def eqv(x: Gen[A], y: Gen[A]): Boolean = {
        val params = Gen.Parameters.default
        def loop(count: Int, seed: Seed): Boolean =
          if (count <= 0) true else {
            val tx = Try(x.doApply(params, seed))
            val ty = Try(y.doApply(params, seed))
            (tx, ty) match {
              case (Failure(_), Failure(_)) =>
                loop(count - 1, Seed.random)
              case (Success(rx), Success(ry)) =>
                if (rx.retrieve != ry.retrieve) false
                else loop(count - 1, seed.next)
              case _ =>
                println("nope3!")
                false
            }
          }
        loop(trials, Seed.random)
      }
    }
}

trait GenInstances extends GenInstances0 {

  implicit def genMonoid[A: Monoid]: Monoid[Gen[A]] =
    new Monoid[Gen[A]] {
      val empty: Gen[A] =
        Gen.const(Monoid[A].empty)
      def combine(gx: Gen[A], gy: Gen[A]): Gen[A] =
        for { x <- gx; y <- gy } yield x |+| y
    }

  implicit val genMonadCombine: MonadCombine[Gen] =
    new MonadCombine[Gen] {
      def empty[A]: Gen[A] =
        Gen.fail
      def combineK[A](gx: Gen[A], gy: Gen[A]): Gen[A] =
        Gen.gen { (params, seed) =>
          val rx = gx.doApply(params, seed)
          if (rx.retrieve.isDefined) rx
          else gy.doApply(params, rx.seed)
        }
      def pure[A](a: A): Gen[A] =
        Gen.const(a)
      override def map[A, B](g: Gen[A])(f: A => B): Gen[B] =
        g.map(f)
      def flatMap[A, B](g: Gen[A])(f: A => Gen[B]): Gen[B] =
        g.flatMap(f)
      def tailRecM[A, B](a: A)(f: A => Gen[Either[A, B]]): Gen[B] = {
        def loop(a: A, params: Parameters, seed: Seed): Gen.R[B] = {
          val r = f(a).doApply(params, seed)
          r.retrieve match {
            case Some(Left(a)) => loop(a, params, r.seed)
            case Some(Right(b)) => Gen.r(Some(b), r.seed)
            case None => Gen.r(None, r.seed)
          }
        }
        Gen.gen((params, seed) => loop(a, params, seed))
      }
    }
}

trait GenInstances0 {
  implicit def genSemigroup[A: Semigroup]: Semigroup[Gen[A]] =
    new Semigroup[Gen[A]] {
      def combine(gx: Gen[A], gy: Gen[A]): Gen[A] =
        for { x <- gx; y <- gy } yield x |+| y
    }
}
