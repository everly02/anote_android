package com.example.myapplication.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert
    fun insert(note: Note)

    @Update
    fun update(note: Note)

    @Delete
    fun delete(note: Note)

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Int): Note

    @Query("SELECT * FROM notes")
    fun getAllNotes(): List<Note>

    @Query("UPDATE notes SET is_archived = :isArchived WHERE id = :id")
    fun archiveNoteById(id: Int, isArchived: Boolean)

    @Query("SELECT * FROM notes WHERE is_archived = 0")
    fun getUnarchivedNotes(): Flow<List<Note>>
}
