package state.after

class Blackjack(cards: Cards) : Finished(cards) {
    override fun judgementGameResult(otherScore: Score): GameResult {
        return if (otherScore.isBlackjack) {
            GameResult.DRAW
        } else {
            GameResult.BLACKJACK_WIN
        }
    }
}
