package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.min
import kotlin.math.pow

fun main(args: Array<String>) {
    var inputFile = ""
    var outputFile = ""
    var inputWidth: Int = -1
    var inputHeight: Int = -1
    for (i in args.indices) {
        if (args[i] == "-in") {
            inputFile = args[i + 1]
            continue
        } else if (args[i] == "-out") {
            outputFile = args[i + 1]
            continue
        } else if (args[i] == "-width") {
            inputWidth = args[i + 1].toInt()
            continue
        } else if (args[i] == "-height") {
            inputHeight = args[i + 1].toInt()
            continue
        }
    }
    var bufferedImage = ImageIO.read(File(inputFile))
    
    repeat(inputWidth) {
        bufferedImage = iterateOnce(bufferedImage)
    }
    repeat(inputHeight) {
        bufferedImage = iterateOnce(bufferedImage, isHorizontal=true)
    }

    ImageIO.write(bufferedImage, "png", File(outputFile))
}

fun iterateOnce(bufferedImage: BufferedImage, isHorizontal: Boolean = false): BufferedImage {
    val energyValues = getEnergyMatrix(bufferedImage)
    val pathValues: Array<Array<Double>>
    pathValues = if (isHorizontal) {
        getPathsMatrix(transposeMatrix(energyValues))
    } else {
        getPathsMatrix(energyValues)
    }

    val lastPixelXCoord = findLastPixelXCoord(pathValues)
    val height = pathValues.size
    val seamCoords = IntArray(height)
    findSeam(pathValues, seamCoords, lastPixelXCoord, height - 1, isHorizontal)

    return removeSeam(bufferedImage, seamCoords, isHorizontal)
}

fun removeSeam(bufferedImage: BufferedImage, seamCoords: IntArray, isHorizontal: Boolean = false): BufferedImage {
    val newImage: BufferedImage
    if (isHorizontal) {
        newImage = BufferedImage(bufferedImage.width, bufferedImage.height - 1, BufferedImage.TYPE_INT_RGB)
       for (x in 0 until newImage.width) {
           var skipValue = 0
           for (y in 0 until newImage.height) {
               if (y == seamCoords[x]) {
                   skipValue = 1
               }
               newImage.setRGB(x, y, bufferedImage.getRGB(x, y + skipValue))
           }
       }
    } else {
        newImage = BufferedImage(bufferedImage.width - 1, bufferedImage.height, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until newImage.height) {
            var skipValue = 0
            for (x in 0 until newImage.width) {
                if (x == seamCoords[y]) {
                    skipValue = 1
                }
                newImage.setRGB(x, y, bufferedImage.getRGB(x + skipValue, y))
            }
        }
    }
    return newImage
}

fun findLastPixelXCoord(pathValues: Array<Array<Double>>): Int {
    val width = pathValues[0].size
    val height = pathValues.size
    var lastMinPixelValue: Double = Double.MAX_VALUE
    var xCoord: Int = -1

    for (x in 0 until width) {
        if (pathValues[height - 1][x] < lastMinPixelValue) {
            lastMinPixelValue = pathValues[height - 1][x]
            xCoord = x
        }
    }

    return xCoord
}

fun getPathsMatrix(energyMatrix: Array<Array<Double>>): Array<Array<Double>> {
    var pathMatrix = arrayOf<Array<Double>>()
    val width = energyMatrix[0].size
    val height = energyMatrix.size

    var array = arrayOf<Double>()
    for (x in 0 until width) {
        array += energyMatrix[0][x]
    }
    pathMatrix += array

    for (y in 1 until height) {
        var rowPathValues = arrayOf<Double>()
        for (x in 0 until width) {
            val min = when (x) {
                0 -> {
                    min(pathMatrix[y - 1][x], pathMatrix[y - 1][x + 1])
                }
                width - 1 -> {
                    min(pathMatrix[y - 1][x - 1], pathMatrix[y - 1][x])
                }
                else -> {
                    min(min(pathMatrix[y - 1][x - 1], pathMatrix[y - 1][x]), pathMatrix[y - 1][x + 1])
                }
            }
            rowPathValues += (energyMatrix[y][x] + min)
        }
        pathMatrix += rowPathValues
    }

    return pathMatrix
}

fun transposeMatrix(matrix: Array<Array<Double>>): Array<Array<Double>> {
    val width = matrix[0].size
    val height = matrix.size

    var newMatrix = arrayOf<Array<Double>>()
    for (x in 0 until width) {
        var array = arrayOf<Double>()
        for (y in 0 until height) {
            array += matrix[y][x]
        }
        newMatrix += array
    }

    return newMatrix
}

fun getEnergyMatrix(bufferedImage: BufferedImage): Array<Array<Double>> {
    var energyValues = arrayOf<Array<Double>>()

    for (y in 0 until bufferedImage.height) {
        var array = arrayOf<Double>()
        for (x in 0 until bufferedImage.width) {
            array += getEnergy(bufferedImage, x, y)
        }
        energyValues += array
    }

    return energyValues
}

fun findSeam(pathValues: Array<Array<Double>>, seamCoords: IntArray, x: Int, y: Int, isHorizontal: Boolean = false) {
    seamCoords[y] = x
    if (y == 0) {
        return
    }

    var startX = -1
    var endX = 1
    val width = pathValues[0].size
    if (x == 0) {
        startX = 0
    } else if (x == width - 1) {
        endX = 0
    }

    var minValue: Double = Double.MAX_VALUE
    var minXCoord: Int = -1
    for (i in startX..endX) {
        if (pathValues[y - 1][x + i] < minValue) {
            minValue = pathValues[y - 1][x + i]
            minXCoord = x + i
        }
    }

    findSeam(pathValues, seamCoords, minXCoord, y - 1, isHorizontal)
}

fun getEnergy(bufferedImage: BufferedImage, x: Int, y: Int): Double {
    var xCoordForDeltaX = x
    var yCoordForDeltay = y

    if (x == 0) {
        xCoordForDeltaX = 1
    } else if (x == bufferedImage.width - 1) {
        xCoordForDeltaX = bufferedImage.width - 2
    }

    if (y == 0) {
        yCoordForDeltay = 1
    } else if (y == bufferedImage.height - 1) {
        yCoordForDeltay = bufferedImage.height - 2
    }

    val leftPixel = Color(bufferedImage.getRGB(xCoordForDeltaX - 1, y), true)
    val rightPixel = Color(bufferedImage.getRGB(xCoordForDeltaX + 1, y), true)
    val lowerPixel = Color(bufferedImage.getRGB(x, yCoordForDeltay + 1), true)
    val upperPixel = Color(bufferedImage.getRGB(x, yCoordForDeltay - 1), true)

    val deltaX = ((leftPixel.green - rightPixel.green).toDouble()).pow(2.0) +
                 ((leftPixel.blue - rightPixel.blue).toDouble()).pow(2.0) +
                 ((leftPixel.red - rightPixel.red).toDouble()).pow(2.0)

    val deltaY = ((upperPixel.green - lowerPixel.green).toDouble()).pow(2.0) +
                 ((upperPixel.blue - lowerPixel.blue).toDouble()).pow(2.0) +
                 ((upperPixel.red - lowerPixel.red).toDouble()).pow(2.0)

    return (deltaX + deltaY).pow(0.5)
}
