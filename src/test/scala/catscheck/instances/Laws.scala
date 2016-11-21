package org.scalacheck
package instances

import scala.util.{Failure, Success, Try}

import cats.Eq
import cats.data.NonEmptyList
import cats.implicits._

import org.scalatest.{FunSuite, Matchers}
import org.scalatest.prop.{Configuration, PropertyChecks}
import org.typelevel.discipline.scalatest.Discipline

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.instances.all._
import org.scalacheck.rng.Seed

class CatsCheckLaws extends FunSuite
    with Matchers
    with PropertyChecks
    with Discipline
    with Setup {

  checkAll("Gen", cats.laws.discipline.MonadCombineTests[Gen].monadCombine[Int, Int, Int])
  checkAll("Gen[String]", cats.kernel.laws.GroupLaws[Gen[String]].monoid)
  checkAll("Gen[NonEmptyList[Int]]", cats.kernel.laws.GroupLaws[Gen[NonEmptyList[Int]]].semigroup)

  checkAll("Cogen", cats.laws.discipline.ContravariantTests[Cogen].contravariant[Int, Int, Int])
  checkAll("Cogen", cats.laws.discipline.CartesianTests[Cogen].cartesian[Int, Int, Int])
  checkAll("Cogen", cats.laws.discipline.MonoidKTests[Cogen].monoidK[Int])
}


trait Setup {
  implicit def genEq[A: Eq]: Eq[Gen[A]] =
    gen.sampledEq(20)

  implicit def cogenEq[A: Arbitrary]: Eq[Cogen[A]] =
    cogen.sampledEq(20)

  implicit lazy val arbitrarySeed: Arbitrary[Seed] =
    Arbitrary(Gen.choose(Long.MinValue, Long.MaxValue).map(n => Seed(n)))

  implicit lazy val cogenSeed: Cogen[Seed] =
    Cogen[Long].contramap(_.long._1)

  implicit def arbitraryNonEmptyList[A: Arbitrary]: Arbitrary[NonEmptyList[A]] =
    Arbitrary((arbitrary[A], arbitrary[List[A]]).map2(NonEmptyList(_, _)))

  // scalacheck's built-in Arbitrary[Gen[A]] is not great.
  implicit def arbitraryGen[A: Arbitrary]: Arbitrary[Gen[A]] = {
    val simple = Gen.const(arbitrary[A])
    val complex = arbitrary[Seed => Seed].map { f =>
      Gen.gen((params, seed) => arbitrary[A].doApply(params, f(seed)))
    }
    Arbitrary(Gen.oneOf(simple, complex))
  }

  // scalacheck does not have Arbitrary[Cogen[A]] yet.
  implicit def arbitraryCogen[A: Cogen]: Arbitrary[Cogen[A]] =
    Arbitrary(arbitrary[Seed => Seed].map { f =>
      Cogen((seed, a) => f(Cogen[A].perturb(seed, a)))
    })
}
