package cz.cuni.gamedev.nail123.roguelike.entities.items

import cz.cuni.gamedev.nail123.roguelike.entities.Player
import cz.cuni.gamedev.nail123.roguelike.entities.attributes.HasInventory
import cz.cuni.gamedev.nail123.roguelike.entities.attributes.Inventory
import cz.cuni.gamedev.nail123.roguelike.tiles.GameTiles

class MaxArmorBoost(val armorIncrease: Int) : Item(GameTiles.MAX_ARMOR, true, true, "You feel invincible! Your defence increased by $armorIncrease.") {
    override fun isEquipable(character: HasInventory): Inventory.EquipResult {
        return Inventory.EquipResult(true, "")
    }

    override fun onEquip(character: HasInventory) {
        if (character is Player) {
            character.defense += armorIncrease
        }
    }

    override fun onUnequip(character: HasInventory) {
        return
    }

    override fun toString(): String {
        return "Defence increase($armorIncrease)"
    }
}