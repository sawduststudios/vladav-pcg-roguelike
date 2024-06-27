package cz.cuni.gamedev.nail123.roguelike.entities.enemies

import cz.cuni.gamedev.nail123.roguelike.entities.attributes.HasVision
import cz.cuni.gamedev.nail123.roguelike.mechanics.Vision
import cz.cuni.gamedev.nail123.roguelike.mechanics.goSmartlyTowards
import cz.cuni.gamedev.nail123.roguelike.tiles.GameTiles

class Orc(overPowered: Boolean): Enemy(GameTiles.ORC), HasVision {
    constructor(): this(false)

    override val blocksVision = true
    override var maxHitpoints = 15
    override var hitpoints = 15
    override var attack = 6
    override var defense = 1

    override var visionRadius = 4

    init {
        if (overPowered) {
            maxHitpoints = 30
            hitpoints = 30
            attack = 6
            defense = 2
            visionRadius = 7
        } else {
            maxHitpoints = 15
            hitpoints = 15
            attack = 6
            defense = 1
            visionRadius = 4
        }
    }

    var hasSeenPlayer = false

    override fun update() {
        // Get the player position
        val playerPosition = area.player.position
        // Use the Vision mechanic to get visible positions
        val canSeePlayer = playerPosition in Vision.getVisiblePositionsFrom(area, position, visionRadius)
        // If he sees player, he will start navigating toward him and never loses track
        if (canSeePlayer) hasSeenPlayer = true
        if (hasSeenPlayer) {
            goSmartlyTowards(playerPosition)
        }
    }
}