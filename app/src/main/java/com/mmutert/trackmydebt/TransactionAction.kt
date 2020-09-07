package com.mmutert.trackmydebt

enum class TransactionAction(val labelId: Int) {
    /** Physical exchange of money from another person to the user */

    MONEY_TO_USER(R.string.transaction_state_money_to_user),

    /** Physical exchange of money from the user to another person */
    MONEY_FROM_USER(R.string.transaction_state_money_from_user),

    /** Another person bought something for the user */
    LENT_TO_USER(R.string.transaction_state_lent_to_user),

    /** User bought something for another person */
    LENT_FROM_USER(R.string.transaction_state_lent_from_user)
}