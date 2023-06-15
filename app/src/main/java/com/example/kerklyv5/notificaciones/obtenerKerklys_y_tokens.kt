package com.example.kerklyv5.notificaciones

import android.content.Context
import com.example.kerklyv5.modelo.usuarios
import com.google.firebase.database.*


class obtenerKerklys_y_tokens {
    private lateinit var firebaseDatabaseUsu: FirebaseDatabase
    private lateinit var databaseUsu: DatabaseReference
    private lateinit var token: String

      fun obtenerTokenKerkly(telefoK: String, problema: String, nombreCliente: String, context: Context){
        val llamartopico = llamarTopico()
        firebaseDatabaseUsu = FirebaseDatabase.getInstance()
        databaseUsu = firebaseDatabaseUsu.getReference("UsuariosR").child(telefoK).child("MisDatos")
        databaseUsu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val u2 = snapshot.getValue(usuarios::class.java)
                 token = u2!!.token
                System.out.println("el token del kerkly " + token)
                llamartopico.llamartopico(context, token, "(Servicio Normal) $problema", "Usuario--> $nombreCliente")
            }

            override fun onCancelled(error: DatabaseError) {
                System.out.println("Firebase: $error")
            }

        })

    }
}
