package state.before

import state.card.Card
import state.card.CardNumber

class Player {
    private val cards: MutableList<Card> = mutableListOf()
    private var gameState: GameState = GameState.INIT

    fun receiveCard(card: Card) {
        when (gameState) {
            GameState.STAY, GameState.BUST, GameState.BLACKJACK ->
                throw IllegalStateException("턴이 종료되어 카드를 받을 수 없습니다.")
            GameState.INIT -> {
                cards.add(card)
                if (cards.size == 2) {
                    gameState = if (score() == 21) GameState.BLACKJACK else GameState.HIT
                }
            }
            GameState.HIT -> {
                cards.add(card)
                if (score() > 21) gameState = GameState.BUST
            }
        }
    }

    fun stay() {
        when (gameState) {
            GameState.HIT -> gameState = GameState.STAY
            GameState.INIT -> throw IllegalStateException("초기 상태에서는 stay 할 수 없습니다.")
            GameState.STAY, GameState.BUST, GameState.BLACKJACK ->
                throw IllegalStateException("턴이 종료되어 stay 상태로 변경할 수 없습니다.")
        }
    }

    fun isFinished(): Boolean = when (gameState) {
        GameState.STAY, GameState.BUST, GameState.BLACKJACK -> true
        GameState.INIT, GameState.HIT -> false
    }

    fun cards(): List<Card> = cards.toList()

    fun score(): Int {
        val baseScore = cards.sumOf { it.number.score }
        val countOfAce = cards.count { it.number == CardNumber.ACE }
        var score = baseScore
        repeat(countOfAce) {
            if (score + 10 <= 21) score += 10
        }
        return score
    }

    fun judgementGameResult(other: Player): GameResult {
        return when (gameState) {
            GameState.BLACKJACK -> {
                if (other.gameState == GameState.BLACKJACK) GameResult.DRAW
                else GameResult.BLACKJACK_WIN
            }
            GameState.BUST -> GameResult.LOSE
            GameState.STAY -> when {
                other.gameState == GameState.BLACKJACK -> GameResult.LOSE
                other.gameState == GameState.BUST -> GameResult.WIN
                score() > other.score() -> GameResult.WIN
                score() < other.score() -> GameResult.LOSE
                else -> GameResult.DRAW
            }
            GameState.INIT, GameState.HIT ->
                throw IllegalStateException("게임이 진행중인 상태에서는 승패 비교를 할 수 없습니다.")
        }
    }
}
