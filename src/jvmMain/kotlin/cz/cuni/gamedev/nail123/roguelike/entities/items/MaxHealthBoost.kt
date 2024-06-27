package cz.cuni.gamedev.nail123.roguelike.entities.items

import cz.cuni.gamedev.nail123.roguelike.entities.Player
import cz.cuni.gamedev.nail123.roguelike.entities.attributes.HasInventory
import cz.cuni.gamedev.nail123.roguelike.entities.attributes.Inventory
import cz.cuni.gamedev.nail123.roguelike.tiles.GameTiles

class MaxHealthBoost(val healthIncrease: Int) : Item(GameTiles.MAX_HEALTH, true, true, "You feel vital! Your max health increased by $healthIncrease.") {
    override fun isEquipable(character: HasInventory): Inventory.EquipResult {
        return Inventory.EquipResult(true, "")
    }

    override fun onEquip(character: HasInventory) {
        if (character is Player) {
            character.maxHitpoints += healthIncrease
        }
    }

    override fun onUnequip(character: HasInventory) {
        return
    }

    override fun toString(): String {
        return "Health increase($healthIncrease)"
    }
}