package org.scalacheck
package instances

import scala.language.higherKinds

import cats._
import cats.implicits._

import functor.Contravariant
import rng.Seed

object cogen extends CogenInstances {

  def sampledEq[A](trials: Int)(implicit ev: Arbitrary[A]): Eq[Cogen[A]] =
    new Eq[Cogen[A]] {
      def eqv(x: Cogen[A], y: Cogen[A]): Boolean = {
        val gen = ev.arbitrary
        val params = Gen.Parameters.default
        def loop(count: Int, retries: Int, seed: Seed): Boolean =
          if (retries <= 0) sys.error("generator failed")
          else if (count <= 0) true
          else {
            val rx = gen.doApply(params, seed)
            rx.retrieve match {
              case None =>
                loop(count, retries - 1, rx.seed)
              case Some(a) =>
                val s = Seed.random
                val sx = x.perturb(s, a)
                val sy = y.perturb(s, a)
                if (sx != sy) false
                else loop(count - 1, retries, rx.seed)
            }
          }
        loop(trials, trials, Seed.random)
      }
    }
}

trait CogenInstances {
  implicit val cogenContravariant: Contravariant[Cogen] =
    new Contravariant[Cogen] {
      def contramap[A, B](c: Cogen[A])(f: B => A): Cogen[B] =
        c.contramap(f)
    }

  implicit val cogenCartesian: Cartesian[Cogen] =
    new Cartesian[Cogen] {
      def product[A, B](ca: Cogen[A], cb: Cogen[B]): Cogen[(A, B)] =
        Cogen.tuple2(ca, cb)
    }

  implicit val cogenMonoidK: MonoidK[Cogen] =
    new MonoidK[Cogen] {
      def empty[A]: Cogen[A] =
        Cogen { (seed, _) => seed }
      def combineK[A](x: Cogen[A], y: Cogen[A]): Cogen[A] =
        Cogen { (seed, a) => y.perturb(x.perturb(seed, a), a) }
    }
}
