package com.mmutert.trackmydebt

enum class TransactionAction(val labelId: Int) {

    // Other Person - Money -> User
    /** Physical exchange of money from another person to the user
     * Equivalent action to [LENT_TO_USER].
     */
    MONEY_TO_USER(R.string.transaction_state_money_to_user),

    /**
     * User bought something for another person.
     * This is equal to receiving money as the user ([TransactionAction.MONEY_TO_USER]),
     *  since this will have to be paid back later.
     * It can also be seen as the other person paying of a debt with the user by buying something
     * */
    LENT_TO_USER(R.string.transaction_state_lent_to_user),

    // User -> Money -> Other Person
    /** Physical exchange of money from the user to another person
     * Equivalent action to [LENT_BY_USER].
     */
    MONEY_FROM_USER(R.string.transaction_state_money_from_user),

    /** Another person bought something for the user
     * This is equal to giving someone else money as the user ([TransactionAction.MONEY_FROM_USER]),
     *  since this money will have to be paid back at a later time.
     * It can also be seen as paying a debt and thus as sending money to the other person.
     */
    LENT_BY_USER(R.string.transaction_state_lent_by_user),
}