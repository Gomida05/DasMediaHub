package com.das.mediaHub.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.das.mediaHub.data.model.SearchData

class SearchHistoryDB(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (db != null) {
            db.execSQL(SQL_DELETE_ENTRIES)
            onCreate(db)
        }
    }


    fun insert(searchData: SearchData) : Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", searchData.id)
            put("value", searchData.value)
        }
        return db.insertWithOnConflict(
            SEARCH_TABLE,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        ).also {
            db.close()
        }
    }

    fun delete(id: String) {
        val db = writableDatabase
        db.delete(
            SEARCH_TABLE,
            "id = ?",
            arrayOf(id)
        ).also {
            db.close()
        }
    }

    fun update(searchData: SearchData) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("value", searchData.value)
        }
        db.update(
            SEARCH_TABLE,
            values,
            "id = ?", arrayOf(searchData.id)
        ).also {
            db.close()
        }
    }






    fun getAllSearches(): List<SearchData> {
        val cursor = readableDatabase.query(
            SEARCH_TABLE, null, null, null, null, null,
            "id ASC"
        )
        val journeys = mutableListOf<SearchData>()
        with(cursor) {
            while (moveToNext()) {
                journeys.add(
                    SearchData(
                        id = getString(getColumnIndexOrThrow("id")),
                        value = getString(getColumnIndexOrThrow("value")),
                    )
                )
            }
            close()
        }
        return journeys

    }


    companion object {
        private const val SEARCH_TABLE = "search_data"
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "search_history.db"
        private const val SQL_CREATE_ENTRIES =
            """ CREATE TABLE IF NOT EXISTS $SEARCH_TABLE ( id TEXT PRIMARY KEY, value TEXT NOT NULL ) """

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS $SEARCH_TABLE"
    }

}