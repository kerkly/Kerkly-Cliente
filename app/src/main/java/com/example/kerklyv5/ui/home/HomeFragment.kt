package com.example.kerklyv5.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kerklyv5.R
import com.example.kerklyv5.SQLite.DataManager
import com.example.kerklyv5.SQLite.MisOficios
import com.example.kerklyv5.controlador.AdapterSpinnercopia
import com.example.kerklyv5.controlador.setProgressDialog
import com.example.kerklyv5.vista.MapsActivity
import com.firebase.ui.auth.AuthUI.getApplicationContext
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class HomeFragment : Fragment(){
    lateinit var spinner: Spinner
    private lateinit var textProblem: EditText
    private lateinit var layoutProblem: TextInputLayout
    private lateinit var oficio: String
    private lateinit var botonPresupuesto: MaterialButton
    private lateinit var b: Bundle
    private lateinit var telefono: String
    private lateinit var problema: String
    private lateinit var boton_servicioUrgente: MaterialButton
    val setprogress = setProgressDialog()
    lateinit var listaTextos: ArrayList<String>
    private lateinit var btn_otrosOficios: MaterialButton
    lateinit var dataManager: DataManager
    private lateinit var lista: ArrayList<MisOficios>
    private  var palabrasClave = mutableMapOf<String, String>()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private var locationManager: LocationManager? = null
    private var banPalabaraAsosiada: Boolean =  false
    // Declaración de la variable miembro
    private var textoAnterior: String = ""
    // Declarar la lista de palabras asociadas fuera del método
    val listaPalabrasAsociadas = mutableListOf<String>()
    private lateinit var scrollview: ScrollView
    private lateinit var buttonObtenerSeleccion: ImageView
    private var clik_Otro: Boolean = false

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        b = Bundle()
        dataManager = DataManager(requireContext())
        spinner = root.findViewById(R.id.spinnerNormal)
        textProblem = root.findViewById(R.id.inputProblematica)
        layoutProblem = root.findViewById(R.id.layoutProblematica)
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //botonDireccion = root.findViewById(R.id.button_dir)
        botonPresupuesto = root.findViewById(R.id.button_presupuesto)
        boton_servicioUrgente = root.findViewById(R.id.boton_servicio_urgente)
        //imageboton = root.findViewById(R.id.kerkly_boton)
       // btnfiltro = root.findViewById(R.id.filtrohome)
        buttonObtenerSeleccion = root.findViewById(R.id.buttonObtenerSeleccion)
        scrollview =  root.findViewById(R.id.scrollViewHome)
        obtenerOficiosDB()
        botonPresupuesto.setOnClickListener {
            val gpsEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!gpsEnabled) {
                val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(settingsIntent)
            }else {
                if (clik_Otro == true){
                    layoutProblem.error = null
                    seleccionarKerkly()
                }else{
                if (banPalabaraAsosiada == true){
                    layoutProblem.error = null
                    seleccionarKerkly()
                }else{
                    //showMessage(R.string.)
                    layoutProblem.error = resources.getString(R.string.palabrasAsociadas)
                }
            }
            }
        }
        //click servicio Urgente
        boton_servicioUrgente.setOnClickListener {
            val gpsEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (!gpsEnabled) {
                val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(settingsIntent)
            }else {
                telefono = arguments?.getString("Telefono")!!
                oficio = spinner.getSelectedItem().toString()
               // showMessage(oficio)
                problema = textProblem.text.toString()
                if (problema.isEmpty()) {
                    layoutProblem.error = getString(R.string.campo_requerido)
                } else {
                    if (clik_Otro == true){
                        layoutProblem.error = null
                        val i = Intent(context, MapsActivity::class.java)
                        b.putBoolean("Express", true)
                        b.putString("Oficio", oficio)
                        b.putString("Telefono", telefono)
                        b.putString("Nombre", currentUser!!.displayName)
                        b.putString("correo", currentUser!!.email)
                        b.putString("Problema", problema)
                        b.putString("uid", currentUser!!.uid)
                        i.putExtras(b)
                        startActivity(i)
                    }else{
                    if (banPalabaraAsosiada == true){
                        layoutProblem.error = null
                        val i = Intent(context, MapsActivity::class.java)
                        b.putBoolean("Express", true)
                        b.putString("Oficio", oficio)
                        b.putString("Telefono", telefono)
                        b.putString("Nombre", currentUser!!.displayName)
                        b.putString("correo", currentUser!!.email)
                        b.putString("Problema", problema)
                        b.putString("uid", currentUser!!.uid)
                        i.putExtras(b)
                        startActivity(i)
                    }else{
                        //showMessage(R.string.)
                        layoutProblem.error = resources.getString(R.string.palabrasAsociadas)
                        //showMessage(resources.getString(R.string.palabrasAsociadas))
                    }
                }
                }
            }
        }
        listaTextos = ArrayList()
        textProblem.setOnClickListener {
            // Desplaza el ScrollView hacia arriba cuando se hace clic en el TextView
            scrollview.post {
                scrollview.fullScroll(View.FOCUS_UP)
            }
        }

// Agrega un listener al ScrollView para manejar el evento de desplazamiento
      /*  scrollview.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                // Ajusta la altura del ScrollView al tamaño de la pantalla
                scrollview.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                return true
            }
        })*/

        textProblem.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                println("beforeTextChanged $p0")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                println("onTextChanged $p0")
            }
            override fun afterTextChanged(p0: Editable?) {
                val p = p0.toString()
                val parts: List<String> = p.split(" ")

                // Verificar si se eliminó una palabra
                val palabrasEliminadas = encontrarPalabrasEliminadas(textoAnterior, p)
                if (palabrasEliminadas.isNotEmpty()) {
                    if (clik_Otro == false){
                    println("Palabras eliminadas: $palabrasEliminadas")

                    // Eliminar las palabras asociadas de listaPalabrasAsociadas
                    for (palabraEliminada in palabrasEliminadas) {
                        val palabraAsociadaEliminada = palabrasClave[palabraEliminada]
                        if (palabraAsociadaEliminada != null) {
                            listaPalabrasAsociadas.remove(palabraAsociadaEliminada)
                            println("Palabra asociada eliminada: $palabraAsociadaEliminada")
                        }
                    }

                    // Actualizar el Spinner con la nueva lista
                    spinner.setAdapter(
                        ArrayAdapter<String>(
                            requireContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            listaPalabrasAsociadas
                        )
                    )
                    }
                } else {
                    println("No se eliminaron palabras.")
                }

                // Actualizar el texto anterior
                textoAnterior = p
               // listaTextos.clear()
                layoutProblem.error = null
                var contienePalabraAsociada = false
                for (i in 0 until parts.size) {
                    var pa = parts[i].trim()
                    if (pa.isNotBlank()) {
                        var palabraAsociada: String? = null
                        for ((clave, palabra) in palabrasClave) {
                            if (clave == pa) {
                                palabraAsociada = palabra
                                break
                            }
                        }
                        if (palabraAsociada != null) {
                            println("La palabra asociada a '$pa' es: $palabraAsociada")
                            banPalabaraAsosiada = true

                            if (!listaPalabrasAsociadas.contains(palabraAsociada)) {
                                listaPalabrasAsociadas.add(0, palabraAsociada)
                                println("palabra agregada a lista")
                                buttonObtenerSeleccion.visibility = View.VISIBLE
                            }
                            spinner.setAdapter(ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, listaPalabrasAsociadas))
                            contienePalabraAsociada = true

                          /*  spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                                    // La lógica que deseas ejecutar cuando se selecciona un oficio
                                    val selectedOficio = parentView.getItemAtPosition(position).toString()

                                    // Puedes realizar acciones basadas en el oficio seleccionado
                                    // Por ejemplo, mostrar un Toast con el oficio seleccionado
                                    Toast.makeText(requireContext(), "Oficio seleccionado: $selectedOficio", Toast.LENGTH_SHORT).show()

                                    // Aquí puedes usar "selectedOficio" como desees
                                    // Por ejemplo, pasarlo a una función o realizar alguna acción específica
                                }

                                override fun onNothingSelected(parentView: AdapterView<*>?) {
                                    // Este método se llama cuando no se ha seleccionado ningún elemento.
                                }
                            })*/

                        } else {

                            if (listaPalabrasAsociadas.isEmpty()) {
                                if (clik_Otro == true){

                                }else{
                                println("no coincide...")
                                llenarSpinner()
                                banPalabaraAsosiada = false
                                buttonObtenerSeleccion.visibility = View.GONE
                                }
                            }
                        }
                    }
                }

                if (p0.toString().isBlank() && !contienePalabraAsociada) {
                    // Llamar al método cuando el texto está vacío y no contiene ninguna palabra asociada
                    if (clik_Otro == false){
                        llenarSpinner()
                        listaPalabrasAsociadas.clear()
                        println("borrado..")
                        buttonObtenerSeleccion.visibility = View.GONE
                    }

                }
            }
        })



        buttonObtenerSeleccion.setOnClickListener {
            val selectedOficio = spinner.selectedItem.toString()
            Log.d("MiApp", "Antes de obtener la descripción para $selectedOficio")
            val descripcion = dataManager.obtenerDescripcion(selectedOficio)
            Log.d("MiApp", "Descripción de $selectedOficio: $descripcion")
            //Toast.makeText(requireContext(), " $selectedOficio", Toast.LENGTH_SHORT).show()
            // Mostrar un AlertDialog con la descripción
            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setTitle("Descripción de $selectedOficio")
            alertDialogBuilder.setMessage(descripcion)
            alertDialogBuilder.setPositiveButton("Aceptar", null)

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
        /*btn_otrosOficios = root.findViewById(R.id.boton_Oficios)
        btn_otrosOficios.setOnClickListener {
            listaTextos.clear()
          //  val aa = AdapterSpinner(requireActivity(), listaArrayOficios)
           // spinner.adapter = aa
        }*/
        sharedViewModel.actualizacionNecesaria.observe(viewLifecycleOwner) { necesitaActualizar ->
            if (necesitaActualizar) {
                // Realiza la actualización del fragmento aquí
                obtenerOficiosDB()
                sharedViewModel.actualizacionNecesaria.value = false // Reinicia la bandera
            }
        }
        return root
    }

    private fun encontrarPalabrasEliminadas(textoAnterior: String, textoActual: String): List<String> {
        val palabrasAntes = textoAnterior.split(" ")
        val palabrasDespues = textoActual.split(" ")

        return palabrasAntes.filterNot { palabrasDespues.contains(it) }
    }

    private fun llenarSpinner(){
        lateinit var listita: ArrayList<String>
        listita = ArrayList()
        listita.add("Selecciona un oficio")
        listita.add("Otro")
        val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, listita)

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                // Verifica si la opción seleccionada es "Otro"
                if (selectedItem == "Otro") {
                    // Aquí puedes manejar el evento cuando se selecciona "Otro"
                    // Por ejemplo, mostrar un cuadro de diálogo, iniciar otra actividad, etc.
                    // Puedes agregar tu lógica aquí.
                  //  Toast.makeText(requireContext(), "Seleccionaste Otro", Toast.LENGTH_SHORT).show()
                     SpinerADapter(lista)
                    buttonObtenerSeleccion.visibility = View.VISIBLE
                    clik_Otro = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Implementa este método si deseas manejar el caso en el que no se selecciona nada.
            }
        }

    }
    class SharedViewModel : ViewModel() {
        val actualizacionNecesaria = MutableLiveData<Boolean>()
    }
    private fun seleccionarKerkly() {
      //  oficio = spinner.selectedItem.toString()
        oficio = spinner.getSelectedItem().toString()
        telefono = arguments?.getString("Telefono")!!

        b.putString("Oficio", oficio)
        b.putString("Telefono", telefono)
        problema = textProblem.text.toString()
        b.putString("Problema", problema)
        b.putString("correo", currentUser!!.email)

        if (problema.isEmpty()) {
            layoutProblem.error = getString(R.string.campo_requerido)
        } else {
            layoutProblem.error = null
                val i = Intent(context, MapsActivity::class.java)
                b.putBoolean("Express", false)
                b.putString("Nombre", currentUser!!.displayName)
                b.putString("uid", currentUser!!.uid)
                i.putExtras(b)
                startActivity(i)
            }
       // }
    }

@SuppressLint("SuspiciousIndentation")
fun obtenerOficiosDB(){
    lista = ArrayList()
//    dataManager = DataManager(requireContext())
     lista= dataManager.getAllOficios()
       //SpinerADapter(lista)
     llenarSpinner()
        Diccionario()
}
    fun showMessage(mensaje: String){
        Toast.makeText(requireContext(),mensaje,Toast.LENGTH_SHORT).show()
    }

    fun SpinerADapter(lista: ArrayList<MisOficios>){
        val aa = AdapterSpinnercopia(requireContext(), lista)
        spinner.adapter = aa

    }
    fun Diccionario(){
        var palClaves = ""
        var oficio = ""
        for (i in 0 until lista!!.size){
            palClaves = lista[i].palabrasClaves
            //  pal = pal+ palClaves +"|"
            oficio = lista[i].nombreOfi
            val parts: List<String> = palClaves.split(", ")
            for (i in 0 until parts.size) {
                var pa = parts[i]
                palabrasClave[pa] = oficio
               // palC.add(pa)
            }
        }
    }
}