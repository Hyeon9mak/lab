package state.after

sealed class AfterInit(val cards: Cards) : State {

    override fun cards(): Cards = cards

    override fun score(): Score = cards.score()
}
