package com.example.kerklyv5.BaseDatosEspacial

import android.widget.EditText
import android.widget.TextView
import org.postgresql.util.PGobject

class consultas {
    lateinit var conexionPostgreSQL: conexionPostgreSQL
    fun obtenerSeccionKekrly(editText: EditText, textView: TextView){
        conexionPostgreSQL = conexionPostgreSQL()
        val curp = editText.text.toString()
        conexionPostgreSQL.conexion!!.createStatement().use { stmt ->
            //  val query = "SELECT * FROM public.kerkly"
            val query = "select \"curp\", \"id_0\"  from kerkly  inner join \"poligonoChilpo\" on id_0 = \"idPoligono\"where \"curp\" ='$curp'"
            stmt.executeQuery(query).use { rs ->
                while (rs.next()) {
                    val idKerkly = rs.getString("curp")
                    val idpoligono = rs.getInt("id_0")
                    println(idKerkly)
                    println(idpoligono)
                    textView.text = "idKerkly: $idKerkly \n idSeccion: $idpoligono"
                }
            }
        }
    }

    fun ObtenerDatosGeom(textView: TextView, longitud: Double, latitud: Double): String{
        conexionPostgreSQL = conexionPostgreSQL()
        var geom:String = ""
        conexionPostgreSQL.conexion!!.createStatement().use { stmt ->
            //  val query = "SELECT * FROM public.kerkly"
            val query = "SELECT ST_AsText(geom) FROM \"poligonoChilpo\" WHERE ST_Contains(geom, ST_SetSRID(ST_MakePoint($longitud,$latitud), 4326))"
            stmt.executeQuery(query).use { rs ->
                while (rs.next()) {
                    //  idpoligono = rs.getInt("id_0").toString()
                    geom = rs.getString("st_astext")
                    println(geom)
                    //  println(idpoligono)
                    //  textView.text = "idSeccion: $idpoligono \n $geom "
                    return geom
                }
            }
        }
        return geom
    }
    fun ObtenerSeccionCoordenadas(longitud: Double, latitud: Double): Int{
        conexionPostgreSQL = conexionPostgreSQL()
        var idSeccion:Int=0
        conexionPostgreSQL.conexion!!.createStatement().use { stmt ->
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

    fun crearTodosLosPoligonos(): ArrayList<String>{
        conexionPostgreSQL = conexionPostgreSQL()
        var ListaDePoligonos :ArrayList<String> = ArrayList()
        conexionPostgreSQL.conexion!!.createStatement().use { stmt ->
            //  val query = "SELECT * FROM public.kerkly"
            /*ST_AsText es una función utilizada en sistemas de bases de datos geoespaciales,
             como PostGIS en PostgreSQL, para convertir objetos geométricos en su representación
              en formato de texto legible por humanos. Esta función toma una geometría como entrada
              y la devuelve en forma de cadena de texto en un formato específico, generalmente en el
               formato Well-Known Text (WKT) o en otro formato de texto geoespacial.
             */
            val query = "SELECT ST_AsText(geom) FROM \"poligonoChilpo\""
            stmt.executeQuery(query).use { rs ->
                while (rs.next()) {
                    val geomText = rs.getString("st_astext")
                    ListaDePoligonos.add(geomText)
                    // println("mi lista "+ListaDePoligonos)
                    //  return ListaDePoligonos
                }
            }
        }
        return ListaDePoligonos
    }

    //metodo que mediante un un poligono circular creado. determina sobre que secciones pertenece
    fun poligonoCircular(latitud: Double,longitud: Double,radio: Double): ArrayList<geom>{
        conexionPostgreSQL = conexionPostgreSQL()
        var listaDeSecciones :ArrayList<geom> = ArrayList()
        conexionPostgreSQL.conexion!!.createStatement().use { stmt ->
            /* ST_Intersects es una función utilizada en sistemas de bases de datos geoespaciales,
            como PostGIS en PostgreSQL, para determinar si dos objetos geométricos se intersectan
            en el espacio. En otras palabras, verifica si dos geometrías tienen algún punto en común
             o se superponen de alguna manera.
             */

            /*ST_Buffer es una función utilizada en sistemas de bases de datos geoespaciales,
            como PostGIS en PostgreSQL, para generar un nuevo objeto geométrico que representa
            el área resultante de expandir o "bufar" una geometría existente alrededor de sus
            límites. La expansión se realiza creando un nuevo polígono que abarca una distancia
             específica desde los bordes de la geometría original.
             */

            /*ST_MakePoint es una función utilizada en sistemas de bases de datos geoespaciales,
             como PostGIS en PostgreSQL, para crear un objeto geométrico de tipo punto en un espacio
             bidimensional o tridimensional. Esta función permite definir las coordenadas del punto
             directamente en la llamada a la función.
             */

            /*La palabra clave "geography" se refiere a un tipo de dato utilizado en sistemas de bases
             de datos geoespaciales para representar datos geográficos en la superficie de la Tierra,
              teniendo en cuenta la forma esférica del planeta. A diferencia de los tipos de datos geométricos
              tradicionales, que trabajan en un plano cartesiano, los datos geográficos consideran la curvatura
               de la Tierra y permiten realizar cálculos precisos de distancia, área y otros aspectos
               en un contexto geoespacial.
             */
            val query = "Select id_0, geom from \"poligonoChilpo\" where ST_Intersects(geom, ST_Buffer(ST_MakePoint($longitud,$latitud)::geography,$radio))"
            stmt.executeQuery(query).use { rs ->
                while (rs.next()) {
                    var id_0 = rs.getInt("id_0")
                    var geom = rs.getString("geom")
                    var wktGeometry = geom
                    var pgObject = PGobject()
                    pgObject.type = "geometry"
                    pgObject.value = wktGeometry

                    var statement = conexionPostgreSQL.conexion!!.prepareStatement("SELECT ST_AsText(?)")
                    statement.setObject(1, pgObject)

                    var resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        var geomText = resultSet.getString(1)
                        println("Geometry as text: $geomText")
                        var g =  geom(id_0,geomText)
                        listaDeSecciones.add(g)
                    }


                }
            }
        }
        return listaDeSecciones
    }
}