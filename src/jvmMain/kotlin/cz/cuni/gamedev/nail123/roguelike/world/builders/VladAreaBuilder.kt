package cz.cuni.gamedev.nail123.roguelike.world.builders

import cz.cuni.gamedev.nail123.roguelike.GameConfig
import cz.cuni.gamedev.nail123.roguelike.blocks.Floor
import cz.cuni.gamedev.nail123.roguelike.blocks.GameBlock
import cz.cuni.gamedev.nail123.roguelike.blocks.Wall
import cz.cuni.gamedev.nail123.utils.collections.ObservableMap
import org.hexworks.zircon.api.data.Position3D
import org.hexworks.zircon.api.data.Size3D
import kotlin.random.Random

class VladAreaBuilder(size: Size3D = GameConfig.AREA_SIZE,
                       visibleSize: Size3D = GameConfig.VISIBLE_SIZE): AreaBuilder(size, visibleSize) {

    open class Room(
        protected var leftR: Int,
        protected var rightR: Int,
        protected var topR: Int,
        protected var bottomR: Int
    ) {
        protected fun getWidth(): Int {
            return rightR - leftR + 1
        }
        protected fun getHeight(): Int {
            return bottomR - topR + 1
        }
        fun getLeft(): Int {
            return leftR
        }
        fun getRight(): Int {
            return rightR
        }
        fun getTop(): Int {
            return topR
        }
        fun getBottom(): Int {
            return bottomR
        }
        open fun draw(blocks: ObservableMap<Position3D, GameBlock>){
            val width = getWidth()
            val height = getHeight()
            for (x in 0 until width) {
                for (y in 0 until height) {
                    val isBorder = x == 0 || x == width - 1 || y == 0 || y == height - 1
                    blocks[Position3D.create(x + leftR, y + topR, 0)] = if (isBorder) Wall() else Floor()
                }
            }
        }
    }
    class BinaryRoom(
        left: Int,
        right: Int,
        top: Int,
        bottom: Int
    ) : Room(left, right, top, bottom) {

        private var horizontalSplit: Boolean = false
        private var verticalSplit: Boolean = false
        private var trimmed: Boolean = false

        private var leftRoom: BinaryRoom? = null
        private var rightRoom: BinaryRoom? = null

        init {
            if (getHeight() < MIN_HEIGHT || getWidth() < MIN_WIDTH) {
                println("Error: Small room: " + getWidth() + "x" + getHeight())
            }
        }

        fun isLeaf(): Boolean {
            if (getWidth() / 2 >= MIN_WIDTH || getHeight() / 2 >= MIN_HEIGHT)
            {
                split()
            }
            return !horizontalSplit && !verticalSplit
        }
        fun split() {
            // Attempt random split
            val rand = Math.random()
            if (rand < 0.5 && getWidth() >= 2 * MIN_WIDTH) {
                verticalSplit()
                return
            } else if (getHeight() >= 2 * MIN_HEIGHT) {
                horizontalSplit()
                return
            }

            // Force split if there's too much space.
            if (getWidth() > MAX_WIDTH) {
                verticalSplit()
                return
            }

            if (getHeight() > MAX_HEIGHT) {
                horizontalSplit()
                return
            }
        }
        override fun draw(blocks: ObservableMap<Position3D, GameBlock>) {
            var isLeaf = isLeaf()
            trim()
            if (isLeaf) {
                super.draw(blocks)
            } else {
                leftRoom?.draw(blocks)
                rightRoom?.draw(blocks)
            }
        }
        private fun verticalSplit() {
            verticalSplit = true

            // Calculate the midpoint and introduce a small random offset
            val minSplit = leftR + MIN_WIDTH
            val maxSplit = rightR - MIN_WIDTH
            val splitRange = maxSplit - minSplit
            val splitX = if (splitRange > 0) {
                minSplit + (Math.random() * splitRange).toInt()
            } else {
                (leftR + rightR) / 2
            }

            leftRoom = BinaryRoom(leftR, splitX, topR, bottomR)
            rightRoom = BinaryRoom(splitX + 1, rightR, topR, bottomR)
        }

        private fun horizontalSplit() {
            horizontalSplit = true

            // Calculate the midpoint and introduce a small random offset
            val minSplit = topR + MIN_HEIGHT
            val maxSplit = bottomR - MIN_HEIGHT
            val splitRange = maxSplit - minSplit
            val splitY = if (splitRange > 0) {
                minSplit + (Math.random() * splitRange).toInt()
            } else {
                (topR + bottomR) / 2
            }

            leftRoom = BinaryRoom(leftR, rightR, topR, splitY)
            rightRoom = BinaryRoom(leftR, rightR, splitY + 1, bottomR)
        }

        fun trim() {
            if (trimmed)
                return
            trimmed = true

            val horizontalTrim = Random.nextInt(MIN_TRIM, MAX_TRIM + 1)
            val verticalTrim = Random.nextInt(MIN_TRIM, MAX_TRIM + 1)

            leftR += horizontalTrim
            rightR -= horizontalTrim
            topR += verticalTrim
            bottomR -= verticalTrim
        }

        fun getTopConnections(): List<Int> {
            val connections = mutableListOf<Int>()

            if (!isLeaf()) {
                rightRoom?.let { connections.addAll(it.getTopConnections()) }
                leftRoom?.let { connections.addAll(it.getTopConnections()) }
            } else {
                for (x in leftR + CORRIDOR_MARGIN..rightR - CORRIDOR_MARGIN) {
                    connections.add(x)
                }
            }

            return connections
        }
        fun getBottomConnections(): List<Int> {
            val connections = mutableListOf<Int>()

            if (!isLeaf()) {
                rightRoom?.let { connections.addAll(it.getBottomConnections()) }
                leftRoom?.let { connections.addAll(it.getBottomConnections()) }
            } else {
                for (x in leftR + CORRIDOR_MARGIN..rightR - CORRIDOR_MARGIN) {
                    connections.add(x)
                }
            }

            return connections
        }
        fun getLeftConnections(): List<Int> {
            val connections = mutableListOf<Int>()

            if (!isLeaf()) {
                leftRoom?.let { connections.addAll(it.getLeftConnections()) }
                if (verticalSplit && rightRoom != null)
                    rightRoom?.let { connections.addAll(it.getLeftConnections()) }
            } else {
                for (y in topR + CORRIDOR_MARGIN..bottomR - CORRIDOR_MARGIN) {
                    connections.add(y)
                }
            }

            return connections
        }
        fun getRightConnections(): List<Int> {
            val connections = mutableListOf<Int>()

            if (!isLeaf()) {
                rightRoom?.let { connections.addAll(it.getRightConnections()) }
                if (horizontalSplit && leftRoom != null)
                    leftRoom?.let { connections.addAll(it.getRightConnections()) }
            } else {
                for (y in topR + CORRIDOR_MARGIN..bottomR - CORRIDOR_MARGIN) {
                    connections.add(y)
                }
            }

            return connections
        }

        fun getIntersectionGroups(points: List<Int>): List<Pair<Int, Int>> {
            val groups = mutableListOf<Pair<Int, Int>>()

            var firstTime = true
            var currentGroup = Pair(0, 0)
            for (i in points.indices) {
                val num = points[i]

                if (firstTime || points.getOrElse(i - 1) { -1 } != num - 1) {
                    if (!firstTime) {
                        groups.add(currentGroup)
                    }

                    firstTime = false
                    currentGroup = Pair(num, num)
                } else {
                    currentGroup = Pair(currentGroup.first, currentGroup.second + 1)
                }
            }

            if (!firstTime) {
                groups.add(currentGroup)
            }

            return groups.filter { it.second - it.first >= MIN_CORRIDOR_THICKNESS }
        }

        companion object {
            private const val MIN_WIDTH = 10
            private const val MAX_WIDTH = 22
            private const val MIN_HEIGHT = 10
            private const val MAX_HEIGHT = 22

            private const val MIN_TRIM = 1
            private const val MAX_TRIM = 2
            private const val CORRIDOR_MARGIN = 1
            private const val MIN_CORRIDOR_THICKNESS = 2
        }
    }



    override fun create(): AreaBuilder {
        // create a binary room with width 62 and height 42
        val binaryRoom = BinaryRoom(0, 61, 0, 41)
        // split the room
        //binaryRoom.split()
        // call its draw method and pass in blocks
        binaryRoom.draw(blocks)

        return this
    }
}