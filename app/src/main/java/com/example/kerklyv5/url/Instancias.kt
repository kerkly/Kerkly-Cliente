package com.example.kerklyv5.url

import android.annotation.SuppressLint
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class Instancias() {
    val database = FirebaseDatabase.getInstance()
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    private val usuario = "UsuariosR"
    private val cliente = "clientes"
    private val kerkly = "kerkly"
    private val misDatos = "MisDatos"
    private val SolicitudUrgente ="SolicitudUrgente"
    private val SolicitudNormal ="SolicitudNormal"
    private val listaUsuarios = "Lista de Usuarios"
    private val chats = "chats"

    fun referenciaInformacionDelUsuario(id: String): DatabaseReference{
        val databaseReferenceMisDatos = database.getReference(usuario).child(cliente).child(id).child(misDatos)
        return databaseReferenceMisDatos
    }
    fun referenciaInformacionDelKerkly(id: String): DatabaseReference{
        val databaseReferenceMisDatos = database.getReference(usuario).child(kerkly).child(id).child(misDatos)
        return databaseReferenceMisDatos
    }

    fun referenciaSolicitudUrgente(id: String):DatabaseReference{
        val databaseReference = database.getReference(usuario).child(cliente).child(id).child(SolicitudUrgente)
        return databaseReference
    }
    fun referenciaSolicitudNormal(id: String):DatabaseReference{
        val databaseReference = database.getReference(usuario).child(cliente).child(id).child(SolicitudNormal)
        return databaseReference
    }
    fun CalcularDistancia(latitud:Double,longitud:Double,latitudFinal:Double,longitudFinal:Double):String{
        val url2 = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$latitud,$longitud&destinations=$latitudFinal,$longitudFinal&mode=driving&language=fr-FR&avoid=tolls&key=AIzaSyD9i-yAGqAoYnIcm8KcMeZ0nsHyiQxl_mo"
        return url2
    }

    fun referenciaListaDeUsuarios(id: String):  DatabaseReference{
        val databaseReference = database.getReference(usuario).child(cliente).child(id).child(listaUsuarios)
        return databaseReference
    }

    @SuppressLint("SuspiciousIndentation")
    fun referenciaChatscliente(idCliente: String, idKerkly:String): DatabaseReference{
      val databaseReference =  database.getReference(usuario).child(cliente).child(idCliente).child(chats)
            .child("$idCliente"+"_"+"$idKerkly")
        return databaseReference
    }
    fun referenciaChatsKerkly(idKerkly: String,idCliente:String): DatabaseReference{
        val databaseReference =  database.getReference(usuario).child(kerkly).child(idKerkly).child(chats)
            .child("$idKerkly"+"_"+"$idCliente")
        return databaseReference
    }

    fun StorageReference(idKerkly: String,idCliente:String,Nombrearchivo: String): StorageReference{
        val ref =  storageRef.child(usuario).child(kerkly).child(idKerkly).child(chats).child("$idKerkly"+"_"+"$idCliente").child(Nombrearchivo)
        return ref
    }
    fun EnviarArchivoStorageReference(idCliente: String,idKerkly:String,Nombrearchivo: String): StorageReference{
        val ref =  storageRef.child(usuario).child(cliente).child(idCliente).child(chats).child("$idCliente"+"_"+"$idKerkly").child(Nombrearchivo)
        return ref
    }
}