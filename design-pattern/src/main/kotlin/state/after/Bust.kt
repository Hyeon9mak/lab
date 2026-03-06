package state.after

class Bust(cards: Cards) : Finished(cards) {
    override fun judgementGameResult(otherScore: Score): GameResult = GameResult.LOSE
}
