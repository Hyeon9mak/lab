package state.after

import state.card.Card

interface State {

    fun receiveCard(card: Card): State

    fun stay(): State

    fun isFinished(): Boolean

    fun cards(): Cards

    fun score(): Score

    fun judgementGameResult(otherScore: Score): GameResult
}
