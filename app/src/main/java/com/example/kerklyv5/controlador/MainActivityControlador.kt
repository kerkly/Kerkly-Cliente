package com.example.kerklyv5.controlador


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.parser.IntegerParser
import com.example.kerklyv5.SolicitarServicio
import com.example.kerklyv5.express.PedirServicioExpress
import com.example.kerklyv5.modelo.Cliente
import com.example.kerklyv5.R
import com.example.kerklyv5.interfaces.*
import com.example.kerklyv5.modelo.serial.ModeloIntentos
import com.example.kerklyv5.url.Url
import com.example.kerklyv5.vista.MainActivity
import com.example.kerklyv5.vista.MainActivityVerificarSMS
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList


class MainActivityControlador {
    private var b = Bundle()
    private lateinit var nombre: String
    private lateinit var apellidoP: String
    private lateinit var apellidoM: String
    private var intentos = 1
    private lateinit var tel: String


    fun verficiarUsuario(usuario: Cliente, contexto: AppCompatActivity) {
        val Url = Url().url
//ProgressDialogFragment.showProgressBar(contexto)
        val adapter = RestAdapter.Builder()
            .setEndpoint(Url)
            .build()
        val api: LoginInterface = adapter.create(LoginInterface::class.java)
        api.VerficarUsuario(usuario.getUsuario(),
            usuario.getContra(),
            object : Callback<Response?> {
                override fun success(t: Response?, response: Response?) {
                    var entrada: BufferedReader? =  null

                  //  ProgressDialogFragment.hideProgressBar(contexto)

                    var Respuesta = ""
                    try {
                        entrada = BufferedReader(InputStreamReader(t?.body?.`in`()))
                        Respuesta = entrada.readLine()
                        Toast.makeText(contexto,Respuesta,Toast.LENGTH_SHORT).show()


                    }catch (e: Exception){
                        e.printStackTrace()
                    }

                  var Res = "Bienvenido"
                    if (Res.equals(Respuesta)){
                        val  intent = Intent(contexto, SolicitarServicio::class.java)
                        intent.putExtra("Telefono", usuario.getUsuario())
                        intent.putExtra("PresupuestoListo", false)
                        contexto.startActivity(intent)
                        contexto.finish()
                    }else{
                        Toast.makeText(contexto, "$Respuesta", Toast.LENGTH_SHORT).show()
                    }


                }

                override fun failure(error: RetrofitError) {

                //    Toast.makeText(contexto, "error $error" , Toast.LENGTH_SHORT).show()
                }

            }
        )
    }


    fun pruebaRegistrarNumero(usuario: Cliente, contexto: Activity, layoutTelefono: TextInputLayout) {
        //verificamos primero si el usuario ya se encuentra registrado
        val ROOT_URL = Url().url
        val adapter = RestAdapter.Builder()
            .setEndpoint(ROOT_URL)
            .build()
        val api: EntrarSinRegistroInterface = adapter.create(EntrarSinRegistroInterface::class.java)
        api.sinRegistro(usuario.getTelefonoNoR(),
            object : Callback<Response?> {
                override fun success(t: Response?, response: Response?) {
                    var salida: BufferedReader? = null
                    var entrada = ""
                    try {
                        salida = BufferedReader(InputStreamReader(t?.body?.`in`()))
                        entrada = salida.readLine()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    //  Toast.makeText(this@MainActivity, entrada, Toast.LENGTH_SHORT).show()
                    var cadena: String = "EL NUMERO YA EXISTE INTENTE CON OTRO"
                    if (cadena == entrada) {
                        layoutTelefono.error = contexto.getText(R.string.numeroRegistrado_error)
                        return
                    } else {
                        //verificar si el numero ya ha sido verificado en la tabla cliente no registrado
                        val ROOT_URL = Url().url

                        val gson = GsonBuilder().setLenient().create()
                        val interceptor = HttpLoggingInterceptor()
                        interceptor.level = HttpLoggingInterceptor.Level.BODY
                        val client: OkHttpClient = OkHttpClient.Builder().build()

                        val retrofit = Retrofit.Builder()
                            .baseUrl("$ROOT_URL/")
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build()
                        val get = retrofit.create(EntrarSinRegistroInterface::class.java)
                        val call = get.verificarClienteNoR(usuario.getTelefonoNoR())
                        call?.enqueue(object : retrofit2.Callback<List<ModeloIntentos?>?> {

                            override fun onResponse(
                                call: Call<List<ModeloIntentos?>?>,
                                response: retrofit2.Response<List<ModeloIntentos?>?>) {
                                val postList: ArrayList<ModeloIntentos> = response.body() as ArrayList<ModeloIntentos>

                                if (postList.size ==0 ){
                                    //en caso de que numero no se encuentre registrado, y sea la primera vez que ingresa, se tendra que verificar su numero
                                     if (entrada == usuario.getTelefonoNoR()) {
                                           layoutTelefono.error = null

                                          val i = Intent(contexto, MainActivityVerificarSMS::class.java)
                                         b.putString("clave", "sinRegistro")
                                           b.putString("Teléfono No Registrado", usuario.getTelefonoNoR())
                                           i.putExtras(b)
                                           i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                           contexto.startActivity(i)
                                           } else {
                                           Toast.makeText(contexto, "Error de registro", Toast.LENGTH_SHORT)
                                               .show()
                                       }
                                }else{
                                    val nom =  postList.get(0).nombre_noR
                                    val intentos1 = postList.get(0).numIntentos
                                  //  Toast.makeText(contexto, "Bienvenido $nom  intentos = $intentos1", Toast.LENGTH_LONG).show()
                                    val intentos2: Int = intentos1
                                    if(intentos1 == 3){
                                        Toast.makeText(contexto, "$nom Se acabaron las Pruebas Sin Registro", Toast.LENGTH_LONG).show()
                                    }else{
                                        //Mandamos el numero de intento actualizado a la base de datos
                                        val s =  intentos2+1
                                        val adap =  RestAdapter.Builder().setEndpoint(ROOT_URL).build()
                                        val api: EntrarSinRegistroInterface = adap.create(EntrarSinRegistroInterface::class.java)
                                        api.IncrementarNumIntentos(usuario.getTelefonoNoR(), s,
                                        object : Callback<Response?>{
                                            override fun success(t: Response?, response: Response?) {
                                                var salida: BufferedReader? = null
                                                var entrada = ""
                                                try {
                                                    salida = BufferedReader(InputStreamReader(t?.body?.`in`()))
                                                    entrada = salida!!.readLine()
                                                } catch (e: IOException) {
                                                    e.printStackTrace()
                                                }
                                              //  Toast.makeText(contexto, "entrada $entrada", Toast.LENGTH_SHORT).show()
                                                var cadena: String = "Exitoso"
                                                if (cadena == entrada) {
                                                    layoutTelefono.error = null
                                               //     Toast.makeText(contexto, "Bienvin@ $nom", Toast.LENGTH_SHORT).show()
                                                    val i = Intent(contexto, PedirServicioExpress::class.java)
                                                    b.putString("Teléfono No Registrado", usuario.getTelefonoNoR())
                                                    i.putExtras(b)
                                                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                    contexto.startActivity(i)
                                                   // finish()
                                                }
                                            }

                                            override fun failure(error: RetrofitError?) {
                                                Log.d("error del retrofit", t.toString())
                                            }

                                        })
                                    }
                                }
                            }
                            override fun onFailure(call: Call<List<ModeloIntentos?>?>, t: Throwable) {
                             //   Toast.makeText(contexto, "Codigo de respuesta de error: $t", Toast.LENGTH_SHORT).show()
                                Log.d("error del retrofit", t.toString())
                            }

                        })

                    }
                }

                override fun failure(error: RetrofitError?) {
                    println("error $error")

                }

            })
    }


    fun verificarSesion(id: String, contexto: AppCompatActivity) {
        val ROOT_URL = Url().url
        val adapter = RestAdapter.Builder()
            .setEndpoint(ROOT_URL)
            .build()
        val api = adapter.create(VerificarSesionInterface::class.java)
        api.sesionAbierta(id,
            object : retrofit.Callback<retrofit.client.Response?> {
                override fun success(
                    t: retrofit.client.Response?,
                    response2: retrofit.client.Response?
                ) {
                    var reader: BufferedReader? = null
                    var output = ""
                    try {
                        reader = BufferedReader(InputStreamReader(t?.body?.`in`()))

                        output = reader.readLine()
                        //  System.out.println("id $id pantalla inicio $output")


                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                        if (output.equals("0")) {
                            System.out.println("entro en linea 233")
                            val intent = Intent(contexto, MainActivity::class.java)
                            intent.putExtra("Telefono", output)
                            contexto.startActivity(intent)
                            contexto.finish()
                        } else {
                            System.out.println("entro en linea 256$output")
                            val intent = Intent(contexto, SolicitarServicio::class.java)
                            intent.putExtra("Telefono", output)
                            intent.putExtra("PresupuestoListo", false)
                            contexto.startActivity(intent)
                            contexto.finish()
                        }



                }

                override fun failure(error: RetrofitError?) {
                    println("error268 $error")
                }
            }
        )
    }

}