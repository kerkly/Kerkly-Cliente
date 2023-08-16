package com.example.kerklyv5.SQLite

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.kerklyv5.modelo.usuariosSqlite
import java.io.ByteArrayOutputStream

class DataManager(context: Context) {
    private val databaseHelper: DatabaseHelper = DatabaseHelper(context)

private fun insertUser(id: Int, palabrasClaves: String,nombreOfi: String) {
        val values = ContentValues()
        values.put(DatabaseHelper.COLUMN_ID,id)
        values.put(DatabaseHelper.COLUMN_PC, palabrasClaves)
        values.put(DatabaseHelper.COLUMN_NAME, nombreOfi)

        val db = databaseHelper.writableDatabase
        db.insert(DatabaseHelper.TABLE_NAME, null, values)
        println("dato insertado $nombreOfi")
        db.close()
    }

    fun insertOrUpdateOficio(oficio: MisOficios,id: Int, palabrasClaves: String,nombreOfi: String) {
        val db = databaseHelper.writableDatabase
        // Consulta para verificar si ya existe un registro con el mismo valor de id
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(oficio.id.toString()))

        if (cursor.moveToFirst()) {
            // Si se encuentra un registro con el mismo id, puedes decidir si quieres actualizarlo o ignorar la inserción
            // Por ejemplo, puedes actualizar el registro existente con los nuevos datos
            // updateOficio(oficio)
            println("si se encuentra")
        } else {
            // Si no se encuentra ningún registro con el mismo id, puedes proceder a insertar el nuevo registro
            /*val values = ContentValues()
            values.put(DatabaseHelper.COLUMN_ID, oficio.id)
            values.put(DatabaseHelper.COLUMN_PC, oficio.palabrasClaves)
            values.put(DatabaseHelper.COLUMN_NAME, oficio.nombreOfi)

            db.insert(DatabaseHelper.TABLE_NAME, null, values)*/
            println("no se encuentra")
            insertUser(id,palabrasClaves,nombreOfi)
        }
        cursor.close()
        db.close()
    }


    @SuppressLint("Range")
    fun getAllOficios(): ArrayList<MisOficios> {
        val db = databaseHelper.readableDatabase
        val oficiosList = mutableListOf<MisOficios>()

        val query = "SELECT * FROM ${DatabaseHelper.TABLE_NAME}"
        val cursor = db.rawQuery(query, null)
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID))
            val palabrasClaves = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PC))
            val nombreOfi = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME))
            val oficio = MisOficios(id, palabrasClaves,nombreOfi)
            oficiosList.add(oficio)
        }
        cursor.close()
        db.close()
        return oficiosList as ArrayList<MisOficios>
    }
    fun deleteAllOficios() {
        val db = databaseHelper.writableDatabase
        val deleteQuery = "DELETE FROM ${DatabaseHelper.TABLE_NAME}"
        db.execSQL(deleteQuery)
        db.close()
    }
    fun verificarSiElUsarioExiste(usuario: usuariosSqlite, telefono: String, foto: ByteArray, nombre: String, apellidoPa:String, apellidoMa:String, correo: String){
        val db = databaseHelper.writableDatabase
        // Consulta para verificar si ya existe un registro con el mismo valor de id
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_NAME_USUARIOS} WHERE ${DatabaseHelper.COLUMN_ID_USUARIOS} = ?"
        val cursor = db.rawQuery(query, arrayOf(usuario.telefono.toString()))
        if (cursor.moveToFirst()) {
            println("el usuario si se encuentra")
        } else {
            misDatos(telefono,foto,nombre,apellidoPa,apellidoMa,correo)
            println("el usuario no se encuentra")
        }
        cursor.close()
        db.close()
    }
    private fun misDatos(telefono: String, foto: ByteArray, nombre: String,apellidoPa:String,apellidoMa:String,correo: String){
        val values = ContentValues()
        values.put(DatabaseHelper.COLUMN_ID_USUARIOS,telefono)
        values.put(DatabaseHelper.COLUMN_FOTO, foto)
        values.put(DatabaseHelper.COLUMN_NOMBRE, nombre)
        values.put(DatabaseHelper.COLUMN_APELIIDO_PA, apellidoPa)
        values.put(DatabaseHelper.COLUMN_APELIIDO_MA, apellidoMa)
        values.put(DatabaseHelper.COLUMN_CORREO, correo)

        val db = databaseHelper.writableDatabase
        db.insert(DatabaseHelper.TABLE_NAME_USUARIOS, null, values)
        //println("dato insertado $nombreOfi")
        db.close()
    }

    @SuppressLint("Range", "SuspiciousIndentation")
    fun DatosDelUsuario(context: Context): ArrayList<usuariosSqlite> {
        val db = databaseHelper.readableDatabase
        val datosUsuario = mutableListOf<usuariosSqlite>()
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_NAME_USUARIOS}"
        val cursor = db.rawQuery(query, null)

        val totalRows = cursor.count
        var loadedRows = 0

        while (cursor.moveToNext()) {
            val idTelefono = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID_USUARIOS))
            val foto = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.COLUMN_FOTO))
            val NOMBRE = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOMBRE))
            val ApellidoPa = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_APELIIDO_PA))
            val ApellidoMa = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_APELIIDO_MA))
            val correo = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CORREO))

            val usuarios = usuariosSqlite(idTelefono, foto, NOMBRE, ApellidoPa, ApellidoMa, correo)
                        datosUsuario.add(usuarios)
        }
        return datosUsuario as ArrayList<usuariosSqlite>
    }

}