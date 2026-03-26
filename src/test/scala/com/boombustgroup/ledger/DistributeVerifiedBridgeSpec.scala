package com.boombustgroup.ledger

import org.scalacheck.Gen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

/** Test-only bridge between the pure distribution reference and the Stainless floor-with-residual list shape.
  *
  * This is not a formal Stainless proof over the production `Array[Long]` implementation. It narrows the gap by checking that the pure
  * executable reference model follows the same BigInt list shape that `Verified.scala` proves.
  */
class DistributeVerifiedBridgeSpec extends AnyFlatSpec with Matchers with ScalaCheckPropertyChecks:

  private def bigIntFloorPrefix(total: Long, shares: List[Long]): List[BigInt] =
    val shareSum = shares.foldLeft(0L)(_ + _)
    val denom    = BigInt(shareSum)
    shares.init.map(share => (BigInt(total) * BigInt(share)) / denom)

  private def verifiedShape(total: Long, shares: List[Long]): List[BigInt] =
    val prefix = bigIntFloorPrefix(total, shares)
    prefix :+ (BigInt(total) - prefix.foldLeft(BigInt(0))(_ + _))

  "DistributeReference.distribute" should "match the Verified floor-with-residual list shape" in {
    val genTotal  = Gen.choose(1L, 10000000000L)
    val genSize   = Gen.choose(1, 20)
    val genShares = genSize.flatMap(n => Gen.listOfN(n, Gen.choose(1L, 10000L)))

    forAll(genTotal, genShares) { (total, shares) =>
      val reference = DistributeReference.distribute(total, shares).map(BigInt(_))
      reference shouldBe verifiedShape(total, shares)
    }
  }

  it should "match the Stainless floor-prefix and residual decomposition for singleton inputs" in {
    val total  = 12345L
    val shares = List(10000L)

    DistributeReference.distribute(total, shares).map(BigInt(_)) shouldBe verifiedShape(total, shares)
  }
