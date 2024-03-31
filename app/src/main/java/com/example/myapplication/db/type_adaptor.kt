package com.example.myapplication.db

import androidx.room.TypeConverter
import com.example.myapplication.db.NoteType

class Converters {
    @TypeConverter
    fun toNoteType(value: Int) = enumValues<NoteType>()[value]

    @TypeConverter
    fun fromNoteType(value: NoteType) = value.ordinal
}
