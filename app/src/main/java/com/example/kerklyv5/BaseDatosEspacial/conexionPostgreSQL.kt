package com.example.kerklyv5.BaseDatosEspacial

import android.annotation.SuppressLint
import android.content.Context
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.kerklyv5.PoligonoCircularCallback
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
            val host = "8.tcp.ngrok.io"
            val port = "18836"
            val databaseName = "Kerkly"
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


    //metodo que mediante un un poligono circular creado. determina sobre que secciones pertenece
    fun poligonoCircular(latitud: Double,longitud: Double,radio: Double): ArrayList<geom>{
      // val latitud = "17.520514"
     //  val longitud = "-99.463207"

        var listaDeSecciones :ArrayList<geom> = ArrayList()
        // Notificar que la consulta se ha iniciado
        conexion!!.createStatement().use { stmt ->
            val query = "Select id_0, geom from \"poligonoChilpo\" where ST_Intersects(geom, ST_Buffer(ST_MakePoint($longitud,$latitud)::geography,$radio))"
            stmt.executeQuery(query).use { rs ->
                while (rs.next()) {
                    var id_0 = rs.getInt("id_0")
                    var geom = rs.getString("geom")
                    var g =  geom(id_0,geom)
                    listaDeSecciones.add(g)
                    /*var wktGeometry = geom
                    var pgObject = PGobject()
                    pgObject.type = "geometry"
                    pgObject.value = wktGeometry

                    var statement = conexion!!.prepareStatement("SELECT ST_AsText(?)")
                    statement.setObject(1, pgObject)

                    var resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        var geomText = resultSet.getString(1)
                        println("Geometry as text: $geomText")
                        var g =  geom(id_0,geomText)
                        listaDeSecciones.add(g)
                    }*/
                }
            }

        }
        return listaDeSecciones
    }


    /*fun kerklyMasCercano(idPoligonosUsuario: ArrayList<geom>, longitudUsuario: Double, latitudUsuario: Double):  List<Kerkly> {
        var distanciaMasCercana = Double.MAX_VALUE
        val kerklysCercanos = mutableListOf<Kerkly>()

        for (idPoligono in idPoligonosUsuario) {
            val consultaKerklySQL = "SELECT \"curp\",\"uidKerkly\", ST_Distance(ubicacion, ST_Point(?, ?)) AS distancia " +
                    "FROM \"KerklyEnMovimiento\" WHERE \"IdPoligono\" = ? ORDER BY distancia LIMIT 1"

            val preparedStatement = conexion?.prepareStatement(consultaKerklySQL)
            preparedStatement?.setDouble(1, longitudUsuario)
            preparedStatement?.setDouble(2, latitudUsuario)
            preparedStatement?.setInt(3, idPoligono.id_0)

            val resultSetKerkly = preparedStatement?.executeQuery()

            if (resultSetKerkly?.next() == true) {
                val idKerkly = resultSetKerkly.getString("curp")
                val uidKerkly = resultSetKerkly.getString("uidKerkly")
                val distancia = resultSetKerkly.getDouble("distancia")

                if (distancia < distanciaMasCercana) {
                    distanciaMasCercana = distancia
                    val kerkly = Kerkly(idKerkly, uidKerkly, distancia)
                    kerklysCercanos.add(kerkly)
                }
            }
        }

        // kerklyMasCercano contendrá el Kerkly más cercano al usuario
        return kerklysCercanos
    }*/


    fun Los5KerklyMasCercanos(idPoligonosUsuario: ArrayList<geom>, longitudUsuario: Double, latitudUsuario: Double,oficio: String): List<Kerkly> {
     //  val latitudUsuario =  17.520514
      //  val longitudUsuario = -99.463207
        println("lat $latitudUsuario")
        println("lon $longitudUsuario")
        val kerklysCercanos = mutableListOf<Kerkly>()

        for (idPoligono in idPoligonosUsuario) {
            val consultaKerklySQL = "SELECT \"curp\", \"uidKerkly\", ST_X(ubicacion::geometry) AS longitud, ST_Y(ubicacion::geometry) AS latitud, \n" +
                    "ST_Distance(ubicacion::geometry, ST_SetSRID(ST_Point(?, ?)::geometry, 4326)) AS distancia\n" +
                    "FROM \"oficio_kerkly\"\n" +
                    "INNER JOIN \"Oficios\" ON \"Oficios\".\"idOficio\"::numeric = \"oficio_kerkly\".\"idOficioK\"::numeric\n" +
                    "INNER JOIN \"KerklyEnMovimiento\" ON \"KerklyEnMovimiento\".\"curp\" = \"oficio_kerkly\".\"idKerklyK\"\n" +
                    "WHERE \"IdPoligono\" = ?\n" +
                    "AND \"Oficios\".\"nombreO\" = ?\n" +
                    "ORDER BY distancia ASC\n" +
                    "LIMIT 5"

            val preparedStatement = conexion?.prepareStatement(consultaKerklySQL)
            preparedStatement?.setDouble(1, longitudUsuario)
            preparedStatement?.setDouble(2, latitudUsuario)
            preparedStatement?.setInt(3, idPoligono.id_0)
            preparedStatement?.setString(4,oficio.toString())

            val resultSetKerkly = preparedStatement?.executeQuery()

            while (resultSetKerkly?.next() == true) {
                val idKerkly = resultSetKerkly.getString("curp")
                val uidKerkly = resultSetKerkly.getString("uidKerkly")
                val distancia = resultSetKerkly.getDouble("distancia").toString()
                val longitud = resultSetKerkly.getDouble("longitud")
                val latitud = resultSetKerkly.getDouble("latitud")
                println("distancia $uidKerkly $idKerkly $distancia  $latitud $longitud")
                // Obtener las coordenadas como un String en formato "POINT(longitud latitud)"
               // val coordenadasStr = resultSetKerkly.getString("coordenadas")
                // Extraer las coordenadas de la cadena
                val kerkly = Kerkly(idKerkly, uidKerkly, distancia, latitud, longitud)
                kerklysCercanos.add(kerkly)
            }
        }

        // kerklysCercanos contendrá los 5 Kerklys más cercanos al usuario
        return kerklysCercanos
    }


}