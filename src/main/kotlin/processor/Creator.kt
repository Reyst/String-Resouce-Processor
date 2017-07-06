package processor

import org.w3c.dom.Document
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class Creator(val from: String, val to: String, val ios: Boolean, val rtl: Boolean = false) {

    fun process() {

        val sc = Scanner(File(from))

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.newDocument()

        if (ios)
            createIosFiles(doc, sc)
        else
            createAndroidFile(doc, sc)

        sc.close()

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
        val sc1 = Scanner(File(to))

        var str = ""

        while (sc1.hasNextLine()) {
            str += sc1.nextLine().replace(Regex("&amp;#x200f;"), "&#x200f;")
        }

        sc1.close()

        val writer = OutputStreamWriter(FileOutputStream(File(to)))
        writer.write(str)
        writer.flush()
        writer.close()
    }

    private fun createIosFiles(doc: Document, sc: Scanner) {

        val root = doc.createElement("plist")
        root.setAttribute("version", "1.0")
        doc.appendChild(root)

        val dictElement = doc.createElement("dict")
        root.appendChild(dictElement)

        val writer = OutputStreamWriter(FileOutputStream(File(to + ".strings")))

        while (sc.hasNextLine()) {

            val str = sc.nextLine()
            if (str == null || str.isEmpty()) continue

            val strList = str.split(';')

            if (strList.size < 2) continue

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

    private fun createAndroidFile(doc: Document, sc: Scanner) {

        val rtlChar = if (rtl) "&#x200f;" else "" // + "&#x200f;"

        val root = doc.createElement("resources")
        doc.appendChild(root)


        while (sc.hasNextLine()) {

            val str = sc.nextLine()
            if (str == null || str.isEmpty()) continue

            val strList = str.split(';')

            if (strList.size < 2) continue

            val strElement = doc.createElement("string")
            strElement.setAttribute("name", strList[0])

            strElement.appendChild(doc.createTextNode(rtlChar + strList[1]))
            //strElement.appendChild(doc.createTextNode(String("\u200f".toByteArray()) + strList[1]))

            root.appendChild(strElement)
        }
    }

}