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
import com.example.kerklyv5.express.MensajesExpress
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

                // Imprimir cada valor
                println("titulo: $titulo")
                println("detalle: $detalle")
                println("nombreCompletoK: $titulo")
                println("nombreCompletoCliente: $nombreCliente")
                println("telefonok: $telefonoKerkly")
                println("telefonoCliente: $telefonoCliente")
                println("urlFotoKerkly: $fotoKerkly")
                println("urlFotoCliente: $fotoCliente")
                println("tokenKerkly: $tokenKerkly")
                println("tokenCliente: $tokenCliente")
                println("uidCliente: $uidCliente")
                println("uidKerkly: $uidKerkly")

                TraerNotificacion(titulo, detalle,nombreKerkly,nombreCliente,telefonoKerkly,telefonoCliente
                    ,fotoKerkly,fotoCliente,tokenKerkly,tokenCliente,uidCliente,uidKerkly)
            }
            if (tipoNoti =="llamarTopicAceptarSolicitudNormal"){
                val tipoSolicitud = message.getData().get("TipoDeSolicitud")
                val telefonoCliente = message.getData().get("Telefono")
                val nombreCliente = message.getData().get("nombreCompletoCliente")
                val uidCliente = message.getData().get("uidCliente")
                val folio = message.data.get("folio")
                val fecha = message.data.get("Fecha")
                val problema = message.data.get("Problema")
                val pagoTotal = message.data.get("pagoTotal")
                val oficio = message.data.get("Oficio")
                val telefonoKerkly = message.data.get("telefonoKerkly")
                val nombreKerkly = message.data.get("nombreCompletoKerkly")
                val direccionKerkly = message.data.get("direccionKerkly")
                val correoKerkly = message.data.get("correoKerkly")
                val uidKerkly = message.data.get("uidKerkly")
                TraerNotificacionSolicitudNormalAceptada(titulo, detalle,tipoSolicitud,telefonoCliente,
                    nombreCliente,uidCliente,
                    folio!!,fecha,problema,pagoTotal
                    ,oficio,telefonoKerkly,nombreKerkly,
                    direccionKerkly,correoKerkly,
                    uidKerkly!!
                )
            }
            if (tipoNoti == "llamarTopicAceptarSolicitudUrgente"){
                val tipoSolicitud = message.getData().get("TipoDeSolicitud")
                val telefonoCliente = message.getData().get("Telefono")
                val nombreCliente = message.getData().get("nombreCompletoCliente")
                val uidCliente = message.getData().get("uidCliente")
                val folio = message.data.get("folio")
                val fecha = message.data.get("Fecha")
                val problema = message.data.get("Problema")
                val pagoTotal = message.data.get("pagoTotal")
                val oficio = message.data.get("Oficio")
                val telefonoKerkly = message.data.get("telefonoKerkly")
                val nombreKerkly = message.data.get("nombreCompletoKerkly")
                val direccionKerkly = message.data.get("direccionKerkly")
                val correoKerkly = message.data.get("correoKerkly")
                val uidKerkly = message.data.get("uidKerkly")
                TraerNotificacionSolicitudUrgenteAceptada(titulo, detalle,tipoSolicitud,telefonoCliente,
                    nombreCliente,uidCliente,
                    folio!!,fecha,problema,pagoTotal
                    ,oficio,telefonoKerkly,nombreKerkly,
                    direccionKerkly,correoKerkly,
                    uidKerkly!!
                )
            }
        }
    }

    private fun TraerNotificacionSolicitudUrgenteAceptada(
        titulo: String,
        detalle: String,
        tipoSolicitud: String?,
        telefonoCliente: String?,
        nombreCliente: String?,
        uidCliente: String?,
        folio: String,
        fecha: String?,
        problema: String?,
        pagoTotal: String?,
        oficio: String?,
        telefonoKerkly: String?,
        nombreKerkly: String?,
        direccionKerkly: String?,
        correoKerkly: String?,
        uidKerkly: String
    ) {
        val id = "$tipoSolicitud $folio"
        //val id2 = id.hashCode()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, id.toString())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nc = NotificationChannel(id, "solicitudUrgenteAceptada", NotificationManager.IMPORTANCE_HIGH)
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
                .setContentIntent(clicknotiSolicitudUrgenteAceptada(tipoSolicitud, telefonoCliente
                    , nombreCliente,uidCliente,folio,fecha,problema,pagoTotal
                    ,oficio,telefonoKerkly,nombreKerkly,
                    direccionKerkly,correoKerkly,uidKerkly))
                .setContentInfo("nuevo")
            val random = Random()
            val idNotity = random.nextInt(1000)
            assert(nm != null)
            nm!!.notify(idNotity, builder.build())
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun clicknotiSolicitudUrgenteAceptada(tipoSolicitud: String?, telefonoCliente: String?,
        nombreCliente: String?, uidCliente: String?, folio: String, fecha: String?,
        problema: String?, pagoTotal: String?, oficio: String?, telefonoKerkly: String?, nombreKerkly: String?,
        direccionKerkly: String?, correoKerkly: String?, uidKerkly: String): PendingIntent? {
        val nf = Intent(applicationContext, PantallaInicio::class.java)


        nf.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val uniqueId = uidKerkly.hashCode() // Puedes cambiar esto según tus necesidades
        return PendingIntent.getActivity(applicationContext, uniqueId, nf, flags)
    }

    private fun TraerNotificacionSolicitudNormalAceptada(
        titulo: String,
        detalle: String,
        tipoSolicitud: String?,
        telefonoCliente: String?,
        nombreCliente: String?,
        uidCliente: String?,folio:String,
        fecha: String?,problema: String?,pagoTotal: String?
        ,oficio: String?,telefonoKerkly: String?,nombre_completo_kerkly: String?,
        direccionKerkly: String?,correoKerkly: String?,uidKerkly: String
    ) {
        val id =  "$tipoSolicitud $folio"
        //val id2 = id.hashCode()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, id.toString())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nc = NotificationChannel(id, "solicitudNormalAceptada", NotificationManager.IMPORTANCE_HIGH)
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
                .setContentIntent(clicknotiSolicitudNormalAceptada(tipoSolicitud, telefonoCliente
                    , nombreCliente,uidCliente,folio,fecha,problema,pagoTotal
            ,oficio,telefonoKerkly,nombre_completo_kerkly,
            direccionKerkly,correoKerkly,uidKerkly))
                .setContentInfo("nuevo")
             val random = Random()
            val idNotity = random.nextInt(1000)
            assert(nm != null)
            nm!!.notify(idNotity, builder.build())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun clicknotiSolicitudNormalAceptada(tipoSolicitud: String?, telefonoCliente: String?,
                                                 nombreCliente: String?, uidCliente: String?
                                                 ,folio:String,fecha: String?,problema: String?,pagoTotal: String?
                                                 ,oficio: String?,telefonoKerkly: String?,nombre_completo_kerkly: String?,
                                                 direccionKerkly: String?,correoKerkly: String?,uidKerkly: String): PendingIntent? {
        val nf = Intent(applicationContext, MensajesExpress::class.java)
        nf.putExtra("tipoServicio","Registrado")
        nf.putExtra("nombreCompletoKerkly", nombre_completo_kerkly)
        nf.putExtra("telefonoKerkly", telefonoKerkly)
        nf.putExtra("direccionKerkly", direccionKerkly)
        nf.putExtra("correoKerkly", correoKerkly)
        nf.putExtra("TipoDeSolicitud", tipoSolicitud)
        nf.putExtra("Telefono", telefonoCliente)
        nf.putExtra("Problema", problema)
        nf.putExtra("NombreCliente", nombreCliente)
        nf.putExtra("uidKerkly", uidKerkly)
        nf.putExtra("Folio", folio.toInt())
        nf.putExtra("pagoTotal", pagoTotal!!.toDouble())
        nf.putExtra("uidCliente",uidCliente)
        nf.putExtra("Noti", "Noti")
        nf.putExtra("Fecha", fecha)
        nf.putExtra("Oficio", oficio)

        // nf.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        nf.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val uniqueId = uidCliente.hashCode() // Puedes cambiar esto según tus necesidades
        return PendingIntent.getActivity(applicationContext, uniqueId, nf, flags)
    }

    private fun TraerNotificacion(titulo: String, detalle: String, nombreKerkly: String,
                                  nombreCliente: String, telefonoKerkly: String,telefonoCliente:String,
                                  fotoKerkly: String, fotoCliente:String, tokenKerkly: String,
                                  tokenCliente: String, uidCliente: String, uidKerkly: String) {
        val id = "mensajeKerkly"
        val id2 = id.hashCode()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nc = NotificationChannel(id, "nuevokerkly", NotificationManager.IMPORTANCE_HIGH)
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
        nf.putExtra("urlFotoKerkly", fotoKerkly)
        nf.putExtra("urlFotoCliente", fotoCliente)
        nf.putExtra("telefonok", telefonoKerkly)
        nf.putExtra("telefonoCliente",telefonoCliente)

        println("fotokerkly clik $fotoKerkly")

        nf.putExtra("tokenKerkly", tokenKerkly)
        nf.putExtra("tokenCliente", tokenCliente)
        nf.putExtra("uidCliente", uidCliente)
        nf.putExtra("uidKerkly", uidKerkly)
        nf.putExtra("Noti", "Noti")



        // nf.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        nf.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val uniqueId = uidKerkly.hashCode() // Puedes cambiar esto según tus necesidades
        return PendingIntent.getActivity(applicationContext, uniqueId, nf, flags)

    }

    override fun onDestroy() {
        super.onDestroy()
        onMessageReceived(m)
    }

}