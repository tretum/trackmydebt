package com.mmutert.trackmydebt.util

import com.mmutert.trackmydebt.TransactionAction
import com.mmutert.trackmydebt.data.Transaction
import java.math.BigDecimal

fun List<Transaction>.balance(): BigDecimal {
    return this.sumOf { t ->
        // Money to other person = More credit = Positive action
        // Value from other person = More debt = Negative action
        when (t.action) {
            TransactionAction.MONEY_TO_USER -> t.amount.negate()
            TransactionAction.LENT_TO_USER -> t.amount.negate()
            TransactionAction.MONEY_FROM_USER -> t.amount
            TransactionAction.LENT_BY_USER -> t.amount
        }
    }
}