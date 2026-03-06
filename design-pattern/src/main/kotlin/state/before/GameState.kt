package state.before

enum class GameState {
    INIT,    // 초기 상태 (카드 2장 받기 전)
    HIT,     // 게임 진행 중
    STAY,    // 플레이어가 stay 선택
    BUST,    // 21 초과
    BLACKJACK // 초기 21
}
