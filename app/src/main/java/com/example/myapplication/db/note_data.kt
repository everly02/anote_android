
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "type") val type: NoteType,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean
)

enum class NoteType {
    TEXT, VIDEO, AUDIO
}
