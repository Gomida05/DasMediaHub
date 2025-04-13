package com.das.forui.databased

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class SearchHistoryDB(
    context: Context
): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

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
        val cursor = db.rawQuery("SELECT 1 FROM results WHERE title = ?",
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
            "results",
            "title = ?",
            arrayOf(selectedItem)
        )

        db.close()
        return rowsDeleted
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
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "search_history.db"
        const val SQL_CREATE_ENTRIES =
            """ CREATE TABLE IF NOT EXISTS results ( title TEXT PRIMARY KEY) """

        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS results"
    }

}
