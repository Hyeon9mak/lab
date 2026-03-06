package composite

fun main() {
    val allMenus = Menu("전체 메뉴", "오늘의 메뉴판")

    val coffeeMenu = Menu("커피", "따뜻하고 차갑고")
    coffeeMenu.add(MenuItem("에스프레소", "뒤지게 씀", 3_000.toBigDecimal()))
    coffeeMenu.add(MenuItem("라떼", "는 말이야", 5_000.toBigDecimal()))
    coffeeMenu.add(MenuItem("카푸치노", "거품이라는 뜻", 500.toBigDecimal()))
    coffeeMenu.add(MenuItem("모카", "멍멍", 1_000_000.toBigDecimal()))

    val alcoholMenu = Menu("주류", "어른의 음료")
    alcoholMenu.add(MenuItem("소주", "한국의 전통 술", 6_000.toBigDecimal()))
    alcoholMenu.add(MenuItem("맥주", "시원한 탄산 음료", 8_000.toBigDecimal()))
    alcoholMenu.add(MenuItem("와인", "포도로 만든 술", 30_000.toBigDecimal()))
    alcoholMenu.add(MenuItem("위스키", "오크통 숙성 증류주", 300_000.toBigDecimal()))

    val beerMenuTap = Menu("생맥주 종류", "탭 리스트")
    beerMenuTap.add(MenuItem("카스", "국민 맥주", 5_000.toBigDecimal()))
    beerMenuTap.add(MenuItem("기네스", "흑맥주 대장", 9_000.toBigDecimal()))

    alcoholMenu.add(beerMenuTap)

    allMenus.add(coffeeMenu)
    allMenus.add(alcoholMenu)

    allMenus.print()
}
