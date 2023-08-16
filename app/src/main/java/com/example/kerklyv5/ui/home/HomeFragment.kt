package com.example.kerklyv5.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.kerklyv5.R
import com.example.kerklyv5.SQLite.DataManager
import com.example.kerklyv5.SQLite.MisOficios
import com.example.kerklyv5.controlador.AdapterOficios
import com.example.kerklyv5.controlador.AdapterSpinner
import com.example.kerklyv5.controlador.AdapterSpinnercopia
import com.example.kerklyv5.controlador.setProgressDialog
import com.example.kerklyv5.interfaces.ObtenerOficiosInterface
import com.example.kerklyv5.modelo.serial.Oficio
import com.example.kerklyv5.url.Url
import com.example.kerklyv5.vista.MapsActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout

import org.apache.commons.text.WordUtils
import org.apache.commons.text.similarity.FuzzyScore
import java.io.File
class HomeFragment : Fragment(){

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var spinner: Spinner
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var textProblem: EditText
   // private lateinit var textProblem: AutoCompleteTextView
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
    private lateinit var dataManager: DataManager
    private lateinit var lista: ArrayList<MisOficios>
    private  var palabrasClave = mutableMapOf<String, String>()
    private  var palC =mutableListOf<String>()


    @SuppressLint("SuspiciousIndentation")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
       b = Bundle()
        dataManager = DataManager(requireContext())
      spinner = root.findViewById(R.id.spinnerNormal)
        textProblem = root.findViewById(R.id.inputProblematica)
       layoutProblem = root.findViewById(R.id.layoutProblematica)

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
            nombreCliente = arguments?.getString("Nombre")!!
            oficio = spinner.getSelectedItem().toString()
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
               b.putString("Nombre", nombreCliente.toString())

                b.putString("Problema", problema)
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
                          println("palabras separadas $pa")
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
                              println("La palabra asociada a '$pa' es: $palabraAsociada")
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
                        SpinerADapter()
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
        return root
    }


    private fun seleccionarKerkly() {
      //  oficio = spinner.selectedItem.toString()
        oficio = spinner.getSelectedItem().toString()
        telefono = arguments?.getString("Telefono")!!
        nombreCliente = arguments?.getString("Nombre")!!
        println("telefono .. $telefono")
        correoCliente = arguments?.getString("correoCliente")!!

        Log.d("tel---->", telefono + nombreCliente  +correoCliente)

        b.putString("Oficio", oficio)
        b.putString("Telefono", telefono)
        problema = textProblem.text.toString()

        b.putString("Problema", problema)

        if (problema.isEmpty()) {
            layoutProblem.error = getString(R.string.campo_requerido)
        } else {
            layoutProblem.error = null

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


fun obtenerOficiosDB(){
    lista = ArrayList()
     lista= dataManager.getAllOficios()
    SpinerADapter()
    Diccionario()
  //  val adaptador = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, palC)
   // textProblem.setAdapter(adaptador)
   // println("Palabras ingresadas:")
   // for (palabra in palC) {
       // println(palabra)
 //   }
}
    fun SpinerADapter(){
        val aa = AdapterSpinnercopia(requireActivity(), lista)
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