import processor.Creator
import processor.Extractor

const val PARAM_EXTRACT = "extract"
const val PARAM_CREATE = "create"
const val PARAM_IOS = "ios"
const val PARAM_RTL = "rtl"


fun main(vararg params: String) {

    val ios = params.contains(PARAM_IOS)
    val rtl = params.contains(PARAM_RTL)

    if (params.contains(PARAM_EXTRACT)) {
        extractStringsToTxt(params, ios)
    } else if (params.contains(PARAM_CREATE)) {
        createStringsFromTxt(params, ios, rtl)
    } else {
        println("str_processor-1.0.jar <create|extract> src dst [rtl] [ios]")
        println()
        println(" - extract - take the strings from the resource files")
        println(" - create  - create the resource file(s) based on the text file")
        println(" - src - file or folder with resources for the EXTRACT operation and file with texts for the CREATE operation")
        println(" - dst - file for writing results, for the IOS-format the program creates additional file .strings for the CREATE operation")
        println(" - rtl - it's flag for the languages with direction from the right to left. It's used for CREATE operation")
        println(" - ios - it's flag IOS-format, without it here is being used android-format")
    }
}

fun createStringsFromTxt(params: Array<out String>, ios: Boolean, rtl : Boolean = false) {

    var from = ""
    var to = ""

    params.filter { it != PARAM_CREATE && it != PARAM_IOS }
            .take(2)
            .forEach { if (from.isEmpty()) from = it else to = it }

    val creator = Creator(from, to, ios, rtl)

    creator.process()

}

fun extractStringsToTxt(params: Array<out String>, ios: Boolean) {

    var from = ""
    var to = ""

    params.filter { it != PARAM_EXTRACT && it != PARAM_IOS }
            .take(2)
            .forEach { if (from.isEmpty()) from = it else to = it }

    val extractor = Extractor(from, to, ios)
    extractor.process()
}

