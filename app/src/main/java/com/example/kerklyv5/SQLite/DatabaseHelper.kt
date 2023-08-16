package com.example.kerklyv5.SQLite

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
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
        const val COLUMN_ID_USUARIOS = "IdUsuario"
        const val COLUMN_FOTO = "Foto"
        const val COLUMN_NOMBRE = "Nombre"
        const val COLUMN_APELIIDO_PA = "ApellidoPaterno"
        const val COLUMN_APELIIDO_MA = "ApellidoMaterno"
        const val COLUMN_CORREO = "Correo"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Crea la tabla
        val createTableQuery = "CREATE TABLE $TABLE_NAME ($COLUMN_ID Long PRIMARY KEY, $COLUMN_PC TEXT, $COLUMN_NAME TEXT)"
        db?.execSQL(createTableQuery)

        val createTableUSer = "CREATE TABLE $TABLE_NAME_USUARIOS($COLUMN_ID_USUARIOS INTEGER PRIMARY KEY, $COLUMN_FOTO BLOB, $COLUMN_NOMBRE TEXT, $COLUMN_APELIIDO_PA TEXT, $COLUMN_APELIIDO_MA  TEXT,$COLUMN_CORREO TEXT)"
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
}