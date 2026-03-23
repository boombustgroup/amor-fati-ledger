package com.boombustgroup.ledger

/** Proportional distribution with residual plugging.
  *
  * Distributes `total` across N recipients according to `shares`. The last recipient absorbs the rounding residual, guaranteeing:
  * distribute(total, shares).sum == total
  *
  * This is exact by construction (Long addition). No tolerance needed.
  */
object Distribute:

  /** Distribute total across shares. Last element gets residual.
    *
    * @param total
    *   amount to distribute (scale 10^4)
    * @param shares
    *   proportional weights (scale 10^4, i.e. 5000 = 50%)
    * @return
    *   array of amounts summing to exactly `total`
    */
  def distribute(total: Long, shares: Array[Long]): Array[Long] =
    require(shares.nonEmpty, "Cannot distribute to zero recipients")
    val n        = shares.length
    val results  = new Array[Long](n)
    val shareSum = shares.sum
    require(shareSum > 0, "Share sum must be positive")
    var allocated = 0L
    var i         = 0
    while i < n - 1 do
      results(i) = bankerRound(BigInt(total) * BigInt(shares(i)), shareSum)
      allocated += results(i)
      i += 1
    results(n - 1) = total - allocated
    results

  /** Banker's rounding: (a * b + half) / divisor, round half to even. */
  private def bankerRound(product: BigInt, divisor: Long): Long =
    val d    = BigInt(divisor)
    val half = d / 2
    val raw  = product + half
    val quot = raw / d
    val rem  = raw % d
    if rem == 0 && product % 2 != 0 then (quot - 1).toLong
    else quot.toLong
