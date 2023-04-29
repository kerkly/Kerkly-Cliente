package com.example.kerklyv5.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kerklyv5.R
import com.example.kerklyv5.controlador.AdapterKerkly
import com.example.kerklyv5.controlador.AdapterSpinner
import com.example.kerklyv5.controlador.setProgressDialog
import com.example.kerklyv5.distancia_tiempo.CalcularTiempoDistancia
import com.example.kerklyv5.interfaces.ObtenerKerklyInterface
import com.example.kerklyv5.interfaces.ObtenerOficiosInterface
import com.example.kerklyv5.modelo.serial.Kerkly
import com.example.kerklyv5.modelo.serial.Oficio
import com.example.kerklyv5.notificaciones.obtenerKerklys_y_tokens
import com.example.kerklyv5.url.Url
import com.example.kerklyv5.vista.MapsActivity
import com.example.kerklyv5.vista.fragmentos.KerklyFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var spinner: Spinner
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var textProblem: TextInputEditText
    private lateinit var layoutProblem: TextInputLayout
    private lateinit var oficio: String
    private var latitud: Double = 0.0
    private var longitud: Double = 0.0
    private lateinit var botonDireccion: MaterialButton
    private lateinit var botonPresupuesto: MaterialButton
    private lateinit var imageboton: ImageButton
    private lateinit var b: Bundle
    private lateinit var telefono: String
    private lateinit var problema: String
    private lateinit var boton_servicioUrgente: MaterialButton
    val setprogress = setProgressDialog()
    private lateinit var nombreCliente: String


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        /*homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)*/
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        // Obtienes el Bundle del Intent
        //setprogress.setProgressDialog(requireContext())
       b = Bundle()

        getOficios()

        spinner = root.findViewById(R.id.spinnerNormal)
        textProblem = root.findViewById(R.id.inputProblematica)
        layoutProblem = root.findViewById(R.id.layoutProblematica)
        //botonDireccion = root.findViewById(R.id.button_dir)
        botonPresupuesto = root.findViewById(R.id.button_presupuesto)
        boton_servicioUrgente = root.findViewById(R.id.boton_servicio_urgente)
        //imageboton = root.findViewById(R.id.kerkly_boton)

        botonPresupuesto.setOnClickListener {
            nombreCliente = arguments?.getString("Nombre")!!
            seleccionarKerkly()
        }

        //click servicio Urgente
        boton_servicioUrgente.setOnClickListener {
            oficio = spinner.selectedItem.toString()
            telefono = arguments?.getString("Telefono")!!
            // Log.d("tel", telefono)
            nombreCliente = arguments?.getString("Nombre")!!
            b.putString("Oficio", oficio)
            b.putString("Telefono", telefono)
            problema = textProblem.text.toString()
            b.putString("Problema", problema)

            if (problema.isEmpty()) {
                layoutProblem.error = getString(R.string.campo_requerido)
            } else {
                layoutProblem.error = null

                val i = Intent(context, MapsActivity::class.java)
                b.putBoolean("Express", true)
                b.putString("Nombre", nombreCliente.toString())
                i.putExtras(b)
                startActivity(i)
            }
        }

        return root
    }

    private fun getOficios () {
        val ROOT_URL = Url().url
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client: OkHttpClient = OkHttpClient.Builder().build()

        val retrofit = Retrofit.Builder()
            .baseUrl("$ROOT_URL/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val presupuestoGET = retrofit.create(ObtenerOficiosInterface::class.java)
        val call = presupuestoGET.oficios()

        call?.enqueue(object : retrofit2.Callback<List<Oficio?>?> {

            override fun onResponse(
                call: Call<List<Oficio?>?>,
                response: retrofit2.Response<List<Oficio?>?>
            ) {
                val postList: ArrayList<Oficio> = response.body()
                        as ArrayList<Oficio>

                val aa = AdapterSpinner(requireActivity(), postList)
                println("------------->" + aa)
                spinner.adapter = aa
                //setprogress.dialog.dismiss()

            }

            override fun onFailure(call: Call<List<Oficio?>?>, t: Throwable) {

                Log.d("error del retrofit", t.toString())
               // Toast.makeText(requireActivity(), t.toString(), Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun seleccionarKerkly() {
        oficio = spinner.selectedItem.toString()

        telefono = arguments?.getString("Telefono")!!

       // Log.d("tel", telefono)

        b.putString("Oficio", oficio)
        b.putString("Telefono", telefono)

        problema = textProblem.text.toString()

        b.putString("Problema", problema)

        if (problema.isEmpty()) {
            layoutProblem.error = getString(R.string.campo_requerido)
        } else {
            layoutProblem.error = null
           /* val f = KerklyFragment()
            f.arguments = b
            var fm = requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.nav_host_fragment_content_solicitar_servicio,f).commit()*/
            val i = Intent(context, MapsActivity::class.java)
            b.putBoolean("Express", false)
            b.putString("Nombre", nombreCliente.toString())
            i.putExtras(b)
            startActivity(i)
            }
        }


}
