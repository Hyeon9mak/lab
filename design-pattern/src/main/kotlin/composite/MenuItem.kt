package composite

import java.math.BigDecimal

class MenuItem(
    override val name: String,
    override val description: String,
    override val price: BigDecimal,
) : MenuComponent {

    override fun print() {
        // ...
    }
}
