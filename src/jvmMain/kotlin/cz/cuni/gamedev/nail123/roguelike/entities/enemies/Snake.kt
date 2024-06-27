package cz.cuni.gamedev.nail123.roguelike.entities.enemies

import cz.cuni.gamedev.nail123.roguelike.entities.attributes.HasSmell
import cz.cuni.gamedev.nail123.roguelike.entities.attributes.HasVision
import cz.cuni.gamedev.nail123.roguelike.mechanics.Pathfinding
import cz.cuni.gamedev.nail123.roguelike.mechanics.Vision
import cz.cuni.gamedev.nail123.roguelike.mechanics.goSmartlyTowards
import cz.cuni.gamedev.nail123.roguelike.tiles.GameTiles

class Snake(overPowered: Boolean): Enemy(GameTiles.SNAKE), HasSmell {
    constructor(): this(false)
    // It's up to you to decide whether you want Orcs to cast shadows
    override val blocksVision = false
    override var maxHitpoints = 6
    override var hitpoints = 3
    override var attack = 10
    override var defense = 0

    override val smellingRadius = 15

    init {
        if (overPowered) {
            maxHitpoints = 6
            hitpoints = 6
            attack = 20
            defense = 0
        } else {
            maxHitpoints = 6
            hitpoints = 3
            attack = 10
            defense = 0
        }
    }

    override fun update() {
        // Get the player position
        val playerPosition = area.player.position
        // Use the Vision mechanic to get visible positions
        if (Pathfinding.chebyshev(position, area.player.position) <= smellingRadius) {
            goSmartlyTowards(playerPosition)
            goSmartlyTowards(playerPosition)
        }
    }
}