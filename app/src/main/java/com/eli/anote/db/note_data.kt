package com.eli.anote.db
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "type") val type: NoteType,
    @ColumnInfo(name="title") var title: String,
    @ColumnInfo(name = "content") var content: String,
    @ColumnInfo(name = "previewImagePath") var previewImage: String? = null,
    @ColumnInfo(name = "is_archived") var isArchived: Boolean
)


enum class NoteType {
    TEXT, VIDEO, AUDIO
}
