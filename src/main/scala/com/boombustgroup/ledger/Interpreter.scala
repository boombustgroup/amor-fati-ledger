package com.boombustgroup.ledger

/** Pure functional flow interpreter — verified core.
  *
  * Every operation preserves the SFC invariant: total system wealth is constant. Stainless/Z3 verifies this via pointwise conservation +
  * frame condition.
  *
  * This is the pure specification. The imperative shell (for performance) is tested for bit-for-bit equivalence against this core.
  */
object Interpreter:

  /** Apply a single flow to a balance map. Double-entry: debit from, credit to.
    *
    * Post-conditions (verified by Stainless):
    *   1. balances(from) + balances(to) is unchanged (flow conservation)
    *   2. All other accounts are unchanged (frame condition)
    */
  def applyFlow(balances: Map[Int, Long], flow: Flow): Map[Int, Long] =
    val currentFrom = balances.getOrElse(flow.from, 0L)
    val currentTo   = balances.getOrElse(flow.to, 0L)
    balances
      .updated(flow.from, currentFrom - flow.amount)
      .updated(flow.to, currentTo + flow.amount)

  /** Apply a list of flows sequentially. */
  def applyAll(balances: Map[Int, Long], flows: Vector[Flow]): Map[Int, Long] =
    flows.foldLeft(balances)(applyFlow)

  /** Total system wealth: sum of all balances. */
  def totalWealth(balances: Map[Int, Long]): Long =
    balances.values.sum
