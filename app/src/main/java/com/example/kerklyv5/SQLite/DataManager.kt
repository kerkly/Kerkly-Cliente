package com.example.kerklyv5.SQLite

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.example.kerklyv5.modelo.usuariosSqlite
import com.example.kerklyv5.ui.home.HomeFragment
import com.squareup.picasso.Picasso
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

class DataManager(context: Context) {
    val context: Context = context
    private val databaseHelper: DatabaseHelper = DatabaseHelper(context)

private fun insertUser(id: Int, palabrasClaves: String,nombreOfi: String) {
        val values = ContentValues()
        values.put(DatabaseHelper.COLUMN_ID,id)
        values.put(DatabaseHelper.COLUMN_PC, palabrasClaves)
        values.put(DatabaseHelper.COLUMN_NAME, nombreOfi)

        val db = databaseHelper.writableDatabase
        db.insert(DatabaseHelper.TABLE_NAME, null, values)
        db.close()
    }

    fun insertOrUpdateOficio(oficio: MisOficios, palabrasClaves: String,nombreOfi: String) {
        val isUsuarioTableExists = databaseHelper.isTableExists(DatabaseHelper.TABLE_NAME)
        if (isUsuarioTableExists) {
            val db = databaseHelper.writableDatabase
            // Consulta para verificar si ya existe un registro con el mismo valor de id
            val query =
                "SELECT * FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_ID} = ?"
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
                println("no se encuentra el oficio")
                insertUser(oficio.id, palabrasClaves, nombreOfi)
            }

            cursor.close()
            db.close()
        }else{
            println("no existe la tabla oficio")
            databaseHelper.onCreate(databaseHelper.writableDatabase) // Crear la tabla
            insertUser(oficio.id, palabrasClaves, nombreOfi)
           /* val homeFragment = HomeFragment()
            homeFragment.dataManager = DataManager(context)
            homeFragment.obtenerOficiosDB()*/
        }
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
    fun deleteAllTablas() {
        val db = databaseHelper.writableDatabase
        try {
            db.beginTransaction() // Comenzar una transacción
            db.delete(DatabaseHelper.TABLE_NAME, null, null) // Eliminar todos los registros de la tabla "Oficio"
            db.delete(DatabaseHelper.TABLE_NAME_USUARIOS, null, null) // Eliminar todos los registros de la tabla "Usuario"
            db.setTransactionSuccessful() // Marcar la transacción como exitosa
            println("Tablas eliminadas")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction() // Finalizar la transacción, independientemente de si fue exitosa o no
            db.close()
        }
    }


    fun verificarSiElUsarioExiste(context: Context, imageView: ImageView,txt_nombre: TextView, txt_correo: TextView,fotoByteArray: ByteArray, usuario: usuariosSqlite, telefono: String, nombre: String, apellidoPa:String, apellidoMa:String, correo: String){
        val isUsuarioTableExists = databaseHelper.isTableExists(DatabaseHelper.TABLE_NAME_USUARIOS)
        if (isUsuarioTableExists) {
            // La tabla "Usuario" existe en la base de datos
            println("La tabla \"Usuario\" existe en la base de datos ${usuario.uid} $isUsuarioTableExists")
            val db = databaseHelper.writableDatabase
            // Consulta para verificar si ya existe un registro con el mismo valor de id
            //val query = "SELECT * FROM ${DatabaseHelper.TABLE_NAME_USUARIOS} WHERE ${DatabaseHelper.COLUMN_ID_USUARIOS} = ?"
            val query = "SELECT * FROM ${DatabaseHelper.TABLE_NAME_USUARIOS} WHERE ${DatabaseHelper.COLUMN_ID_USUARIOS} = ?"
            val cursor = db.rawQuery(query, arrayOf(usuario.uid.toString()))
            if (cursor.moveToFirst()) {
                println("el usuario si se encuentra")
                val datos = DatosDelUsuario(context)
                for (usuario in datos) {
                    txt_nombre.text = "${usuario.nombre}. ${usuario.apellidoPa} ${usuario.apellidoMa}"
                    txt_correo.text = correo
                    val urlString: String = "data:image/jpeg;base64," + Base64.encodeToString(fotoByteArray, Base64.DEFAULT)
                    cargarImagen(urlString,context,imageView)
                }

            } else {
                InsertarDatosDelUsuario(telefono,fotoByteArray,nombre,apellidoPa,apellidoMa,correo)
                val datos = DatosDelUsuario(context)
                for (usuario in datos) {
                    txt_nombre.text = "${usuario.nombre}. ${usuario.apellidoPa} ${usuario.apellidoMa}"
                    txt_correo.text = correo
                    val urlString: String = "data:image/jpeg;base64," + Base64.encodeToString(fotoByteArray, Base64.DEFAULT)
                    cargarImagen(urlString,context,imageView)
                }
                //println("el usuario no se encuentra")

            }
            cursor.close()
            db.close()
        } else {
            // La tabla "Usuario" no existe en la base de datos
          //  println("La tabla \"Usuario\" no existe en la base de datos")
            databaseHelper.onCreate(databaseHelper.writableDatabase) // Crear la tabla
            InsertarDatosDelUsuario(telefono, fotoByteArray, nombre, apellidoPa, apellidoMa, correo)

        }

    }
    private fun InsertarDatosDelUsuario(telefono: String, foto: ByteArray, nombre: String,apellidoPa:String,apellidoMa:String,correo: String){
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

    fun mostrarInformacion(context: Context, imageView: ImageView,txt_nombre: TextView,txt_correo: TextView){
        val datos = DatosDelUsuario(context)
        println("informacion ${datos.size}")
        for (usuario in datos) {
            txt_nombre.text = "${usuario.nombre}. ${usuario.apellidoPa} ${usuario.apellidoMa}"
            txt_correo.text = usuario.correo
            val urlString: String = "data:image/jpeg;base64," + Base64.encodeToString(usuario.foto, Base64.DEFAULT)
            cargarImagen(urlString,context,imageView)
        }
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
            val uid = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID_USUARIOS))
            val Telefono = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_TELEFONO))
            val foto = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.COLUMN_FOTO))
            val NOMBRE = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NOMBRE))
            val ApellidoPa = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_APELIIDO_PA))
            val ApellidoMa = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_APELIIDO_MA))
            val correo = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CORREO))

            val usuarios = usuariosSqlite(uid,Telefono, foto, NOMBRE, ApellidoPa, ApellidoMa, correo)
                        datosUsuario.add(usuarios)
        }
        return datosUsuario as ArrayList<usuariosSqlite>
    }

   /* fun obtenerDatosUsuario(){
        val datos = dataManager.DatosDelUsuario(this)
        if (datos.size ==null) {
            // println("datos ${datos.size}")
            showToast("Base de datos vacia")
        }else{
            for (usuario in datos) {
                val idTelefono = usuario.telefono
                val fotoByteArray = usuario.foto
                val nombre = usuario.nombre
                val apellidoPa = usuario.apellidoPa
                val apellidoMa = usuario.apellidoMa
                val correo = usuario.correo

                txt_nombre.text = "$nombre $apellidoPa $apellidoMa"
                txt_correo.text = correo
                val urlString: String =
                    "data:image/jpeg;base64," + Base64.encodeToString(fotoByteArray, Base64.DEFAULT)
                cargarImagen(urlString)
            }
        }
    }*/

    private fun cargarImagen(urlImagen: String,context: Context,fotoPerfil: ImageView) {
        val file: Uri
        file = Uri.parse(urlImagen)
        System.out.println("imagen aqui: "+ file)
        Picasso.get().load(urlImagen).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {

            }
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                val multi = MultiTransformation<Bitmap>(RoundedCornersTransformation(128, 0, RoundedCornersTransformation.CornerType.ALL))
                Glide.with(context).load(file)
                    .apply(RequestOptions.bitmapTransform(multi))
                    .into(fotoPerfil)
               // setProgressDialog.dialog.dismiss()
            }
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                System.out.println("Respuesta error 3 "+ e.toString())
              //  setProgressDialog.dialog.dismiss()
                //Toast.makeText(this@SolicitarServicio, "si hay foto respuesta 3", Toast.LENGTH_SHORT).show()
            }

        })
    }
}