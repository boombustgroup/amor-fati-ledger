package com.boombustgroup.ledger

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MutableWorldStateSpec extends AnyFlatSpec with Matchers:

  private val HH    = EntitySector.Households
  private val Banks = EntitySector.Banks
  private val Asset = AssetType.DemandDeposit
  private val Loan  = AssetType.FirmLoan

  "MutableWorldState" should "reuse the same backing array for the same sector and asset" in {
    val state  = new MutableWorldState(Map(HH -> 3, Banks -> 2))
    val first  = state.getBalances(HH, Asset)
    val second = state.getBalances(HH, Asset)

    first(0) = 123L

    second should be theSameInstanceAs first
    state.balance(HH, Asset, 0) shouldBe 123L
  }

  it should "keep separate backing arrays for different sector or asset keys" in {
    val state       = new MutableWorldState(Map(HH -> 3, Banks -> 2))
    val hhDeposits  = state.getBalances(HH, Asset)
    val bankDeposit = state.getBalances(Banks, Asset)
    val hhLoans     = state.getBalances(HH, Loan)

    hhDeposits(0) = 10L
    bankDeposit(0) = 20L
    hhLoans(0) = 30L

    state.balance(HH, Asset, 0) shouldBe 10L
    state.balance(Banks, Asset, 0) shouldBe 20L
    state.balance(HH, Loan, 0) shouldBe 30L
  }

  it should "return zero for missing balances" in {
    val state = new MutableWorldState(Map(HH -> 3))

    state.balance(HH, Asset, 0) shouldBe 0L
  }

  it should "omit zero entries from snapshots" in {
    val state = new MutableWorldState(Map(HH -> 3, Banks -> 2))
    val hh    = state.getBalances(HH, Asset)
    val bank  = state.getBalances(Banks, Asset)

    hh(0) = 11L
    hh(1) = 0L
    bank(1) = -11L

    state.snapshot shouldBe Map(
      (HH, Asset, 0)    -> 11L,
      (Banks, Asset, 1) -> -11L
    )
  }

  it should "sum totals only for the requested asset type" in {
    val state       = new MutableWorldState(Map(HH -> 3, Banks -> 2))
    val hhDeposits  = state.getBalances(HH, Asset)
    val bankDeposit = state.getBalances(Banks, Asset)
    val hhLoans     = state.getBalances(HH, Loan)

    hhDeposits(0) = 100L
    hhDeposits(1) = -40L
    bankDeposit(0) = -60L
    hhLoans(0) = 999L

    state.totalForAsset(Asset) shouldBe 0L
    state.totalForAsset(Loan) shouldBe 999L
  }

  it should "default unknown sector sizes to 1" in {
    val state = new MutableWorldState(Map(HH -> 3))

    state.sectorSize(Banks) shouldBe 1
    state.getBalances(Banks, Asset).length shouldBe 1
  }
