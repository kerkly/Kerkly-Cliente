package com.example.kerklyv5.SQLite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Base64

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
     val context: Context = context
    companion object {
        private const val DATABASE_NAME = "DBkerkly.db"
        private const val DATABASE_VERSION = 1

        // Define la estructura de la tabla
         const val TABLE_NAME = "Oficio"
         const val COLUMN_ID = "id"
         const val COLUMN_PC = "palabras_claves"
         const val COLUMN_NAME = "nombreOfi"

        //segunda tabla
        const val TABLE_NAME_USUARIOS = "Usuario"
        const val COLUMN_ID_USUARIOS = "uid"
        const val COLUMN_TELEFONO = "telefono"
        const val COLUMN_FOTO = "foto"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_APELIIDO_PA = "apellidoPa"
        const val COLUMN_APELIIDO_MA = "apellidoMa"
        const val COLUMN_CORREO = "correo"
    }
    private lateinit var dataManager: DataManager

    override fun onCreate(db: SQLiteDatabase?) {
        // Crea la tabla
        val createTableQuery = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_PC TEXT, $COLUMN_NAME TEXT)"
        db?.execSQL(createTableQuery)

        val createTableUSer = "CREATE TABLE $TABLE_NAME_USUARIOS($COLUMN_ID_USUARIOS INTEGER PRIMARY KEY, $COLUMN_TELEFONO TEXT, $COLUMN_FOTO BLOB, $COLUMN_NOMBRE TEXT, $COLUMN_APELIIDO_PA TEXT, $COLUMN_APELIIDO_MA  TEXT,$COLUMN_CORREO TEXT)"
        db?.execSQL(createTableUSer)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Si necesitas realizar cambios en la estructura de la base de datos
        // puedes implementar la lógica de actualización aquí
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Si necesitas revertir a una versión anterior de la base de datos
        // puedes implementar la lógica de degradación aquí
    }

    fun isTableExists(tableName: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'", null)
        val tableExists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return tableExists
    }


}