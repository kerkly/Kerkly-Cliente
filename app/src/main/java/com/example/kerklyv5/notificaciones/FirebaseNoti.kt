package com.example.kerklyv5.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.kerklyv5.MainActivityChats
import com.example.kerklyv5.R
import com.example.kerklyv5.vista.PantallaInicio
import com.example.kerklyv5.vista.fragmentos.MainActivityMostrarSolicitudes
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException
import java.util.*

class FirebaseNoti: FirebaseMessagingService() {

    lateinit var token1: String
    lateinit var m : RemoteMessage
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        token1 = token
        Log.e("token", "mi token es:$token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val from: String = message.getFrom()!!
        m = message
        if (message.getData().size > 0) {
            val titulo: String = message.getData().get("titulo")!!
            val detalle: String = message.getData().get("detalle")!!
            val tipoNoti: String = message.getData().get("tipoNoti")!!
            //print("aquii---> $tipoNoti")
            if (tipoNoti == "chats"){
                val nombreKerkly: String = message.getData().get("nombreCompletoK")!!
                val nombreCliente: String = message.getData().get("nombreCompletoCliente")!!
                val telefonoKerkly: String = message.getData().get("telefonok")!!
                val telefonoCliente: String = message.getData().get("telefonoCliente")!!
                val fotoKerkly: String = message.getData().get("urlFotoKerkly")!!
                val fotoCliente: String = message.getData().get("urlFotoCliente")!!
                val tokenKerkly: String = message.getData().get("tokenKerkly")!!
                val tokenCliente: String = message.getData().get("tokenCliente")!!
                val uidCliente: String = message.getData().get("uidCliente")!!
                val uidKerkly: String = message.getData().get("uidKerkly")!!


                TraerNotificacion(titulo, detalle,nombreKerkly,nombreCliente,telefonoKerkly,telefonoCliente
                    ,fotoKerkly,fotoCliente,tokenKerkly,tokenCliente,uidCliente,uidKerkly)
            }
            if (tipoNoti =="llamarTopicSolicitud"){
                val tipoSolicitud = message.getData().get("TipoDeSolicitud")
                val telefonoCliente = message.getData().get("Telefono")
                val nombreCliente = message.getData().get("nombreCompletoCliente")
                val uidCliente = message.getData().get("uidCliente")
                TraerNotificacionSolicitudAceptada(titulo, detalle,tipoSolicitud,telefonoCliente,nombreCliente,uidCliente)
            }
        }
    }

    private fun TraerNotificacionSolicitudAceptada(
        titulo: String,
        detalle: String,
        tipoSolicitud: String?,
        telefonoCliente: String?,
        nombreCliente: String?,
        uidCliente: String?
    ) {
        val id = tipoSolicitud
        //val id2 = id.hashCode()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, id.toString())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nc = NotificationChannel(id, "solicitudAceptada", NotificationManager.IMPORTANCE_HIGH)
            nc.setShowBadge(true)
            assert(nm != null)
            nm!!.createNotificationChannel(nc)
        }
        try {
            builder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(titulo)
                .setSmallIcon(R.drawable.archivos)
                .setContentText(detalle)
                .setContentIntent(clicknotiSolicitudAceptada(tipoSolicitud, telefonoCliente
                    , nombreCliente,uidCliente))
                .setContentInfo("nuevo")
             val random = Random()
            val idNotity = random.nextInt(1000)
            assert(nm != null)
            nm!!.notify(idNotity, builder.build())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun clicknotiSolicitudAceptada(tipoSolicitud: String?, telefonoCliente: String?, nombreCliente: String?, uidCliente: String?): PendingIntent? {
        val nf = Intent(applicationContext, MainActivityMostrarSolicitudes::class.java)
        nf.putExtra("TipoDeSolicitud", tipoSolicitud)
        nf.putExtra("Telefono", telefonoCliente)
        nf.putExtra("nombreCompletoCliente", nombreCliente)
        nf.putExtra("uidCliente",uidCliente)
        nf.putExtra("Noti", "Noti")

        nf.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val flags = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getActivity(this, 0, nf, flags)
    }

    private fun TraerNotificacion(titulo: String, detalle: String, nombreKerkly: String,
                                  nombreCliente: String, telefonoKerkly: String,telefonoCliente:String,
                                  fotoKerkly: String, fotoCliente:String, tokenKerkly: String,
                                  tokenCliente: String, uidCliente: String, uidKerkly: String) {
        val id = "mensajeCliente"
        val id2 = id.hashCode()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nc = NotificationChannel(id, "nuevoCliente", NotificationManager.IMPORTANCE_HIGH)
            nc.setShowBadge(true)
            assert(nm != null)
            nm!!.createNotificationChannel(nc)
        }
        try {
            builder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(titulo)
                .setSmallIcon(R.drawable.archivos)
                .setContentText(detalle)
                .setContentIntent(clicknoti(nombreKerkly, nombreCliente
                    , telefonoKerkly,telefonoCliente, fotoKerkly,fotoCliente, tokenKerkly, tokenCliente
                    , uidCliente, uidKerkly))
                .setContentInfo("nuevo")
            // val random = Random()
            //val idNotity = random.nextInt(1000)
            assert(nm != null)
            nm!!.notify(id2, builder.build())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun clicknoti(nombreKerkly: String, nombreCliente: String
                          , telefonoKerkly: String,telefonoCliente: String, fotoKerkly: String,fotoCliente: String, tokenKerkly: String, tokenCliente: String
                          , uidCliente: String, uidKerkly: String): PendingIntent? {

        val nf = Intent(applicationContext, MainActivityChats::class.java)
        //val pendingIntent : PendingIntent = PendingIntent.getActivity(applicationContext, 0, nf, flags)
        //nf.putExtra("notificacion", "chat")
        // Agrega otros datos que desees pasar a la actividad de chat
        // println("aqui 99---->ker $nombreKerkly clien $nombreCliente telek $telefonoKerkly teleC $telefonoCliente " +
        //  "urlC $fotoKerkly uid c $uidCliente kerkly $uidKerkly  token k $tokenKerkly token c $tokenCliente")
        nf.putExtra("nombreCompletoK", nombreKerkly)
        nf.putExtra("nombreCompletoCliente", nombreCliente)
        nf.putExtra("telefonok", telefonoKerkly)
        nf.putExtra("telefonoCliente",telefonoCliente)
        nf.putExtra("urlFotoKerkly", fotoKerkly)
        nf.putExtra("urlFotoCliente", fotoCliente)
        nf.putExtra("tokenKerkly", tokenKerkly)
        nf.putExtra("tokenCliente", tokenCliente)
        nf.putExtra("uidCliente", uidCliente)
        nf.putExtra("uidKerkly", uidKerkly)
        nf.putExtra("Noti", "Noti")

        println("url cliente $fotoCliente")

        println("url kerkly $fotoKerkly")

        nf.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val flags = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getActivity(this, 0, nf, flags)

    }

    override fun onDestroy() {
        super.onDestroy()
        onMessageReceived(m)
    }

}