package cz.cuni.gamedev.nail123.roguelike.mechanics

import cz.cuni.gamedev.nail123.roguelike.entities.Player
import cz.cuni.gamedev.nail123.roguelike.entities.attributes.HasCombatStats
import cz.cuni.gamedev.nail123.roguelike.events.logMessage
import java.util.Random
import kotlin.math.max

object Combat {
    /**
     * Very basic combat dealing damage equal to difference between attacker's attack and defender's defense.
     * Meant to be expanded.
     */
    var wasCritical: Boolean = false
    var wasParry: Boolean = false
    val rng = Random()
    fun attack(attacker: HasCombatStats, defender: HasCombatStats) {
        val critChance = 0.1
        val parryChance = 0.1
        wasCritical = false
        wasParry = false
        var damage = max(attacker.attack - defender.defense, 0)
        if (attacker is Player && rng.nextFloat() < critChance) {
            damage *= 2
            wasCritical = true
        }
        if (defender is Player && rng.nextFloat() < parryChance) {
            damage = 0
            wasParry = true
        }
        defender.takeDamage(damage)
        if (defender is Player && wasParry && damage == 0) {
            when {
                attacker is Player -> this.logMessage("You hit $defender for $damage damage!")
                defender is Player -> this.logMessage("PARRY! You take 0 damage!")
                else -> this.logMessage("$attacker hits $defender for $damage damage!")
            }
        }
        if (!wasCritical) {
            when {
                attacker is Player -> this.logMessage("You hit $defender for $damage damage!")
                defender is Player -> this.logMessage("$attacker hits you for $damage damage!")
                else -> this.logMessage("$attacker hits $defender for $damage damage!")
            }
        }
        else {
            when {
                attacker is Player -> this.logMessage("CRITICAL HIT! You hit $defender for $damage damage!")
                defender is Player -> this.logMessage("CRITICAL HIT! $attacker hits you for $damage damage!")
                else -> this.logMessage("CRITICAL HIT! $attacker hits $defender for $damage damage!")
            }
        }

    }
}