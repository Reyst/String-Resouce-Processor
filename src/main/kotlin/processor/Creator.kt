package processor

import org.w3c.dom.Document
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Stream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

const val CHAR_DELIMITER = ';'

class Creator(val from: String, val to: String, val ios: Boolean, val rtl: Boolean = false) {

    fun process() {

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.newDocument()

        val inputLines = Files.lines(Paths.get(from), StandardCharsets.UTF_8)

        if (ios)
            createIosFiles(doc, inputLines)
        else
            createAndroidFile(doc, inputLines)

        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        val domSource = DOMSource(doc)
        val streamResult = StreamResult(File(to))

        // If you use
        // StreamResult result = new StreamResult(System.out);
        // the output will be pushed to the standard output ...
        // You can use that for debugging
        transformer.transform(domSource, streamResult)

        if (rtl) {
            rtlCharacterCorrection()
        }
    }

    private fun rtlCharacterCorrection() {

        val regex = Regex("&amp;#x200f;")
        var str = ""

        Files.lines(Paths.get(to), StandardCharsets.UTF_8)
                .forEach { str += it.replace(regex, "&#x200f;") }

        val writer = OutputStreamWriter(FileOutputStream(File(to)), StandardCharsets.UTF_8)
        writer.write(str)
        writer.flush()
        writer.close()
    }

    private fun createIosFiles(doc: Document, inputLines: Stream<String>) {

        val root = doc.createElement("plist")
        root.setAttribute("version", "1.0")
        doc.appendChild(root)

        val dictElement = doc.createElement("dict")
        root.appendChild(dictElement)

        val writer = OutputStreamWriter(FileOutputStream(File(to + ".strings")), StandardCharsets.UTF_8)

        inputLines
                .filter({ str -> str != null && !str.isEmpty() })
                .map { str -> str.split(CHAR_DELIMITER) }
                .filter { it.size > 2 }
                .forEach { strList ->
                    val key = strList[0]
                    val value = strList[1]

                    writer.write("\"$key\" = \"$value\";\n")

                    val keyElement = doc.createElement("key")
                    keyElement.textContent = key
                    val valueElement = doc.createElement("string")
                    valueElement.textContent = value

                    dictElement.appendChild(keyElement)
                    dictElement.appendChild(valueElement)
                }

        writer.flush()
        writer.close()

    }

    private fun createAndroidFile(doc: Document, inputLines: Stream<String>) {

        val rtlChar = if (rtl) "&#x200f;" else "" // + "&#x200f;"

        val root = doc.createElement("resources")
        doc.appendChild(root)

        inputLines
                .filter({ str ->
                    str != null && !str.isEmpty() })
                .map { str ->
                    str.split(CHAR_DELIMITER) }
                .filter {
                    it.size >= 2 }
                .forEach { strList ->
                    val strElement = doc.createElement("string")
                    strElement.setAttribute("name", strList[0])
                    strElement.appendChild(doc.createTextNode(rtlChar + strList[1]))
                    root.appendChild(strElement)
                }

//        inputLines
////                .forEach { println(it) }
//                .forEach { str ->
//                    if(str != null && !str.isEmpty()) {
//                        val strList = str.split(CHAR_DELIMITER)
//                        if (strList.size >= 2) {
//                            val strElement = doc.createElement("string")
//                            strElement.setAttribute("name", strList[0])
//                            strElement.appendChild(doc.createTextNode(rtlChar + strList[1]))
//                            root.appendChild(strElement)
//                        }
//                    }
//                }


    }

}