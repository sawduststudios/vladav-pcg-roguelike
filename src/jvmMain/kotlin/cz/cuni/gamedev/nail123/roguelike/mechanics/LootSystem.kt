package cz.cuni.gamedev.nail123.roguelike.mechanics

import cz.cuni.gamedev.nail123.roguelike.entities.enemies.Enemy
import cz.cuni.gamedev.nail123.roguelike.entities.enemies.Orc
import cz.cuni.gamedev.nail123.roguelike.entities.enemies.Rat
import cz.cuni.gamedev.nail123.roguelike.entities.items.Item
import cz.cuni.gamedev.nail123.roguelike.entities.items.Sword
import kotlin.random.Random

object LootSystem {
    interface ItemDrop {
        fun getDrops() = listOf<Item>()
    }
    object NoDrop : ItemDrop {
        override fun getDrops() = listOf<Item>()
    }
    class SingleDrop(val instantioator: () -> Item) : ItemDrop {
        override fun getDrops() = listOf(instantioator())
    }
    class TreasureClass(val numDrops: Int, val possibleDrops: List<Pair<Int, ItemDrop>>) : ItemDrop {
        val totalProb = possibleDrops.map {it.first}.sum()

        override fun getDrops(): List<Item> {
            val drops = ArrayList<Item>()
            repeat(numDrops) {
                drops.addAll(pickDrop().getDrops())
            }
            return drops
        }

        private fun pickDrop(): ItemDrop {
            var randNumber = Random.Default.nextInt(totalProb)
            for (drop in possibleDrops) {
                randNumber -= drop.first
                if (randNumber < 0) return drop.second
            }
            // Never happens, but we need to place something here anyway
            return possibleDrops.last().second
        }
    }


    val rng = Random.Default
    // power 2-4
    val basicSword = SingleDrop { Sword(rng.nextInt(3) + 2)}
    // power 5-6
    val rareSword = SingleDrop { Sword(rng.nextInt(2) + 5)}

    val enemyDrops = mapOf(
        Rat::class to TreasureClass(1, listOf(
            2 to NoDrop,
            1 to basicSword
        )),
        Orc::class to TreasureClass(1, listOf(
            4 to NoDrop,
            2 to basicSword,
            1 to rareSword
        ))
    )

    fun onDeath(enemy: Enemy) {
        val drops = enemyDrops[enemy::class]?.getDrops() ?: return
        for (item in drops) {
            enemy.area[enemy.position]?.entities?.add(item)
        }
    }


//    val basicDrop = TreasureClass(1, listOf(
//        2 to NoDrop,
//        1 to basicSword
//    ))
//    val enemyDrops = mapOf(
//        Rat::class to basicDrop,
//        Orc::class to TreasureClass(1, listOf(
//            6 to basicDrop,
//            1 to rareSword
//        ))
//    )
}