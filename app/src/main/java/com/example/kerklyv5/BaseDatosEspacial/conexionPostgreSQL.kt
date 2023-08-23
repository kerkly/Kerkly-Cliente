package com.example.kerklyv5.BaseDatosEspacial

import android.annotation.SuppressLint
import android.content.Context
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.postgresql.util.PGobject
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException


class conexionPostgreSQL {
    var conexion: Connection? = null
    @SuppressLint("SuspiciousIndentation")
    fun obtenerConexion(context: Context) {
        val threadPolicy: ThreadPolicy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(threadPolicy)
        try {
            Class.forName("org.postgresql.Driver") // Cargar el driver JDBC
            val host = "4.tcp.ngrok.io"
            val port = "16212"
            val databaseName = "kerkly"
            val username = "luis_admin"
            val password = "Lu0599@"
            val url = "jdbc:postgresql://$host:$port/$databaseName"

             conexion = DriverManager.getConnection(url, username, password)
            //Toast.makeText(context, "Conexión exitosa", Toast.LENGTH_SHORT).show()

        }catch (e: SQLException){
            println(e.message)
            e.printStackTrace()
        }
    }
    fun cerrarConexion(){
        conexion!!.close()
    }


}