package composite

import java.math.BigDecimal

interface MenuComponent {
    val name: String
    val description: String
    val price: BigDecimal get() = throw UnsupportedOperationException()

    fun print()

    fun add(component: MenuComponent): Unit = throw UnsupportedOperationException()
    fun remove(component: MenuComponent): Unit = throw UnsupportedOperationException()
}
