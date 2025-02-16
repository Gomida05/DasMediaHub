package com.das.forui.databased

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DatabaseHelper1(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    fun getResults(): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM results", null) }
    fun insertData(title: String, path: String): Boolean {
        if (isTitleExist(title)) {

            return false
        }
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("title", title)
            put("path", path)
        }
        val result = db.insert("results", null, contentValues)
        db.close()
        return result != -1L }
    private fun isTitleExist(title: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM results WHERE title = ?", arrayOf(title))

        val exists = cursor.count > 0 // If count > 0, title exists
        cursor.close() // Don't forget to close the cursor
        db.close() // Close the database connection
        return exists
    }
    private fun getSortedResults(): Cursor {
        val db = this.readableDatabase
        val query = "SELECT * FROM results ORDER BY created_at DESC"
        return db.rawQuery(query, null)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "downloads_history.db"
        const val SQL_CREATE_ENTRIES =
            """ CREATE TABLE IF NOT EXISTS results ( title TEXT PRIMARY KEY, path TEXT NOT NULL )"""

        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS results"
    }

}
