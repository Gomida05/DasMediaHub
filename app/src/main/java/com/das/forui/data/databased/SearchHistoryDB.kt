package com.das.forui.data.databased

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class SearchHistoryDB(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

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
        return db.rawQuery("SELECT * FROM $DATABASE_TABLE_NAME ORDER BY rowid DESC", null)
    }

    private fun isWatchUrlExist(url: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM $DATABASE_TABLE_NAME WHERE title = ?",
            arrayOf(
                url.let {
                    it.trimEnd()
                    it.trimStart()
                }
            )
        )

        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }



    fun deleteSearchList(selectedItem: String): Int {
        val db = this.writableDatabase


        val rowsDeleted = db.delete(
            DATABASE_TABLE_NAME,
            "title = ?",
            arrayOf(selectedItem.apply {
                trimEnd()
                trimStart()
            }
            )
        )

        db.close()
        return rowsDeleted
    }




    fun insertData(title: String): Boolean {
        if(isWatchUrlExist(title)){
            return false
        }

        val db = this.writableDatabase
        val trimmedDta = title.apply {
            trimEnd()
            trimStart()
        }
        val contentValues = ContentValues().apply {
            put("title", trimmedDta)
        }
        val result = db.insert(DATABASE_TABLE_NAME, null, contentValues)
        db.close()
        return result != -1L
    }


    companion object {
        private const val DATABASE_TABLE_NAME = "Search_history"
        private const val DATABASE_VERSION = 3
        private const val DATABASE_NAME = "search_history.db"
        private const val SQL_CREATE_ENTRIES =
            """ CREATE TABLE IF NOT EXISTS $DATABASE_TABLE_NAME ( title TEXT PRIMARY KEY) """

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $DATABASE_TABLE_NAME"
    }

}
