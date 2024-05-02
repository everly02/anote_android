package com.eli.anote.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun toNoteType(value: Int) = enumValues<NoteType>()[value]

    @TypeConverter
    fun fromNoteType(value: NoteType) = value.ordinal
}
