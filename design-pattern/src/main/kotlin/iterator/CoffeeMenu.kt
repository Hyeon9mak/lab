package iterator

class CoffeeMenu {
    val menuItems: List<MenuItem> = listOf(
        MenuItem(
            name = "에스프레소",
            description = "뒤지게 씀",
            price = 3_000.toBigDecimal(),
        ),
        MenuItem(
            name = "라떼",
            description = "는 말이야",
            price = 5_000.toBigDecimal(),
        ),
        MenuItem(
            name = "카푸치노",
            description = "거품이라는 뜻",
            price = 5_00.toBigDecimal()
        ),
        MenuItem(
            name = "모카",
            description = "멍멍",
            price = 1_000_000.toBigDecimal(),
        ),
    )
}
