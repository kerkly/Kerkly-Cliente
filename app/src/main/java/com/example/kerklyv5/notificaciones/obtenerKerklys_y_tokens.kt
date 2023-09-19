package com.example.kerklyv5.notificaciones

import android.content.Context
import com.example.kerklyv5.modelo.usuarios
import com.example.kerklyv5.url.Instancias
import com.google.firebase.database.*


class obtenerKerklys_y_tokens {
    private lateinit var token: String
    private lateinit var instancias: Instancias

      fun obtenerTokenKerkly(uid: String, problema: String, nombreCliente: String, context: Context){
        val llamartopico = llamarTopico()
          instancias = Instancias()
          val databaseUsu = instancias.referenciaInformacionDelKerkly(uid)
        databaseUsu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null){

                }else{
                    val u2 = snapshot.getValue(usuarios::class.java)
                    token = u2!!.token
                    System.out.println("el token del kerkly " +token)
                    llamartopico.llamartopico(context, token, "(Servicio Urgente) $problema", "Mensaje de $nombreCliente")
                }

            }

            override fun onCancelled(error: DatabaseError) {
                System.out.println("Firebase: $error")
            }

        })

    }
}
