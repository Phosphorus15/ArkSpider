import org.dizitart.no2.Document.createDocument
import org.dizitart.no2.IndexOptions
import org.dizitart.no2.IndexOptions.indexOptions
import org.dizitart.no2.IndexType
import org.dizitart.no2.Nitrite
import java.io.File

fun main(args: Array<String>) {
    val db = "H:\\Toothpaste\\toothpastes.db"
    val dir = "H:\\Toothpaste\\"
    val files = File(dir).listFiles().filter { it.isFile && it.name.startsWith('(') }
    val uniqueIndex = mutableSetOf<String>()
    val indexHeader = mutableMapOf<String, String>()
    val indexOrigin = mutableMapOf<String, String>()
    val specs = mutableListOf<ProductSpecs>()
    indexHeader["ark_id"] = "Essentials"
    indexOrigin["ark_id"] = "Ark Id"
    uniqueIndex.add("full_name")
    indexHeader["full_name"] = "Headers"
    indexOrigin["full_name"] = "Full Id"
    uniqueIndex.add("hint")
    indexHeader["hint"] = "Headers"
    indexOrigin["hint"] = "Hint"
    uniqueIndex.add("ark_id")
    files.forEach {
        val triple = it.name.loadTriple()
        val lines = it.readText().split('\n')
        val spec = ProductSpecs(lines[0], lines[1])
        spec["root"] = mutableMapOf(Pair("hint", lines[1]), Pair("full_name", lines[0]))
        var current = ""
        lines.subList(2, lines.size).filter { it.isNotBlank() }.forEach {
            if (it.startsWith('<')) {
                current = it.substring(1, it.length - 1)
            } else {
                val value = it.substring(it.indexOf(':') + 1).trim()
                val key = it.substring(0, it.indexOf(':')).trim()
                if (!uniqueIndex.contains(key.legalize())) {
                    uniqueIndex.add(key.legalize())
                    indexOrigin[key.legalize()] = key
                    indexHeader[key.legalize()] = current
                }
                (spec["root"] as MutableMap)[key.legalize()] = value
            }
        }
        specs.add(spec)
    }
    Nitrite.builder().compressed().filePath(db).openOrCreate().apply {
        getCollection("processors").apply {
            uniqueIndex.forEach {
                if (!hasIndex(it))
                    when (it) {
                        "ark_id" -> createIndex(it, indexOptions(IndexType.Unique))
                        "full_name" -> createIndex(it, indexOptions(IndexType.NonUnique))
                        else -> createIndex(it, indexOptions(IndexType.NonUnique))
                    }
            }
            specs.forEach {
                println(it.name)
                val doc = createDocument("ark_id", it["root"]!!["ark_id"]?.toInt())
                it["root"]!!.forEach { key, value ->
                    doc[key] = value
                }
                insert(doc)
            }
        }
        getCollection("name_full").apply {
            if (!hasIndex("original")) createIndex("original", indexOptions(IndexType.Unique))
            if (!hasIndex("legalized")) createIndex("legalized", indexOptions(IndexType.Unique))
            indexOrigin.forEach { key, value ->
                val doc = createDocument("legalized", key)
                doc["original"] = value
                insert(doc)
            }
        }
        getCollection("index_header").apply {
            if (!hasIndex("header")) createIndex("header", indexOptions(IndexType.NonUnique))
            if (!hasIndex("index")) createIndex("index", indexOptions(IndexType.Unique))
            indexHeader.forEach { key, value ->
                val doc = createDocument("index", key)
                doc["header"] = value
                insert(doc)
            }
        }
    }.close()
}