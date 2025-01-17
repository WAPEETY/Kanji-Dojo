package ua.syt0r.kanji.parser.parsers

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.File

data class JMdictFuriganaItem(
    @SerializedName("text") val kanjiExpression: String,
    @SerializedName("reading") val kanaExpression: String,
    @SerializedName("furigana") val furigana: List<JMDictFuriganaRubyItem>
)

data class JMDictFuriganaRubyItem(
    val ruby: String,
    val rt: String?
)

object JMdictFuriganaParser {

    fun parse(file: File): List<JMdictFuriganaItem> {
        val jsonReader = JsonReader(file.inputStream().reader())
        return Gson().fromJson(
            jsonReader,
            object : TypeToken<List<JMdictFuriganaItem>>() {}.type
        )
    }

}