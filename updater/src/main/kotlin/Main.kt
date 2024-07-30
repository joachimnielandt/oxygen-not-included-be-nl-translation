import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.File
import kotlin.math.max

val translationUri = "http://localhost:5000/translate"
val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }
}

/**
 * Translate using the libretranslate container.
 */
suspend fun translate(text: String): TranslationResponse {
    return client.post(translationUri) {
        contentType(ContentType.Application.Json)
        setBody(TranslationRequest(text))
    }.body<TranslationResponse>()
}

@Serializable
data class TranslationRequest(
    val q: String,
    val source: String = "auto",
    val target: String = "nl",
    val format: String = "text",
    val alternatives: Int = 0,
    val api_key: String = ""
)

//"alternatives": [
//"dit is een string"
//],
//"detectedLanguage": {
//    "confidence": 45,
//    "language": "en"
//},
//"translatedText": "dit is een tekenreeks"
@Serializable
data class TranslationResponse(val translatedText: String, val detectedLanguage: JsonObject)

suspend fun main(args: Array<String>) {
    // read the files
    var templateFile = File("../strings_template.pot")
    var poFile = File("../release/strings.po")
    // convert
    val template = toPo(templateFile)
    val po = toPo(poFile)
        // translate empty strings
        .let { translatePo(it) }
        .let { commonFixes(it) }
    // merge new translations into the po file
    var result = mergeNewTranslations(template, po)
    result = overwriteOriginalIds(result, template.translations)
    result = removeEliminatedTranslations(result, template.translations)
    writeOutput(po.preamble, result, File("temp.po"))
}

/**
 * Applied to all translations.
 */
fun commonFixes(po: Po): Po {
    return po.copy(preamble = po.preamble, translations = po.translations.map {
        it.copy(comment = it.comment, ctxt = it.ctxt, id = it.id, str = it.str
            ?.replace("</stijl>", "</style>")
            ?.replace("<stijl>", "<style>")
        )
    })
}

suspend fun translatePo(po: Po): Po {
    val newTranslations = po.translations.map {
        if (it.str.isNullOrBlank() && !it.id.isNullOrBlank()) {
            val translatedText = translate(it.id!!).translatedText
            if (translatedText.isNotBlank()) {
                println("translatedText = ${translatedText}")
                it.copy(str = translatedText, id = it.id, ctxt = it.ctxt, comment = it.comment)
            } else {
                println("not translated: ${it}")
                it
            }
        } else {
            it
        }
    }
    return po.copy(preamble = po.preamble, translations = newTranslations)
}

fun removeEliminatedTranslations(translations: List<Trans>, originals: List<Trans>): List<Trans> {
    val result = translations.toMutableList()
    result.removeIf { r ->
        originals.find { o -> o.comment == r.comment } == null
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
            pw.println("msgctxt \"${t.ctxt}\"")
            pw.println("msgid \"${t.id}\"")
            pw.println("msgstr \"${t.str}\"")
            pw.println()
        }
    }
}

fun mergeNewTranslations(template: Po, po: Po): List<Trans> {
    val result = ArrayList<Trans>(po.translations)

    template.translations.forEachIndexed { templateIndex, templateTranslation ->
        // process the ones we can't find
        val foundIt = po.translations.find { p -> p.comment == templateTranslation.comment } != null
        if (!foundIt) {
            // find the place where to insert it: right behind the previous template translation
            val previousTemplateTranslation = template.translations[max(templateIndex - 1, 0)]
            val targetIndex = result.indexOfFirst { f -> f.comment == previousTemplateTranslation.comment }
            result.add(targetIndex + 1, templateTranslation)
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
                val msgCtxtContent = "msgctxt +\"(.*)\"".toRegex().find(line)?.groupValues?.get(1)
                translations.last().ctxt = msgCtxtContent
            } else if (isMsgId(line)) {
                val msgIdContent = "msgid +\"(.*)\"".toRegex().find(line)?.groupValues?.get(1)
                translations.last().id = msgIdContent
            } else if (isMsgStr(line)) {
                val msgStrContent = "msgstr +\"(.*)\"".toRegex().find(line)?.groupValues?.get(1)
                translations.last().str = msgStrContent
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

