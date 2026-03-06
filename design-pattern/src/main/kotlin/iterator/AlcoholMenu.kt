package iterator

class AlcoholMenu {
    val menuItems: Array<MenuItem> = arrayOf(
        MenuItem(
            name = "소주",
            description = "한국의 전통 술",
            price = 6_000.toBigDecimal(),
        ),
        MenuItem(
            name = "맥주",
            description = "시원한 탄산 음료",
            price = 8_000.toBigDecimal(),
        ),
        MenuItem(
            name = "와인",
            description = "포도로 만든 술",
            price = 30_000.toBigDecimal(),
        ),
        MenuItem(
            name = "위스키",
            description = "오크통에서 숙성된 증류주",
            price = 300_000.toBigDecimal(),
        ),
    )
}
