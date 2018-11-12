import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*

class ProductSpecs(public var name: String, public var brief: String) : HashMap<String, Map<String, String>>() {

    fun flat(): Map<String, String> {
        val collector = mutableMapOf<String, String>()
        forEach { _, m ->
            m.forEach { key, value ->
                collector[key.legalize()] = value
            }
        }
        return collector
    }

    override fun toString(): String {
        var buffer = ""
        buffer += name + '\n'
        buffer += brief + '\n'
        forEach { nym, m ->
            buffer += "<$nym>\n"
            m.forEach { key, value ->
                buffer += "$key : $value\n"
            }
        }
        return buffer
    }

}

fun Document.toSpecs(): ProductSpecs {
    val header = select("div[class=product-family-title-text]")[0]
    val name = header.child(0).text()
    val brief = header.child(1).text()
    val specs = select("div[id=tab-blade-1-0]").select("div[class=blade-inside]")
    val headers = specs.select("div[class=subhead]")
    val headerNames = headers.select("h2[class]").map(Element::text)
    val specLists = specs.select("ul[class=specs-list]")
    val specObj = ProductSpecs(name, brief)
    var i = 0
    specLists.forEach {
        val specsMap = TreeMap<String, String>()
        it.children().forEach {
            specsMap[it.child(0).text().charFilter()] = it.child(1).text()
        }
        specObj[headerNames[i++]] = specsMap
    }
    return specObj
}

enum class ProductType {
    NUC, Processor, SSD, Chipset, Memory, Compute, Other
}

fun String.asType() = when {
    ProductType.values().map(ProductType::name).any { this.contains(it) } -> ProductType.values().find { this.contains(it.name) }
    else -> {
        ProductType.Other
    }
}

fun String.loadTriple(): Triple<String, Int, ProductType> = if (startsWith('(')) substring(1, indexOf(')')).loadTriple() else split(',').let {
    val id = it.subList(0, it.size - 2).joinToString()
    Triple(id.trim(), it[it.size - 2].trim().toInt(), it[it.size - 1].trim().asType()!!)
}

fun main(args: Array<String>) {
    println("(Intel®-Celeron®-D-Processor-350%350J, 27127, Processor) : product intel®_celeron®_d_processor_350%350j".loadTriple())
}