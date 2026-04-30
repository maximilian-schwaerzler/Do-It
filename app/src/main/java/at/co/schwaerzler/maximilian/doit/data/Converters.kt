package at.co.schwaerzler.maximilian.doit.data

import androidx.room.TypeConverter
import java.util.Date
import kotlin.time.Instant

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochSeconds(it) }
    }

    @TypeConverter
    fun instantToTimestamp(date: Instant?): Long? {
        return date?.epochSeconds
    }
}