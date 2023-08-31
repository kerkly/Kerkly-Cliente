package com.example.kerklyv5.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kerklyv5.R
import com.example.kerklyv5.SQLite.DataManager
import com.example.kerklyv5.SQLite.MisOficios
import com.example.kerklyv5.controlador.AdapterSpinner
import com.example.kerklyv5.controlador.AdapterSpinnercopia
import com.example.kerklyv5.controlador.setProgressDialog
import com.example.kerklyv5.modelo.serial.Oficio
import com.example.kerklyv5.vista.MapsActivity
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
        //botonDireccion = root.findViewById(R.id.button_dir)
        botonPresupuesto = root.findViewById(R.id.button_presupuesto)
        boton_servicioUrgente = root.findViewById(R.id.boton_servicio_urgente)
        //imageboton = root.findViewById(R.id.kerkly_boton)
       // btnfiltro = root.findViewById(R.id.filtrohome)
        obtenerOficiosDB()
        botonPresupuesto.setOnClickListener {
            seleccionarKerkly()
        }
        //click servicio Urgente
        boton_servicioUrgente.setOnClickListener {
            telefono = arguments?.getString("Telefono")!!
            oficio = spinner.getSelectedItem().toString()
           showMessage(oficio)
            problema = textProblem.text.toString()
            if (problema.isEmpty()) {
               layoutProblem.error = getString(R.string.campo_requerido)
           } else {
               layoutProblem.error = null
             //   val diccionarioPath = "https://firebasestorage.googleapis.com/v0/b/hybrid-saga-346617.appspot.com/o/0_palabras_todas_no_conjugaciones.txt?alt=media&token=eb2a8142-d979-4a45-834f-ec1953e4b48b"
               // val diccionario = File(diccionarioPath).readLines().toSet()

              //  if (problema in diccionario) {
                //    println("La palabra existe en el diccionario")
              //      Toast.makeText(requireContext(),"La palabra existe en el diccionario",Toast.LENGTH_SHORT).show()
              //  } else {
                //    println("La palabra no existe en el diccionario")
                //    Toast.makeText(requireContext(),"La palabra no existe en el diccionario",Toast.LENGTH_SHORT).show()
               // }
               val i = Intent(context, MapsActivity::class.java)
                b.putBoolean("Express", true)
                b.putString("Oficio", oficio)
                b.putString("Telefono", telefono)
               b.putString("Nombre", currentUser!!.displayName)
                b.putString("correo", currentUser!!.email)
                b.putString("Problema", problema)
                b.putString("uid",currentUser!!.uid)
               i.putExtras(b)
                startActivity(i)
            }
        }
        listaTextos = ArrayList()
          textProblem.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    println("beforeTextChanged $p0")
                }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                   println("onTextChanged $p0")
                }

                override fun afterTextChanged(p0: Editable?) {
                      val p = p0.toString()
                      val parts: List<String> = p.split(" ")
                      //val palabrasClaves = listOf("palabra1", "palabra2", "palabra3", "palabra4")
                    listaTextos.clear()
                      for (i in 0 until parts.size) {
                          var pa = parts[i]
                          //println("palabras separadas $pa")
                         //var palabraAsociada = palabrasClave[pa]
                          var palabraAsociada: String? = null
                          for ((clave, palabra) in palabrasClave) {
                              if (clave == pa) {
                                  palabraAsociada = palabra
                                  println("---> $palabraAsociada")
                                  break
                              }
                          }
                          if (palabraAsociada != null) {
                             // println("La palabra asociada a '$pa' es: $palabraAsociada")
                             // listaTextos.clear()
                              listaTextos.add(palabraAsociada)
                              spinner.setAdapter(ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, listaTextos))
                              //textProblem.setAdapter(ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, listaTextos.distinct()))

                          } else {
                              println("No se encontró una palabra asociada a '$pa'")
                          }
                         /* val patron: Pattern = Pattern.compile(expresion)
                          val emparejador: Matcher = patron.matcher(pa)
                          val esCoincidente = emparejador.find()
                          var estaSiEs = ""
                          if (esCoincidente) {
                              estaSiEs = parts[i]
                              listaTextos.add(parts[i])
                              //btnfiltro.text = "texto Reconocido: $estaSiEs + tamaño del arraylist ${listaTextos.size}"
                              if (listaTextos.size <= 1) {
                                  spinner.setAdapter(
                                      ArrayAdapter<String>(
                                          requireContext(),
                                          android.R.layout.simple_spinner_dropdown_item,
                                          listaTextos
                                      )
                                  )

                              } else {
                                  listaTextos.clear()
                                  obtenerOficiosDB()
                                  //listaTextos.clear()
                              }
                          } else {
                              println("no reconocido: ")
                              //btnfiltro.text = "no reconocido: "
                          }*/
                      }
                    if (p0.toString() == "") {
                        // btnfiltro.text = "no hay texto"
                        SpinerADapter(lista)
                        listaTextos.clear()
                    }
                }
            })
        btn_otrosOficios = root.findViewById(R.id.boton_Oficios)
        btn_otrosOficios.setOnClickListener {
            listaTextos.clear()
          //  val aa = AdapterSpinner(requireActivity(), listaArrayOficios)
           // spinner.adapter = aa
        }
        sharedViewModel.actualizacionNecesaria.observe(viewLifecycleOwner) { necesitaActualizar ->
            if (necesitaActualizar) {
                // Realiza la actualización del fragmento aquí
                obtenerOficiosDB()
                sharedViewModel.actualizacionNecesaria.value = false // Reinicia la bandera
            }
        }
        return root
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
        SpinerADapter(lista)
        Diccionario()

  //  val adaptador = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, palC)
   // textProblem.setAdapter(adaptador)
   // println("Palabras ingresadas:")
   // for (palabra in palC) {
       // println(palabra)
 //   }
}
    fun showMessage(mensaje: String){
        Toast.makeText(requireContext(),mensaje,Toast.LENGTH_SHORT).show()
    }

    fun SpinerADapter(lista: ArrayList<MisOficios>){
        val aa = AdapterSpinnercopia(requireContext(), lista)
        spinner.adapter = aa
    }
    fun Diccionario(){
        //inicio = "(?i)(\\W|^)("
        //pal = ""
       // final ="\\smía|ostras)(\\W|\$)"
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
       // expresion = "$inicio$pal"+"$final"
       // println("expresion armada $inicio"+pal+final)
    }
}