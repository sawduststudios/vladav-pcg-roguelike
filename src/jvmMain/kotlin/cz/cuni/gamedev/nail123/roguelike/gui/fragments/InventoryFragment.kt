package cz.cuni.gamedev.nail123.roguelike.gui.fragments

import cz.cuni.gamedev.nail123.roguelike.GameConfig
import cz.cuni.gamedev.nail123.roguelike.world.World
import org.hexworks.cobalt.databinding.api.extension.createPropertyFrom
import org.hexworks.zircon.api.Components
import org.hexworks.zircon.api.component.Fragment

class InventoryFragment(val world: World): Fragment {
    val width: Int = GameConfig.SIDEBAR_WIDTH - 2
    val inventoryBox = Components.paragraph()
            .withSize(width, 8)
            .build()

    val selectedIndexProperty = createPropertyFrom(-1)
    var selectedIndex
        get() = selectedIndexProperty.value
        set(value) { selectedIndexProperty.value = value }

    override val root = Components.vbox()
            .withSize(width, 10)
            .withPosition(0, 10)
            .withSpacing(0)
            .build().apply {
                addComponent(Components.header().withText("Inventory").build())
                addComponent(inventoryBox)
            }

    fun update() {
        val inventory = world.player.inventory
        if (inventory.items.isEmpty()) {
            inventoryBox.text = "Empty"
        } else {
            inventoryBox.text = inventory.items.mapIndexed { i, item ->
                val prefix = if (selectedIndex == i) ">" else ""
                val suffix = if (item.isEquipped) " E" else ""
                val line = "$prefix$item$suffix"
                // Pad until the end of the next line

                line.padEnd(nearestGreaterThanOrEqualMultiple(line.length, width - 1))
            }.joinToString("\n")
        }
    }

    fun nearestGreaterThanOrEqualMultiple(value: Int, mult: Int): Int
        = (value + (mult - 1)) / mult * mult
}