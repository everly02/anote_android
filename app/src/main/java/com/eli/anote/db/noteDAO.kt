package com.eli.anote.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note

    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<Note>

    @Query("UPDATE notes SET is_archived = :isArchived WHERE id = :id")
    suspend fun archiveNoteById(id: Int, isArchived: Boolean)

    @Query("SELECT * FROM notes WHERE is_archived = 0")
    fun getUnarchivedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE is_archived = 1")
    fun getarchievedNotes(): Flow<List<Note>>
}
