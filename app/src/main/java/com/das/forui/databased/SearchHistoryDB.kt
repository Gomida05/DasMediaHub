package com.das.forui.databased

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class SearchHistoryDB(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

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
    private fun isWatchUrlExist(url: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM results WHERE title = ?", arrayOf(url.let {
            it.trimEnd()
            it.trimStart()
        }))

        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }



    fun deleteSearchList(selectedItem: String): Int {
        val db = this.writableDatabase

        try {
            // Log the trimmed item to ensure correct value
            Log.d("Database", "Attempting to delete item: '${selectedItem}'")

            val rowsDeleted = db.delete(
                "results",
                "title = ?",
                arrayOf(selectedItem)
            )

            db.close()
            Log.d("Database", "Rows deleted: $rowsDeleted")
            return rowsDeleted
        } catch (e: Exception) {
            Log.e("Database", "Error deleting item: ${e.message}")
            return 0
        }
    }




    fun insertData(title: String): Boolean {
        if(isWatchUrlExist(title)){
            return false
        }
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("title", title)
        }
        val result = db.insert("results", null, contentValues)
        db.close()
        return result != -1L
    }


    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "search_history.db"
        const val SQL_CREATE_ENTRIES =
            """ CREATE TABLE IF NOT EXISTS results ( title TEXT PRIMARY KEY) """

        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS results"
    }

}
