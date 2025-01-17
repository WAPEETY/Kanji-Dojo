package ua.syt0r.kanji.core.kanji_data.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FuriganaString(
    val compounds: List<FuriganaStringCompound>
) : Parcelable {

    operator fun plus(string: String): FuriganaString {
        return FuriganaString(compounds.plus(FuriganaStringCompound(string)))
    }

}

@Parcelize
data class FuriganaStringCompound(
    val text: String,
    val annotation: String? = null
) : Parcelable

class FuriganaStringBuilder {

    private val list = mutableListOf<FuriganaStringCompound>()

    fun append(character: String, annotation: String? = null) =
        list.add(FuriganaStringCompound(character, annotation))

    fun append(furiganaString: FuriganaString) {
        list.addAll(furiganaString.compounds)
    }

    fun build() = FuriganaString(list)

}

fun buildFuriganaString(scope: FuriganaStringBuilder.() -> Unit): FuriganaString {
    val builder = FuriganaStringBuilder()
    builder.scope()
    return builder.build()
}

private const val ENCODED_SYMBOL = "○"
fun FuriganaString.encode(characterToEncode: String): FuriganaString {
    return FuriganaString(
        compounds = compounds.map {
            FuriganaStringCompound(
                text = it.text.replace(characterToEncode, ENCODED_SYMBOL),
                annotation = it.annotation?.replace(characterToEncode, ENCODED_SYMBOL)
            )
        }
    )
}