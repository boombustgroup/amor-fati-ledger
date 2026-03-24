# amor-fati-ledger-poc

Formally verified double-entry flow interpreter for Stock-Flow Consistent (SFC) agent-based models.

Every monetary flow is a debit/credit pair. The interpreter makes it **mathematically impossible** to produce unbalanced books — proved by [Stainless](https://epfl-lara.github.io/stainless/) + Z3 SMT solver. 16/16 verification conditions valid.

Built for [amor-fati](https://github.com/boombustgroup/amor-fati), a macroeconomic SFC-ABM simulation engine.

### Properties proved (16/16 valid)

| Property | What it guarantees | Proved by |
|---|---|---|
| **Flow conservation** | `balances(from) + balances(to)` unchanged after any flow | Z3 (pointwise) |
| **Frame condition** | All accounts not involved in the flow are untouched | Z3 (universal quantifier) |
| **Sequential application** | `applyFlowList` preserves conservation across any flow sequence | Z3 (structural induction) |
| **Distribution exactness** | `distribute(total, shares).sum == total` — no rounding loss | Z3 (residual plug) |
| **Commutativity** | Flows on disjoint accounts produce the same result in any order | Z3 (map equality) |

### Why pointwise, not global sum?

Global `balances.values.sum == const` requires induction lemmas over arbitrary-size maps, which can stall SMT solvers. Instead, we prove **pointwise conservation** (the two involved accounts sum to the same value) plus the **frame condition** (everything else unchanged). This implies global conservation and verifies in <1 second.

## Architecture

```
src/
  main/
    scala/                    # Production code (sbt compile + test)
      com/boombustgroup/ledger/
        Flow.scala            # Single flow: from, to, amount, mechanism
        BatchedFlow.scala     # Sealed trait: Scatter (N:M) + Broadcast (1:N)
        EntitySector.scala    # Population types: Households, Firms, Banks, ...
        AssetType.scala       # Balance types: DemandDeposit, FirmLoan, ...
        MechanismId.scala     # Opaque type for audit trail (generic, not model-specific)
        Interpreter.scala     # Pure functional interpreter (Map-based, verified)
        ImperativeInterpreter.scala  # Production interpreter (Array-based, fast)
        MutableWorldState.scala      # Array[Long] per (sector, asset) — DOD
        Distribute.scala      # Proportional distribution with residual plug
    scala-stainless/          # Verified core (Stainless standalone, not sbt)
      Verified.scala          # Post-conditions verified by Z3
  test/
    scala/
      com/boombustgroup/ledger/
        InterpreterSpec.scala         # Unit tests
        InterpreterPropertySpec.scala # ScalaCheck property-based tests
        DistributeSpec.scala          # Distribution exactness tests
        EquivalenceSpec.scala         # Pure == Imperative bit-for-bit proof
```

Three verification layers:
1. **Stainless + Z3** on `Verified.scala` — mathematical proof, runs in CI via `verify.yml`
2. **ScalaCheck** on `Interpreter.scala` — 100+ random scenarios per property, runs in CI via `ci.yml`
3. **Equivalence test** on `ImperativeInterpreter.scala` — proves Array-based shell == Map-based core bit-for-bit

### Chain of trust

```
Stainless/Z3 proves → Verified.scala (pure Map)
EquivalenceSpec proves → ImperativeInterpreter == Verified (bit-for-bit)
Therefore → ImperativeInterpreter is correct
```

## Run

```bash
# Tests (24 tests, property-based + equivalence)
sbt test

# Formal verification (requires Stainless standalone + Z3)
./verify.sh
```

## Stack

- **Scala 3.7** (pinned to Stainless 0.9.9.2 bundled compiler)
- **Stainless** (EPFL) — formal verification for Scala, powered by Z3
- **Z3** (Microsoft Research) — SMT solver
- **ScalaCheck** — property-based testing
- **Long-based arithmetic** — all amounts are `Long` (scale 10^4), addition is exact

## Part of the amor-fati ecosystem

- [amor-fati](https://github.com/boombustgroup/amor-fati) — macroeconomic SFC-ABM simulation engine
- **amor-fati-ledger-poc** — this repo (verified flow interpreter)

## License

Apache 2.0 — Copyright 2026 [BoomBustGroup](https://www.boombustgroup.com/)
