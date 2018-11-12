import java.io.File
import kotlin.math.abs

fun main(args: Array<String>) {
    val dir = "H:\\Toothpaste"
    val source = "H:\\Toothpaste\\Processors.txt"
    val set1 = File(source).readLines()
            .filter(String::isNotBlank).map(String::loadTriple).map { it.second }.toHashSet()
    val set2 = File(dir).listFiles().filter(File::isFile).map(File::getName)
            .filter { it.startsWith('(') }
            .map(String::loadTriple).map { it.second }.toHashSet()
    if (abs(set1.size - set2.size) != 0)
        System.err.println(set1.minus(set2))
    else
        println("Check okay!")
}