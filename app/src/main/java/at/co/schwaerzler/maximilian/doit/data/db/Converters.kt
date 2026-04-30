package at.co.schwaerzler.maximilian.doit.data.db

import androidx.room.TypeConverter
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