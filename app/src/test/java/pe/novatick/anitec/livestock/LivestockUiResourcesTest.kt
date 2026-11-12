package pe.novatick.anitec.livestock
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
class LivestockUiResourcesTest {
    @Test fun translationsKeepIdenticalKeysAndFormatArgumentsAndValidUtf8() {
        fun strings(folder: String): Map<String,String> {
            val file=listOf(File("src/main/res/$folder/livestock_strings.xml"),File("app/src/main/res/$folder/livestock_strings.xml")).first {it.exists()}
            val text=file.readBytes().toString(Charsets.UTF_8);assertFalse(text.contains('\uFFFD'))
            val nodes=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getElementsByTagName("string")
            return (0 until nodes.length).associate { val node=nodes.item(it);node.attributes.getNamedItem("name").nodeValue to node.textContent }
        }
        val spanish=strings("values");val english=strings("values-en");assertEquals(spanish.keys,english.keys)
        val formats=Regex("%[0-9]+\\$[ds]")
        spanish.forEach { (key,value)->assertEquals(key,formats.findAll(value).map {it.value}.toList(),formats.findAll(english.getValue(key)).map {it.value}.toList()) }
    }
}
