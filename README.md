## Cats-check

### Overview

*Cats-check* associates ScalaCheck data types (i.e. `Gen` and `Cogen`)
with Cats type classes (e.g. `Monad`).

### Set up

*Cats-check* is not yet published.

### Usage

The easiest way to use these type class instances is with a single
bulk import:

```scala
import org.scalacheck.instances.all._
```

You can just import all the instances for `Gen` (or `Cogen`) with the
following imports:

```scala
import org.scalacheck.instances.gen._    // just Gen instances
import org.scalacheck.instances.cogen._  // just Cogen instances
```

You can also import instances by name if you prefer (although this
approach is not recommended by the author):

```scala
import org.scalacheck.instances.gen.genMonadCombine
```

### Custom Eq instances

In addition to the implicit instances (described above), this library
provides explicit constructors you can use to build custom `Eq`
instances. These instances cannot guarantee total equality, but can be
used to rule out instances that can be shown to behave differently.

```scala
import cats.Eq
import org.scalacheck.Gen

// try to see if two gen instances are equal by comparing values
// generated for the same seed. If 20 iterations are successful
// then we consider the instances equal (though they might have
// an unseen difference).
implicit def genEq[A: Eq]: Eq[Gen[A]] =
  org.scalacheck.instances.gen.sampledEq(20)

// try to see if two cogen instances are equal by comparing seed
// permuations for the same 'A' value. if 20 iterations are
// successful then we consider the instances equal (though they
// might have an unseen difference).
implicit def cogenEq[A: Arbitrary]: Eq[Cogen[A]] =
  org.scalacheck.instances.cogen.sampledEq(20)
```

Why would you want these?

*Cats-check* uses these instances internally to try to catch errors
(i.e. law violations). During testing it's often useful to be able to
provide these kind of "best effort" instances for types that wouldn't
otherwise be directly comparable.

### Copyright and License

All code is available to you under the MIT license, available at
http://opensource.org/licenses/mit-license.php and also in the COPYING
file.

Copyright Erik Osheim, 2016.
