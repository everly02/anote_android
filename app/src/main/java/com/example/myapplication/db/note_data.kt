
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "type") val type: NoteType,
    @ColumnInfo(name="title") val title: String,
    @ColumnInfo(name = "content") val content: String,//文本串或文件路径
    @ColumnInfo(name = "is_archived") val isArchived: Boolean
)

enum class NoteType {
    TEXT, VIDEO, AUDIO
}
