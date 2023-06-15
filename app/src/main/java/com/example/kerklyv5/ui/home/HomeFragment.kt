package com.example.kerklyv5.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.kerklyv5.R
import com.example.kerklyv5.controlador.AdapterOficios
import com.example.kerklyv5.controlador.AdapterSpinner
import com.example.kerklyv5.controlador.setProgressDialog
import com.example.kerklyv5.interfaces.ObtenerOficiosInterface
import com.example.kerklyv5.modelo.serial.Oficio
import com.example.kerklyv5.url.Url
import com.example.kerklyv5.vista.MapsActivity
import com.firebase.ui.auth.AuthUI.getApplicationContext
import com.google.android.material.button.MaterialButton
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.regex.Matcher
import java.util.regex.Pattern


class HomeFragment : Fragment(){

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var spinner: Spinner
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var textProblem: EditText
  //  private lateinit var layoutProblem: TextInputLayout
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
    private lateinit var Miadapter: AdapterOficios
    private lateinit var buscar: SearchView
    private lateinit var recyclerViewOficios: RecyclerView
     lateinit var listaArrayOficios: ArrayList<Oficio>
 //   private lateinit var btnfiltro: Button
    private lateinit var inicio: String
    private lateinit var pal: String
    private lateinit var final:String
    private lateinit var expresion:String
    lateinit var listaTextos: ArrayList<String>
    lateinit var correoCliente:String
    private lateinit var btn_otrosOficios: MaterialButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        /*homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)*/
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        // Obtienes el Bundle del Intent

       b = Bundle()

      spinner = root.findViewById(R.id.spinnerNormal)
        textProblem = root.findViewById(R.id.inputProblematica)
     //  layoutProblem = root.findViewById(R.id.layoutProblematica)

        //botonDireccion = root.findViewById(R.id.button_dir)
        botonPresupuesto = root.findViewById(R.id.button_presupuesto)
        boton_servicioUrgente = root.findViewById(R.id.boton_servicio_urgente)
        //imageboton = root.findViewById(R.id.kerkly_boton)
       // btnfiltro = root.findViewById(R.id.filtrohome)

        botonPresupuesto.setOnClickListener {
            nombreCliente = arguments?.getString("Nombre")!!
            seleccionarKerkly()
        }

        //click servicio Urgente
        boton_servicioUrgente.setOnClickListener {
            oficio = spinner.getSelectedItem().toString()
            telefono = arguments?.getString("Telefono")!!
            System.out.println("-------------> oficiooo $oficio")
            nombreCliente = arguments?.getString("Nombre")!!
            b.putString("Oficio", oficio)
            b.putString("Telefono", telefono)
            problema = textProblem.text.toString()
            b.putString("Problema", problema)

            if (problema.isEmpty()) {
              // layoutProblem.error = getString(R.string.campo_requerido)
           } else {
              // layoutProblem.error = null

               val i = Intent(context, MapsActivity::class.java)
                b.putBoolean("Express", true)
               b.putString("Nombre", nombreCliente.toString())
               i.putExtras(b)
                startActivity(i)
            }
        }

       /* buscar = root.findViewById(R.id.search_oficios)
        recyclerViewOficios = root.findViewById(R.id.recycler_oficios)
        listaArrayOficios = ArrayList<Oficio>()
        recyclerViewOficios.setHasFixedSize(true)
        recyclerViewOficios.layoutManager = LinearLayoutManager(context)*/
        getOficios()

     /*   val plomero= "plomero"
        val regex = "[A-Za-z]+@[a-z]+\\.[a-z]+" //expresion para reconocer correos
        val nombreA = "^[A-ZÑa-zñáéíóúÁÉÍÓÚ'° ]+\$" //Expresion Para reconocer nombres y apellidos
        val palabras = "(?i)(\\W|^)(tontería|maldito|caray|$plomero|madre\\smía|ostras)(\\W|\$)"

        val patron: Pattern = Pattern.compile(palabras)
        var texto = "plomero"
            val emparejador: Matcher = patron.matcher(texto)
            val esCoincidente = emparejador.find()
            if (esCoincidente) {
                println("texto Reconocido: ")
            }else{
                println("no reconocido: ")
            }*/

       // setprogress.setProgressDialog(requireContext())
    /*    btnfiltro.setOnClickListener{
            val aa = AdapterSpinner(requireActivity(), listaArrayOficios)
           // spinner.adapter = aa
          //   problema = textProblem.text.toString()
          //  Miadapter.filtrado(problema)
        }*/
        listaTextos = ArrayList()
        textProblem.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                println("beforeTextChanged $p0")
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
               println("onTextChanged $p0")
            }

            override fun afterTextChanged(p0: Editable?) {
                println("afterTextChanged $p0 ")
                val p = p0.toString()
                val parts: List<String> = p.split(" ")
                for(i in 0 until parts.size){
                    var pa = parts[i]
                    println("palabras separadas $pa")

                    val patron: Pattern = Pattern.compile(expresion)
                    val emparejador: Matcher = patron.matcher(pa)
                    val esCoincidente = emparejador.find()
                    var estaSiEs =""
                    if (esCoincidente) {
                        estaSiEs = parts[i]
                        listaTextos.add(parts[i])
                        //btnfiltro.text = "texto Reconocido: $estaSiEs + tamaño del arraylist ${listaTextos.size}"
                        if (listaTextos.size<=1){
                            spinner.setAdapter(ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, listaTextos))
                        }else{
                            listaTextos.clear()
                            //val aa = AdapterSpinner(requireActivity(), listaArrayOficios)
                            //spinner.adapter = aa
                            //listaTextos.clear()
                        }
                    }else{
                        println("no reconocido: ")
                        //btnfiltro.text = "no reconocido: "
                    }
                }
                if (p0.toString() == ""){
                   // btnfiltro.text = "no hay texto"
                    val aa = AdapterSpinner(requireActivity(), listaArrayOficios)
                    spinner.adapter = aa
                    listaTextos.clear()
                }
            }
        })

        btn_otrosOficios = root.findViewById(R.id.boton_Oficios)
        btn_otrosOficios.setOnClickListener {
            listaTextos.clear()
            val aa = AdapterSpinner(requireActivity(), listaArrayOficios)
            spinner.adapter = aa
            listaTextos.clear()
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
            override fun onResponse(call: Call<List<Oficio?>?>, response: retrofit2.Response<List<Oficio?>?>) {
                listaArrayOficios = response.body() as ArrayList<Oficio>
                 inicio = "(?i)(\\W|^)("
                 pal = ""
                 final ="\\smía|ostras)(\\W|\$)"
                var oficio = ""
                for (i in 0 until listaArrayOficios!!.size){
                    oficio = listaArrayOficios[i].nombreO
                    pal = pal+ oficio +"|"
                }
                 expresion = "$inicio$pal$final"
                println("expresion armada $inicio"+pal+final)
               /*
                val inicio = "(?i)(\\W|^)("
                var pal = ""
                val final ="\\smía|ostras)(\\W|\$)"
                var oficio = ""
            for (i in 0 until listaArrayOficios!!.size){
                oficio = listaArrayOficios[i].nombreO
                pal = pal+ oficio +"|"
            }
                val expresion = "$inicio$pal$final"
                println("expresion armada $inicio"+pal+final)
                val patron: Pattern = Pattern.compile(expresion)
                var texto = "plomero"
                val emparejador: Matcher = patron.matcher(texto)
                val esCoincidente = emparejador.find()
                if (esCoincidente) {
                    println("texto Reconocido: ")
                    val aa = AdapterSpinner(requireActivity(), listaArrayOficios)
                }else{
                    println("no reconocido: ")
                }
*/
                val aa = AdapterSpinner(requireActivity(), listaArrayOficios)
                spinner.adapter = aa

              //  setprogress.dialog.dismiss()
             //   println("--->" + postList[0].nombreO)
         /*     Miadapter = AdapterOficios(listaArrayOficios, listaArrayOficios)
               Miadapter.setOnClickListener{
                   val nombreOfi = listaArrayOficios[recyclerViewOficios.getChildAdapterPosition(it)].nombreO.trim()
                   Toast.makeText(requireContext(), "$nombreOfi", Toast.LENGTH_SHORT).show()
                }
                recyclerViewOficios.adapter = Miadapter
                buscar.setOnQueryTextListener(this@HomeFragment)*/
            }

            override fun onFailure(call: Call<List<Oficio?>?>, t: Throwable) {
               // setprogress.dialog.dismiss()
                Log.d("error del retrofit", t.toString())
               // Toast.makeText(requireActivity(), t.toString(), Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun seleccionarKerkly() {
      //  oficio = spinner.selectedItem.toString()
        oficio = spinner.getSelectedItem().toString()
        telefono = arguments?.getString("Telefono")!!
        correoCliente = arguments?.getString("correoCliente")!!

        // Log.d("tel", telefono)

        b.putString("Oficio", oficio)
        b.putString("Telefono", telefono)
        problema = textProblem.text.toString()

        b.putString("Problema", problema)

        if (problema.isEmpty()) {
          //  layoutProblem.error = getString(R.string.campo_requerido)
        } else {
         //   layoutProblem.error = null
         //   val f = KerklyFragment()
         //   f.arguments = b
         //   var fm = requireActivity().supportFragmentManager.beginTransaction().apply {
         //       replace(R.id.nav_host_fragment_content_solicitar_servicio, f).commit()
                val i = Intent(context, MapsActivity::class.java)
                b.putBoolean("Express", false)
                b.putString("correoCliente", correoCliente)
                b.putString("Nombre", nombreCliente.toString())
                i.putExtras(b)
                startActivity(i)
            }
       // }
    }

   /* override fun onQueryTextSubmit(p0: String?): Boolean {
        println("home 220 ")
        Miadapter.filtrado(p0!!)
        return false
    }*/

  /*  override fun onQueryTextChange(p0: String?): Boolean {
       Miadapter.filtrado(p0!!)
        println("home 225 ")
        return false
    }*/


}
