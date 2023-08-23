package com.example.kerklyv5.url

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Instancias() {
    val database = FirebaseDatabase.getInstance()
    private val usuario = "UsuariosR"
    private val cliente = "clientes"
    private val misDatos = "MisDatos"
    private val SolicitudUrgente ="SolicitudUrgente"

    fun referenciaInformacionDelUsuario(id: String): DatabaseReference{
        val databaseReferenceMisDatos = database.getReference(usuario).child(cliente).child(id).child(misDatos)
        return databaseReferenceMisDatos
    }

    fun referenciaSolicitudUrgente(id: String):DatabaseReference{
        val databaseReference = database.getReference(usuario).child(cliente).child(id).child(SolicitudUrgente)
        return databaseReference
    }

    fun CalcularDistancia(latitud:Double,longitud:Double,latitudFinal:Double,longitudFinal:Double):String{
        val url2 = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$latitud,$longitud&destinations=$latitudFinal,$longitudFinal&mode=driving&language=fr-FR&avoid=tolls&key=AIzaSyD9i-yAGqAoYnIcm8KcMeZ0nsHyiQxl_mo"
        return url2
    }

}