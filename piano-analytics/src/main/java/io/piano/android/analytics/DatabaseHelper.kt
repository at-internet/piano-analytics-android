package io.piano.android.analytics

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.piano.android.analytics.model.EventRecord

internal class DatabaseHelper(
    context: Context,
    private val dataEncoder: DataEncoder,
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            for (v in 0 until DATABASE_VERSION)
                migrate(db, v)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (db != null) {
            for (v in oldVersion until newVersion)
                migrate(db, v)
        }
    }

    fun migrate(db: SQLiteDatabase, version: Int) {
        when (version) {
            0 -> // database does not exist, create it
                db.execSQL(
                    """
                        CREATE TABLE ${EventRecord.TABLE_NAME} (
                        ${EventRecord.ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                        ${EventRecord.DATA} TEXT NOT NULL,
                        ${EventRecord.TIME} INTEGER,
                        ${EventRecord.IS_SENT} INTEGER
                        );
                    """.trimIndent()
                )
        }
    }

    internal fun EventRecord.toContentValues(): ContentValues = ContentValues().apply {
        put(EventRecord.DATA, dataEncoder.encode(data))
        put(EventRecord.TIME, timestamp)
        put(EventRecord.IS_SENT, isSent)
    }

    fun ContentValues.toEventRecord(): EventRecord = EventRecord(
        dataEncoder.decode(getAsString(EventRecord.DATA)),
        getAsLong(EventRecord.TIME),
        getAsLong(EventRecord.ID),
        getAsBoolean(EventRecord.IS_SENT)
    )

    fun save(eventRecord: EventRecord): Long = eventRecord.id?.let { id ->
        writableDatabase.update(
            EventRecord.TABLE_NAME,
            eventRecord.toContentValues(),
            "${EventRecord.ID} = ?",
            arrayOf(id.toString())
        ).toLong()
    } ?: writableDatabase.insert(
        EventRecord.TABLE_NAME,
        null,
        eventRecord.toContentValues()
    )

    fun delete(eventRecord: EventRecord): Int = eventRecord.id?.let { id ->
        delete("${EventRecord.ID} = ?", id.toString())
    } ?: -1

    fun delete(whereClause: String?, vararg whereArgs: String): Int =
        writableDatabase.delete(EventRecord.TABLE_NAME, whereClause, whereArgs)

    fun query(
        columns: Array<String>? = EventRecord.COLUMNS,
        selection: String? = null,
        selectionArgs: Array<out String?>? = null,
        groupBy: String? = null,
        having: String? = null,
        orderBy: String? = "${EventRecord.TIME} ASC",
        limit: String? = null,
    ): List<EventRecord> = readableDatabase.query(
        EventRecord.TABLE_NAME,
        columns,
        selection,
        selectionArgs,
        groupBy,
        having,
        orderBy,
        limit
    ).use { c ->
        generateSequence { if (c.moveToNext()) c else null }
            .map(Companion::cursorRowToContentValues)
            .map {
                it.toEventRecord()
            }.filter {
                it.isValid
            }.toList()
    }

    companion object {
        const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "events.db"

        @JvmStatic
        private fun cursorRowToContentValues(c: Cursor): ContentValues = ContentValues().apply {
            for (i in 0 until c.columnCount) {
                val name = c.columnNames[i]
                when (c.getType(i)) {
                    Cursor.FIELD_TYPE_BLOB -> put(name, c.getBlob(i))
                    Cursor.FIELD_TYPE_FLOAT -> put(name, c.getFloat(i))
                    Cursor.FIELD_TYPE_INTEGER -> put(name, c.getLong(i))
                    else -> put(name, c.getString(i))
                }
            }
        }
    }
}
