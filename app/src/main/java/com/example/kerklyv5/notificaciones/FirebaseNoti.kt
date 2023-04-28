package com.example.kerklyv5.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.kerklyv5.R
import com.example.kerklyv5.vista.PantallaInicio
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException
import java.util.*

class FirebaseNoti: FirebaseMessagingService() {

    lateinit var token1: String
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        token1 = token
        Log.e("token", "mi token es:$token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val from: String = message.getFrom()!!

        if (message.getData().size > 0) {
            val titulo: String = message.getData().get("titulo")!!
            val detalle: String = message.getData().get("detalle")!!
            // val foto: String = message.getData().get("foto")!!
            CraerNotificacion(titulo, detalle)
        }
    }

    private fun CraerNotificacion(titulo: String, detalle: String) {
        val id = "mensajeCliente"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nc = NotificationChannel(id, "nuevoCliente", NotificationManager.IMPORTANCE_HIGH)
            nc.setShowBadge(true)
            assert(nm != null)
            nm!!.createNotificationChannel(nc)
        }
        try {
            //Bitmap imf_foto= Picasso.get(getApplicationContext()).load(foto).get();
            //   Picasso.get().load(user.getPhotoUrl()).placeholder(R.drawable.iconoperito).into(img);
            //val imf_foto = Picasso.get().load(foto).get()
            builder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(titulo)
                .setSmallIcon(R.drawable.ic_baseline_account_circle_24)
                .setContentText(detalle)
                .setContentIntent(clicknoti())
                .setContentInfo("nuevo")
            val random = Random()
            val idNotity = random.nextInt(8000)
            assert(nm != null)
            nm!!.notify(idNotity, builder.build())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun clicknoti(): PendingIntent? {
        val flags = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

        val nf = Intent(applicationContext, PantallaInicio::class.java)
        val pendingIntent : PendingIntent = PendingIntent.getActivity(applicationContext, 0, nf, flags)
        nf.putExtra("color", "rojo")
        nf.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(this, 0, nf, flags)

    }


}