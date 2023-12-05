package com.example.kerklyv5.BaseDatosEspacial

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class conexionPostgreSQL {
    var conexion: Connection? = null
    @SuppressLint("SuspiciousIndentation")
    fun obtenerConexion(context: Context) :Connection?{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            val threadPolicy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(threadPolicy)
        }
        try {
            Class.forName("org.postgresql.Driver") // Cargar el driver JDBC
            val host = "6.tcp.ngrok.io"
            val port = "16646"
            val databaseName = "Kerkly"
            val username = "luis_admin"
            val password = "Lu0599@"
            val url = "jdbc:postgresql://$host:$port/$databaseName"
             conexion = DriverManager.getConnection(url, username, password)
            //Toast.makeText(context, "Conexión exitosa", Toast.LENGTH_SHORT).show()
            return conexion
        }catch (e: SQLException){
            println(e.message)
            e.printStackTrace()
            conexion = null
        }
        return null
    }
    fun cerrarConexion(){
        conexion!!.close()
    }
    //metodo que mediante un un poligono circular creado. determina sobre que secciones pertenece
    fun poligonoCircular(latitud: Double,longitud: Double,radio: Double): ArrayList<geom>{
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
                }
            }

        }
        return listaDeSecciones
    }

    fun Los5KerklyMasCercanos(
        idPoligonosUsuario: ArrayList<geom>,
        longitudUsuario: Double,
        latitudUsuario: Double,
        oficio: String
    ): MutableList<Kerkly>? {
        val kerklysCercanos = mutableListOf<Kerkly>()
        val latitudUsuario  =17.544556
            val longitudUsuario = -99.497408

        for (idPoligono in idPoligonosUsuario) {
            val consultaKerklySQL =
                "SELECT \"curp\", \"uidKerkly\",\n" +
                        "    ST_X(ubicacion::geometry)  AS longitud,\n" +
                        "    ST_Y(ubicacion::geometry)  AS latitud,\n" +
                        "    ST_Distance(ubicacion::geometry, ST_SetSRID(ST_Point(?, ?)::geometry, 4326)) AS distancia \n" +
                        "FROM \"KerklyEnMovimiento\"\n" +
                        "INNER JOIN \"oficios_kerklyM\"  ON \"curp\" = \"id_kerklyK\"\n" +
                        "INNER JOIN \"Oficios\" ON \"id_oficioK\"::numeric = \"idOficio\"::numeric\n" +
                        "WHERE \"IdPoligono\" = ? AND \"nombreO\" = ? AND \"on\" = ?\n"
            "ORDER BY distancia ASC\n" +
                    "LIMIT 5"



            val preparedStatement = conexion?.prepareStatement(consultaKerklySQL)
            preparedStatement?.setDouble(1, longitudUsuario)
            preparedStatement?.setDouble(2, latitudUsuario)
            preparedStatement?.setInt(3, idPoligono.id_0)
            preparedStatement?.setString(4, oficio)
            preparedStatement?.setBoolean(5,true)

            val resultSetKerkly = preparedStatement?.executeQuery()
            // Verificar si no se encontraron resultados
            if (resultSetKerkly != null && resultSetKerkly.next()) {
                do {
                    val idKerkly = resultSetKerkly.getString("curp")
                    val uidKerkly = resultSetKerkly.getString("uidKerkly")
                    val distancia = resultSetKerkly.getDouble("distancia").toString()
                    val longitud = resultSetKerkly.getDouble("longitud")
                    val latitud = resultSetKerkly.getDouble("latitud")

                    println("idpoligono ${idPoligono.id_0}")
                    println("distancia $uidKerkly $idKerkly $distancia  $latitud $longitud")

                    val kerkly = Kerkly(idKerkly, uidKerkly, distancia, latitud, longitud)
                    kerklysCercanos.add(kerkly)
                } while (resultSetKerkly.next())
            } else {
                println("idpoligono ${idPoligono.id_0}")
                println("No se encontraron Kerklys cercanos para el polígono y oficio dados.")

            }
        }

        // Devolver null si no se encontraron Kerklys cercanos
        return  kerklysCercanos
    }



}