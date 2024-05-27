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
    val random: Random = Random(4)
    open class Room(
        protected var leftR: Int,
        protected var rightR: Int,
        protected var topR: Int,
        protected var bottomR: Int,
        protected var blocks : ObservableMap<Position3D, GameBlock>,
        protected val random: Random
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
        open fun draw(){
            println("-----DRAWING ROOM")
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
        bottom: Int,
        blocks: ObservableMap<Position3D, GameBlock>,
        random: Random
    ) : Room(left, right, top, bottom, blocks, random) {

        private var horizontalSplit: Boolean = false
        private var verticalSplit: Boolean = false
        private var trimmed: Boolean = false

        private var leftRoom: BinaryRoom? = null
        private var rightRoom: BinaryRoom? = null

        init {
            if (getHeight() < MIN_HEIGHT || getWidth() < MIN_WIDTH) {
                println("Warning: Small room: " + getWidth() + "x" + getHeight())
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
            if (horizontalSplit || verticalSplit)
                return
            // Attempt random split
            val rand = random.nextFloat()
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
        override fun draw() {
            var isLeaf = isLeaf()
            trim()
            if (isLeaf) {
                super.draw()
            } else {
                leftRoom?.draw()
                rightRoom?.draw()
            }
        }
        private fun verticalSplit() {
            verticalSplit = true

            // Calculate the midpoint and introduce a small random offset
            val minSplit = leftR + MIN_WIDTH
            val maxSplit = rightR - MIN_WIDTH
            val splitRange = maxSplit - minSplit
            val splitX = if (splitRange > 0) {
                minSplit + (random.nextFloat() * splitRange).toInt()
            } else {
                (leftR + rightR) / 2
            }

            leftRoom = BinaryRoom(leftR, splitX, topR, bottomR, blocks, random)
            rightRoom = BinaryRoom(splitX + 1, rightR, topR, bottomR, blocks, random)
        }

        private fun horizontalSplit() {
            horizontalSplit = true

            // Calculate the midpoint and introduce a small random offset
            val minSplit = topR + MIN_HEIGHT
            val maxSplit = bottomR - MIN_HEIGHT
            val splitRange = maxSplit - minSplit
            val splitY = if (splitRange > 0) {
                minSplit + (random.nextFloat() * splitRange).toInt()
            } else {
                (topR + bottomR) / 2
            }

            leftRoom = BinaryRoom(leftR, rightR, topR, splitY, blocks, random)
            rightRoom = BinaryRoom(leftR, rightR, splitY + 1, bottomR, blocks, random)
        }

        fun trim() {
            if (trimmed)
                return
            trimmed = true

            val horizontalTrim = random.nextInt(MIN_TRIM, MAX_TRIM + 1)
            val verticalTrim = random.nextInt(MIN_TRIM, MAX_TRIM + 1)

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
                for (x in leftR + CORRIDOR_MARGIN + 1..rightR - CORRIDOR_MARGIN - 1) {
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
            var groups = mutableListOf<Pair<Int, Int>>()

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

            groups = groups.filter { it.second - it.first >= MIN_CORRIDOR_THICKNESS }.toMutableList()
            // sometimes the groups are longer than MAX_CORRIDOR_THICKNESS
            // if they are, replace it with a randomly chosen sub group of MAX_CORRIDOR_THICKNESS
            val updatedGroups = mutableListOf<Pair<Int, Int>>()
            for (group in groups) {
                if (group.second - group.first + 1 > MAX_CORRIDOR_THICKNESS) {
                    val start = group.first
                    val end = group.second
                    val rangeSize = end - start + 1

                    val maxStart = end - MAX_CORRIDOR_THICKNESS + 1
                    val randomStart = if (rangeSize > MAX_CORRIDOR_THICKNESS) {
                        random.nextInt(start, maxStart + 1)
                    } else {
                        start
                    }
                    val randomEnd = randomStart + MAX_CORRIDOR_THICKNESS - 1
                    updatedGroups.add(Pair(randomStart, randomEnd))
                } else {
                    updatedGroups.add(group)
                }
            }

            return updatedGroups
        }

        fun addCorridors() {
            if (isLeaf())
                return

            // TODO: check if no corridor exists and try again

            if (leftRoom != null && rightRoom != null) {
                if (verticalSplit) {
                    val positions = leftRoom!!.getRightConnections().intersect(rightRoom!!.getLeftConnections()).toList()
                    val groups = getIntersectionGroups(positions)
                    val p = groups.random()
                    drawCorridor(leftRoom!!.rightR, p.first,rightRoom!!.leftR, p.second, false)
                } else {
                    val positions = leftRoom!!.getBottomConnections().intersect(rightRoom!!.getTopConnections()).toList()
                    val groups = getIntersectionGroups(positions)
                    val p = groups.random()
                    drawCorridor(p.first, leftRoom!!.bottomR, p.second, rightRoom!!.topR,true)
                }
            }

            leftRoom?.addCorridors()
            rightRoom?.addCorridors()
        }
        fun drawCorridor(left: Int, top: Int, right: Int, bottom: Int, vertical: Boolean) {
            if (vertical) {
                // draw the left and right wall, everything else is floor
                for (y in top..bottom) {
                    for (x in left..right) {
                        val isWall = x == left || x == right
                        blocks[Position3D.create(x, y, 0)] = if (isWall) Wall() else Floor()
                    }
                }
                println("DRAWING VERTICAL CORRIDOR: " + left + "," + top + "," + right + "," + bottom)
            } else {
                // draw the top and bottom wall, everything else is floor
                for (x in left..right) {
                    for (y in top..bottom) {
                        val isWall = y == bottom || y == top
                        blocks[Position3D.create(x, y, 0)] = if (isWall) Wall() else Floor()
                    }
                }
                println("DRAWING HORIZONTAL CORRIDOR: " + left + "," + top + "," + right + "," + bottom)
            }

        }



        companion object {
            private const val MIN_WIDTH = 10
            private const val MAX_WIDTH = 22
            private const val MIN_HEIGHT = 10
            private const val MAX_HEIGHT = 22

            private const val MIN_TRIM = 2
            private const val MAX_TRIM = 2
            private const val CORRIDOR_MARGIN = 1
            private const val MIN_CORRIDOR_THICKNESS = 2
            private const val MAX_CORRIDOR_THICKNESS = 4
        }
    }



    override fun create(): AreaBuilder {
        // create a binary room with width 62 and height 42
        val binaryRoom = BinaryRoom(0, 61, 0, 41, blocks, random)
        // split the room
        //binaryRoom.split()
        // call its draw method and pass in blocks
        binaryRoom.draw()
        binaryRoom.addCorridors()

        return this
    }
}