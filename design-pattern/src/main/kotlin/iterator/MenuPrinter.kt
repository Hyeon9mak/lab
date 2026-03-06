package iterator

/**
 * Kotlin 의 위대함을 맛보아라.
 */
fun `인덱스 활용`() {
    val coffeeMenu = CoffeeMenu()
    val alcoholMenu = AlcoholMenu()

    println("커피 메뉴")
    for (i in coffeeMenu.menuItems.indices) {
        val menuItem = coffeeMenu.menuItems[i]
        println("${menuItem.name} - ${menuItem.description}: ${menuItem.price}")
    }

    println("술 메뉴")
    for (i in alcoholMenu.menuItems.indices) {
        val menuItem = alcoholMenu.menuItems[i]
        println("${menuItem.name} - ${menuItem.description}: ${menuItem.price}")
    }
}

fun `iterator 활용`() {
    val coffeeMenu = CoffeeMenu()
    val alcoholMenu = AlcoholMenu()

    println("커피 메뉴")
    for (menuItem in coffeeMenu.menuItems) {
        println("${menuItem.name} - ${menuItem.description}: ${menuItem.price}")
    }

    println("술 메뉴")
    for (menuItem in alcoholMenu.menuItems) {
        println("${menuItem.name} - ${menuItem.description}: ${menuItem.price}")
    }
}
