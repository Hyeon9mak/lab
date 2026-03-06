package composite

class Menu(
    override val name: String,
    override val description: String,
) : MenuComponent {

    private val values = mutableListOf<MenuComponent>()

    override fun add(component: MenuComponent) { 
	    values.add(component) 
	}
    
    override fun remove(component: MenuComponent) { 
	    values.remove(component)
	}

    override fun print() {
        // ...
    }
}
