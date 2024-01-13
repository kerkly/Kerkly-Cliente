package com.example.kerklyv5.notificaciones

import android.content.Context
import com.example.kerklyv5.modelo.usuarios
import com.example.kerklyv5.modelo.usuariosKerkly
import com.example.kerklyv5.url.Instancias
import com.google.firebase.database.*


class obtenerKerklys_y_tokens {
    private lateinit var tokenKerkly: String
    private lateinit var instancias: Instancias

      fun obtenerTokenKerkly(uid: String, problema: String, nombreCliente: String, folio:String, context: Context, telefonoKerkly:String){
          val llamartopico = llamarTopico()
          instancias = Instancias()
          val databaseUsu = instancias.referenciaInformacionDelKerkly(uid)
        databaseUsu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null){

                }else{
                    val u2 = snapshot.getValue(usuarios::class.java)
                    tokenKerkly = u2!!.token
                    System.out.println("el token del kerkly " +tokenKerkly)
                   llamartopico.llamarTopicoAceptarSolicitud(context, tokenKerkly, "(Presupuesto Aceptado, Solicitud num. $folio $problema", "$nombreCliente", folio, telefonoKerkly)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                System.out.println("Firebase: $error")
            }

        })

    }

   /* fun obtenerTokenKerklySolicitudUrgente(uidKerkly:String,context: Context, Mensaje: String, Titulo: String,latitud:String,
                                           longitud:String, Folio:String,direccion:String,telefonoCliente:String,
                                           correoCliente:String, uidCliente:String,problema: String){

        val llamartopico = llamarTopico()
        instancias = Instancias()
        val databaseUsu = instancias.referenciaInformacionDelKerkly(uidKerkly)
        databaseUsu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null){
                    println("error null al obtener token")
                }else{
                    val u2 = snapshot.getValue(usuariosKerkly::class.java)
                    val curp = u2!!.curp
                    tokenKerkly = u2!!.token
                    val telefonoKerkly = u2!!.telefono
                    val nombreK = u2!!.nombre
                    val correok = u2!!.correo
                    llamartopico.llamarTopicEnviarSolicitudUrgente(context, tokenKerkly, "(Solicitud Urgente) $Mensaje", "Mensaje de $Titulo",
                        latitud,longitud, Folio, direccion, telefonoCliente, curp,telefonoKerkly, correoCliente,correok, nombreK, uidCliente,problema)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                System.out.println("Firebase: $error")
            }

        })

    }*/
}
