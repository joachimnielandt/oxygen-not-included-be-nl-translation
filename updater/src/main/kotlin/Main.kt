import java.io.File

fun main(args: Array<String>) {
    // read the files
    var templateFile = File("../../strings_template.pot")
    var poFile = File("../../release/strings.po")
    // convert
    val template = toPo(templateFile)
    val po = toPo(poFile)
    // merge new translations into the po file
    var result = mergeNewTranslations(template, po)
    result = overwriteOriginalIds(result, template.translations)
    result = removeEliminatedTranslations(result, template.translations)
    writeOutput(po.preamble, result, File("temp.po"))
}

fun removeEliminatedTranslations(translations: List<Trans>, originals: List<Trans>): List<Trans> {
    val result = translations.toMutableList()
    result.removeIf {  r ->
       originals.find { o -> o.comment == r.comment} == null
    }
    return result
}

fun overwriteOriginalIds(target: List<Trans>, originals: List<Trans>): List<Trans> {
    return target.map { t ->
        originals
            .firstOrNull { it.ctxt == t.ctxt }
            ?.let { t.id = it.id }
        t
    }
}

fun writeOutput(preamble: List<String>, merged: List<Trans>, file: File) {
    file.printWriter().use { pw ->
        // preamble write
        preamble.forEach { pw.println(it) }
        // process translations
        merged.forEach { t ->
            pw.println(t.comment)
            pw.println(t.ctxt)
            pw.println(t.id)
            pw.println(t.str)
            pw.println()
        }
    }
}

fun mergeNewTranslations(template: Po, po: Po): List<Trans> {
    val result = ArrayList<Trans>(po.translations)

    template.translations.forEachIndexed { templateIndex, templateTranslation ->
        // process the ones we can't find
        val foundIt = po.translations.find { p -> p.comment == templateTranslation.comment } != null
        if(!foundIt) {
            // find the place where to insert it: right behind the previous template translation
            val previousTemplateTranslation = template.translations[templateIndex - 1]
            val targetIndex = result.indexOfFirst { f -> f.comment == previousTemplateTranslation.comment }
            result.add(targetIndex+1, templateTranslation)
        }
    }

    return result
}

fun toPo(file: File): Po {
    val lines = file.readLines()
    val preAmble = lines.takeWhile { !isTranslationStart(it) }
    val fold = lines.dropWhile { !isTranslationStart(it) }
        .filter { it.isNotEmpty() }
        .fold(ArrayList<Trans>()) { translations, line: String ->
            // append
            if (isTranslationStart(line)) {
                translations.add(Trans(comment = line))
            } else if (isMsgCtxt(line)) {
                translations.last().ctxt = line
            } else if (isMsgId(line)) {
                translations.last().id = line
            } else if (isMsgStr(line)) {
                translations.last().str = line
            }
            // return the result
            translations
        }

    // the result
    return Po(preAmble, fold)
}

fun isMsgStr(line: String): Boolean = line.startsWith("msgstr ")

fun isMsgId(line: String): Boolean = line.startsWith("msgid ")

fun isMsgCtxt(line: String): Boolean = line.startsWith("msgctxt ")

fun isTranslationStart(line: String): Boolean = line.startsWith("#. ")

