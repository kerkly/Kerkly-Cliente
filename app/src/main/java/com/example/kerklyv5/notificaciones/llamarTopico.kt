package com.example.kerklyv5.notificaciones

import android.content.Context
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class llamarTopico {

     fun llamartopico(context: Context, token: String, Mensaje: String, Titulo: String,) {
         var firebaseMessaging = FirebaseMessaging.getInstance().subscribeToTopic("EnviarNoti")
         firebaseMessaging.addOnCompleteListener {
             //Toast.makeText(this@MainActivityChats, "Registrado:", Toast.LENGTH_SHORT).show()
         }

        val myrequest = Volley.newRequestQueue(context)
        val json = JSONObject()
        try {
            //  val token = "fNBcaF1mT2qEj9KexMEduK:APA91bGPhunaVKF8eBITrArKl_G_5qvl-ZLAOPzsBxhEZaNXH-MqmrISayZDxVt1FjzdU-qXPECesJ2IvhPM-f1lgo6786bANQT_apL2iMhV2DV5k1Uw9YYp1_m_5qcT8IfaW4QETJE_"
            json.put("to", "/$token/" + "EnviarNoti")
            val notificacion = JSONObject()
            notificacion.put("titulo", Titulo)
            notificacion.put("detalle", Mensaje)
            notificacion.put("dato", "ejemplo de cliente")
            //  notificacion.put("foto", url_foto)
            json.put("data", notificacion)
            val URL = "https://fcm.googleapis.com/fcm/send"
            val request: JsonObjectRequest =
                object : JsonObjectRequest(Method.POST, URL, json, null, null) {
                    override fun getHeaders(): Map<String, String> {
                        val header: MutableMap<String, String> = HashMap()
                        header["content-type"] = "application/json"
                        header["authorization"] = "key=AAAA5adbonE:APA91bE_Ymd-u5HEcSLb3Ps5878UXdXMf1GXT_Yrl9l5m3CPHlwyEXqchhqblmetYtejadNViumDgtxCBDEiO7nUu5K7yNSc52AsIIviInR93QqLhsWIT4fXLZj3L_R36W4y5lF633Pj"
                        return header
                    }
                }
            myrequest.add(request)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun llamarTopicEnviarSolicitudUrgente(
        context: Context,
        token: String,
        Mensaje: String,
        Titulo: String,
        latitud: String,
        longitud: String,
        Folio: String,
        direccion: String,
        telefonoCliente: String,
        Curp: String,
        telefonoKerkly: String,
        correoCliente: String,
        correoKerkly: String,
        NombreKerkly: String,
        uidCliente: String,
        problema: String
    ,fechaSolicitud:String,
        nombreOficio:String
    ){

        var firebaseMessaging = FirebaseMessaging.getInstance().subscribeToTopic("EnviarNoti")
        firebaseMessaging.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("Suscrito exitosamente a 'EnviarNoti'")
                // Resto del código
        val myrequest = Volley.newRequestQueue(context)
        val json = JSONObject()
        try {
            println("fecha $fechaSolicitud  nombreOficio $nombreOficio Folio---> $Folio, coordenadas $latitud $longitud")
            println("titulo $Titulo detalle $Mensaje latitud $latitud longitud $longitud Folio $Folio nombreCompletoCliente $Titulo")
            println("direccion $direccion problema $problema telefonoCliente $telefonoCliente tipoServicio urgente Curp $Curp")
            println("telefonok $telefonoKerkly correoCliente $correoCliente correoKerkly $correoKerkly nombreCompletoKerkly $NombreKerkly uidCliente $uidCliente")

            json.put("to", "/$token/" + "EnviarNoti")
            val notificacion = JSONObject()
            notificacion.put("tipoNoti","llamarTopicEnviarSolicitudUrgente")
            notificacion.put("titulo", Titulo)
            notificacion.put("detalle", Mensaje)
            notificacion.put("latitud", latitud)
            notificacion.put("longitud",longitud)
            notificacion.put("Folio", Folio)
            notificacion.put("nombreCompletoCliente", Titulo)
            notificacion.put("direccion", direccion)
            notificacion.put("problema", problema)
            notificacion.put("telefonoCliente", telefonoCliente)
            notificacion.put("tipoServicio", "urgente")
            notificacion.put("Curp", Curp)
            notificacion.put("telefonok", telefonoKerkly)
            notificacion.put("correoCliente",correoCliente)
            notificacion.put("correoKerkly",correoKerkly)
            notificacion.put("nombreCompletoKerkly", NombreKerkly)
            notificacion.put("uidCliente",uidCliente)
            notificacion.put("fechaSolicitud",fechaSolicitud)
            notificacion.put("nombreOficio",nombreOficio)

            json.put("data", notificacion)
            val URL = "https://fcm.googleapis.com/fcm/send"
            val request: JsonObjectRequest =
                object : JsonObjectRequest(Method.POST, URL, json,
                    { /* onResponse */ },
                    { error ->
                        error.printStackTrace()
                        println("Error en la solicitud HTTP: ${error.message}")
                    }
                ) {
                    override fun getHeaders(): Map<String, String> {
                        val header: MutableMap<String, String> = HashMap()
                        header["content-type"] = "application/json"
                        header["authorization"] = "key=AAAA5adbonE:APA91bE_Ymd-u5HEcSLb3Ps5878UXdXMf1GXT_Yrl9l5m3CPHlwyEXqchhqblmetYtejadNViumDgtxCBDEiO7nUu5K7yNSc52AsIIviInR93QqLhsWIT4fXLZj3L_R36W4y5lF633Pj"
                        return header
                    }
                }



            myrequest.add(request)
        }catch (e: JSONException) {
            e.printStackTrace()
            println("Error al construir el JSON para la notificación: ${e.message}")
        } catch (e: VolleyError) {
            e.printStackTrace()
            println("Error al enviar la solicitud HTTP: ${e.message}")
        }
            } else {
                println("Error al suscribirse a 'EnviarNoti': ${task.exception}")
            }
        }
    }


    fun llamarTopicoAceptarSolicitud(context: Context, token: String, Mensaje: String, Titulo: String, folio:String,telefonoKerkly:String){
        var firebaseMessaging = FirebaseMessaging.getInstance().subscribeToTopic("EnviarNoti")
        firebaseMessaging.addOnCompleteListener {
            //Toast.makeText(this@MainActivityChats, "Registrado:", Toast.LENGTH_SHORT).show()
        }

        val myrequest = Volley.newRequestQueue(context)
        val json = JSONObject()
        try {
            //  val token = "fNBcaF1mT2qEj9KexMEduK:APA91bGPhunaVKF8eBITrArKl_G_5qvl-ZLAOPzsBxhEZaNXH-MqmrISayZDxVt1FjzdU-qXPECesJ2IvhPM-f1lgo6786bANQT_apL2iMhV2DV5k1Uw9YYp1_m_5qcT8IfaW4QETJE_"
            json.put("to", "/$token/" + "EnviarNoti")
            val notificacion = JSONObject()
            notificacion.put("tipoNoti","PresupuestoAceptado")
            notificacion.put("titulo", Titulo)
            notificacion.put("detalle", Mensaje)
            notificacion.put("Folio", folio)
            notificacion.put("numT", telefonoKerkly)

            json.put("data", notificacion)
            val URL = "https://fcm.googleapis.com/fcm/send"
            val request: JsonObjectRequest =
                object : JsonObjectRequest(Method.POST, URL, json, null, null) {
                    override fun getHeaders(): Map<String, String> {
                        val header: MutableMap<String, String> = HashMap()
                        header["content-type"] = "application/json"
                        header["authorization"] = "key=AAAA5adbonE:APA91bE_Ymd-u5HEcSLb3Ps5878UXdXMf1GXT_Yrl9l5m3CPHlwyEXqchhqblmetYtejadNViumDgtxCBDEiO7nUu5K7yNSc52AsIIviInR93QqLhsWIT4fXLZj3L_R36W4y5lF633Pj"
                        return header
                    }
                }
            myrequest.add(request)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun llamartopicoSolicitudClienteNR(context: Context, token: String, Mensaje: String, Titulo: String,) {
        var firebaseMessaging = FirebaseMessaging.getInstance().subscribeToTopic("EnviarNoti")
        firebaseMessaging.addOnCompleteListener {
            //Toast.makeText(this@MainActivityChats, "Registrado:", Toast.LENGTH_SHORT).show()
        }

        val myrequest = Volley.newRequestQueue(context)
        val json = JSONObject()
        try {
            //  val token = "fNBcaF1mT2qEj9KexMEduK:APA91bGPhunaVKF8eBITrArKl_G_5qvl-ZLAOPzsBxhEZaNXH-MqmrISayZDxVt1FjzdU-qXPECesJ2IvhPM-f1lgo6786bANQT_apL2iMhV2DV5k1Uw9YYp1_m_5qcT8IfaW4QETJE_"
            json.put("to", "/$token/" + "EnviarNoti")
            val notificacion = JSONObject()
            notificacion.put("titulo", Titulo)
            notificacion.put("detalle", Mensaje)
            notificacion.put("dato", "ejemplo de cliente")
            //  notificacion.put("foto", url_foto)
            json.put("data", notificacion)
            val URL = "https://fcm.googleapis.com/fcm/send"
            val request: JsonObjectRequest =
                object : JsonObjectRequest(Method.POST, URL, json, null, null) {
                    override fun getHeaders(): Map<String, String> {
                        val header: MutableMap<String, String> = HashMap()
                        header["content-type"] = "application/json"
                        header["authorization"] = "key=AAAA5adbonE:APA91bE_Ymd-u5HEcSLb3Ps5878UXdXMf1GXT_Yrl9l5m3CPHlwyEXqchhqblmetYtejadNViumDgtxCBDEiO7nUu5K7yNSc52AsIIviInR93QqLhsWIT4fXLZj3L_R36W4y5lF633Pj"
                        return header
                    }
                }
            myrequest.add(request)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun chats(context: Context, token: String, Mensaje: String, Titulo: String,
              nombreKerkly: String,telefonoKerkly:String,telefonoCliente:String, fotoCliente:String, tokenCliente:String,
    uidCliente: String, uidKerkly:String) {
        //println("mandando ---> $uidCliente")
        var firebaseMessaging = FirebaseMessaging.getInstance().subscribeToTopic("EnviarNoti")
        firebaseMessaging.addOnCompleteListener {
            //Toast.makeText(this@MainActivityChats, "Registrado:", Toast.LENGTH_SHORT).show()
        }

        val myrequest = Volley.newRequestQueue(context)
        val json = JSONObject()
        try {
            json.put("to", "/$token/" + "EnviarNoti")
            val notificacion = JSONObject()
            notificacion.put("tipoNoti","chats")
            notificacion.put("titulo", Titulo)
            notificacion.put("detalle", Mensaje)
            notificacion.put("nombreCompletoCliente", Titulo)
            notificacion.put("nombreCompletoKerkly", nombreKerkly)
            notificacion.put("telefonoKerkly", telefonoKerkly)
            notificacion.put("telefonoCliente", telefonoCliente)
            notificacion.put("urlFotoCliente", fotoCliente)
            notificacion.put("tokenCliente", tokenCliente)
            notificacion.put("uidCliente", uidCliente)
            notificacion.put("uidKerkly", uidKerkly)
            notificacion.put("Noti", "Noti")

            println("notificacion enviada")
            //  notificacion.put("foto", url_foto)
            json.put("data", notificacion)
            val URL = "https://fcm.googleapis.com/fcm/send"
            val request: JsonObjectRequest =
                object : JsonObjectRequest(Method.POST, URL, json, null, null) {
                    override fun getHeaders(): Map<String, String> {
                        val header: MutableMap<String, String> = HashMap()
                        header["content-type"] = "application/json"
                        header["authorization"] = "key=AAAA5adbonE:APA91bE_Ymd-u5HEcSLb3Ps5878UXdXMf1GXT_Yrl9l5m3CPHlwyEXqchhqblmetYtejadNViumDgtxCBDEiO7nUu5K7yNSc52AsIIviInR93QqLhsWIT4fXLZj3L_R36W4y5lF633Pj"
                        return header
                    }
                }
            myrequest.add(request)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }


    fun llamarTopicEnviarSolicitudNormal(context: Context, token: String, Mensaje: String, Titulo: String,latitud: String,
                                          longitud:String, Folio:String,direccion:String, telefonoCliente:String, Curp:String,
                                          telefonoKerkly:String, correoCliente:String,NombreKerkly:String,
                                         uidCliente:String,fechaSolicitud: String,nombreOficio: String,problema: String
    ){

        var firebaseMessaging = FirebaseMessaging.getInstance().subscribeToTopic("EnviarNoti")
        firebaseMessaging.addOnCompleteListener {
            //Toast.makeText(this@MainActivityChats, "Registrado:", Toast.LENGTH_SHORT).show()
        }

        val myrequest = Volley.newRequestQueue(context)
        val json = JSONObject()
        try {
            //  val token = "fNBcaF1mT2qEj9KexMEduK:APA91bGPhunaVKF8eBITrArKl_G_5qvl-ZLAOPzsBxhEZaNXH-MqmrISayZDxVt1FjzdU-qXPECesJ2IvhPM-f1lgo6786bANQT_apL2iMhV2DV5k1Uw9YYp1_m_5qcT8IfaW4QETJE_"
            json.put("to", "/$token/" + "EnviarNoti$Folio")
            val notificacion = JSONObject()
            notificacion.put("tipoNoti","llamarTopicEnviarSolicitudNormal")
            notificacion.put("titulo", Titulo)
            notificacion.put("detalle", Mensaje)
            notificacion.put("latitud", latitud)
            notificacion.put("longitud",longitud)
            notificacion.put("Folio", Folio)
            notificacion.put("nombreCompletoCliente", Titulo)
            notificacion.put("direccion", direccion)
            notificacion.put("problema", problema)
            notificacion.put("telefonoCliente", telefonoCliente)
            notificacion.put("tipoServicio", "normal")
            notificacion.put("Curp", Curp)
            notificacion.put("telefonok", telefonoKerkly)
            notificacion.put("correoCliente",correoCliente)
            notificacion.put("correoKerkly",correoCliente)
            notificacion.put("nombreCompletoKerkly", NombreKerkly)
            notificacion.put("uidCliente",uidCliente)
            notificacion.put("fechaSolicitud",fechaSolicitud)
            notificacion.put("nombreOficio",nombreOficio)
            println("fecha $fechaSolicitud  nombreOficio $nombreOficio Folio---> $Folio, coordenadas $latitud $longitud")

            json.put("data", notificacion)
            val URL = "https://fcm.googleapis.com/fcm/send"
            val request: JsonObjectRequest =
                object : JsonObjectRequest(Method.POST, URL, json, null, null) {
                    override fun getHeaders(): Map<String, String> {
                        val header: MutableMap<String, String> = HashMap()
                        header["content-type"] = "application/json"
                        header["authorization"] = "key=AAAA5adbonE:APA91bE_Ymd-u5HEcSLb3Ps5878UXdXMf1GXT_Yrl9l5m3CPHlwyEXqchhqblmetYtejadNViumDgtxCBDEiO7nUu5K7yNSc52AsIIviInR93QqLhsWIT4fXLZj3L_R36W4y5lF633Pj"
                        return header
                    }
                }
            myrequest.add(request)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}