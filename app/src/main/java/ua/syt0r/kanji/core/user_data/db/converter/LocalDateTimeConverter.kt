package ua.syt0r.kanji.core.user_data.db.converter

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object LocalDateTimeConverter {

    @TypeConverter
    fun convert(time: Long): LocalDateTime? = when (time) {
        0L -> null
        else -> LocalDateTime.ofInstant(
            Instant.ofEpochMilli(time),
            ZoneId.systemDefault()
        )
    }

    @TypeConverter
    fun convert(time: LocalDateTime?): Long = time?.atZone(ZoneId.systemDefault())
        ?.toInstant()
        ?.toEpochMilli()
        ?: 0

}