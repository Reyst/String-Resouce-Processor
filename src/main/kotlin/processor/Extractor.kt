package processor

import org.w3c.dom.NodeList
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class Extractor(val from: String, val to: String, val ios: Boolean) {

    fun process() {

        val src = File(from)
        val fileNamePattern = Regex(if (!ios) ".*xml$" else ".*strings$")

        val data = if (src.isDirectory) {
            src.listFiles { _, name -> name.matches(fileNamePattern) }
        } else {
            arrayOf(src)
        }

        val writer = OutputStreamWriter(FileOutputStream(File(to)))

        data.forEach {
            writer.write("${it.name}\n")
            processFile(it, writer, ios)
            writer.write("\n\n")
        }

        writer.flush()
        writer.close()

    }

    private fun processFile(file: File, writer: OutputStreamWriter, ios: Boolean) {

        println("File: ${file.name}")

        if (!ios) {
            processAndroidFile(file, writer)
        } else {
            processIosFile(file, writer)

        }
    }

    private fun processIosFile(file: File, writer: OutputStreamWriter) {
        val sc = Scanner(file)

        while (sc.hasNextLine()) {

            val str = sc.nextLine()

            if (str == null || str.isEmpty()) continue

            val strParts = str.split('=')

            if (strParts.size < 2) continue

            val name = strParts[0].trim(' ', ';' , '"')
            val txt = strParts[1].trim(' ', ';' , '"')
            println("$name;$txt")
            writer.write("$name;$txt\n")
        }
    }

    private fun processAndroidFile(file: File, writer: OutputStreamWriter) {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val parser = builder.parse(file)

        val xPath = XPathFactory.newInstance().newXPath()
        val expression = "//string"
        val strs = xPath.compile(expression).evaluate(parser, XPathConstants.NODESET) as NodeList

        (0..strs.length)
                .map { strs.item(it) }
                .forEach {

                    if (it != null && it.hasAttributes()) {
                        val translatable = it.attributes.getNamedItem("translatable")
                        if (translatable == null || translatable.nodeValue != "false") {
                            val name = it.attributes.getNamedItem("name").nodeValue
                            val txt = it.textContent
                            println("$name;$txt")
                            if (name != null && txt != null) writer.write("$name;$txt\n")
                        }
                    }
                }
    }

}
