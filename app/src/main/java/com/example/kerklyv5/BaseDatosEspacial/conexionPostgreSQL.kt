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
            val host = "6.tcp.ngrok.io"
            val port = "10123"
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

    fun ObtenerSeccionCoordenadas(longitud: Double, latitud: Double): Int{
        var idSeccion:Int=0
        conexion!!.createStatement().use { stmt ->
            val query = "SELECT * FROM \"poligonoChilpo\" WHERE ST_Contains(geom, ST_SetSRID(ST_MakePoint($longitud,$latitud), 4326))"
            stmt.executeQuery(query).use { rs ->
                while (rs.next()) {
                    idSeccion = rs.getInt("id_0")
                    // idSeccion = rs.getString("st_astext")
                    println(idSeccion)
                    //  println(idpoligono)
                    //  textView.text = "idSeccion: $idpoligono \n $geom "
                    return idSeccion.toInt()
                }
            }
        }
        return idSeccion
    }


}