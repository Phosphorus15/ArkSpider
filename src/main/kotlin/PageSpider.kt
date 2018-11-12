import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.net.URL
import java.nio.file.Files
import java.util.*
import javax.print.attribute.standard.MediaSize
import kotlin.collections.HashMap

fun String.charFilter() = String(replace("#", "Numbers")
        .toCharArray().filter { it.isLetterOrDigit() || it.isWhitespace() }.toCharArray()).trim()

fun String.legalize() = replace(' ', '_').replace('\\', '%').replace('/', '%').toLowerCase()

fun Document.listSeries() =
        select("a[href]").map { it.attr("href") }
                .filter { it.contains("/products/series") }.map {
                    Pair(it.substring(it.lastIndexOf('/') + 1), Regex("[0-9]+").find(it)!!.groupValues[0].toInt())
                }.map { Triple(it.first, it.second, it.first.asType()) }

fun Int.productSeries() = Jsoup.parse(URL("https://ark.intel.com/products/series/$this").readText())
        .select("td[class=ark-product-name]")!!.map { it.child(0).attr("href") }
        .map {
            Pair(it.substring(it.lastIndexOf('/') + 1), Regex("[0-9]+").find(it)!!.groupValues[0].toInt())
        }.map { Triple(it.first, it.second, it.first.asType()) }

fun main(args: Array<String>) {
    var buffer = ""
    val dir = "H:\\Toothpaste\\"
    URL("https://ark.intel.com/#@Processors").readText().apply {
        Jsoup.parse(this).listSeries().filter { it.third == ProductType.Processor }.forEach {
            it.second.productSeries().forEach {
                buffer += "$it\n"
                try {
                    Jsoup.parse(URL("https://ark.intel.com/products/${it.second}/").readText()).toSpecs().apply {
                        val id = this.name.legalize()
                        println("$it : product $id")
                        File("$dir$it - $id.txt").writeText(
                                toString()
                        )
                    }
                } catch (ignored: Exception) {
                    System.err.println("$it : error - ${ignored.message}")
                }
            }
        }
    }
    File("${dir}Processors.txt").writeText(buffer)
}// 27472 30767 27466 27463 30763 30764 31732 27454 27450 27609 27127 27117 27113 27109 27106 27338 91204
