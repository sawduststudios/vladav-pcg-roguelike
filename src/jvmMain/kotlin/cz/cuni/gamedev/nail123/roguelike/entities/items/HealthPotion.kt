package cz.cuni.gamedev.nail123.roguelike.entities.items

import cz.cuni.gamedev.nail123.roguelike.entities.Player
import cz.cuni.gamedev.nail123.roguelike.entities.attributes.HasInventory
import cz.cuni.gamedev.nail123.roguelike.entities.attributes.Inventory
import cz.cuni.gamedev.nail123.roguelike.tiles.GameTiles

class HealthPotion(val healingPower: Int) : Item(GameTiles.HEALTH_POTION, false, true, "You drank a delicious potion. It gave you $healingPower health.") {
    override fun isEquipable(character: HasInventory): Inventory.EquipResult {
        return Inventory.EquipResult(true, "")
    }

    override fun onEquip(character: HasInventory) {
        if (character is Player) {
            character.hitpoints += healingPower
            if (character.hitpoints > character.maxHitpoints)
                character.hitpoints = character.maxHitpoints
        }
    }

    override fun onUnequip(character: HasInventory) {
        return
    }

    override fun toString(): String {
        return "Health Potion($healingPower)"
    }
}