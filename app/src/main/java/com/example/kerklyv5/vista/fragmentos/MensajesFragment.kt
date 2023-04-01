package com.example.kerklyv5.vista.fragmentos

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.example.kerklyv5.MainActivityChats
import com.example.kerklyv5.R
import com.example.kerklyv5.modelo.adapterUsuarios
import com.example.kerklyv5.modelo.usuarios
import com.google.firebase.database.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class MensajesFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var id: String? = null
    private var correo: String? = null
    var fechaHora: String? = null
    lateinit var foto: String
    private var nombre: String? = null
    private lateinit var array: ArrayList<String>
    private lateinit var arrayListdatos: ArrayList<usuarios>


    private var b: Bundle? = null
    private lateinit var reciclerView: RecyclerView
    private lateinit var Miadapter: adapterUsuarios
    private lateinit var firebaseDatabaseUsu: FirebaseDatabase
    private lateinit var databaseUsu: DatabaseReference
    var usu: usuarios? = usuarios()
    var cont: Int =0
    lateinit var  telefonoCliente: String
    lateinit var fotourl: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v =  inflater.inflate(R.layout.fragment_mensajes, container, false)
        reciclerView = v.findViewById(R.id.recycler_Usuarios)

        b = requireArguments()
         telefonoCliente = b!!.getString("telefonoCliente")!!
        fotourl = b!!.getString("urlFotoCliente")!!

        arrayListdatos = ArrayList()
        Miadapter = adapterUsuarios(requireContext())
        reciclerView.setHasFixedSize(true)
        reciclerView.layoutManager = LinearLayoutManager(context)
        reciclerView.adapter = Miadapter

        Miadapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                setScrollBar()
            }
        })


        //Primero Obtendremos la lista de numeros de telefonos
        var firebaseDatabaseLista: FirebaseDatabase
        var databaseReference: DatabaseReference
       // var databaseReferenceLista: Task<DataSnapshot>
       // firebaseDatabaseLista = FirebaseDatabase.getInstance()
        array = ArrayList<String>()
        firebaseDatabaseLista = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabaseLista.getReference("UsuariosR").child(telefonoCliente)
         .child("Lista de Usuarios")
    //  val  firebaseDatabaseusu = firebaseDatabaseLista.getReference("UsuariosR").child("7471503418")
           // .child("Lista de Usuarios")
        databaseReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
             mostrarUsuarios(snapshot)

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
             //   Miadapter.lista.clear()
                Miadapter.notifyDataSetChanged();
                //mostrarUsuarios(snapshot)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        b = Bundle()


        return v
    }


    private fun setScrollBar() {
        reciclerView.scrollToPosition(Miadapter.itemCount-1)
       // println("entro 217 "+ {Miadapter.itemCount-1 })
    }

    private fun mostrarUsuarios (snapshot: DataSnapshot){
        println(" tel: " + snapshot.child("telefono").value)
        array = arrayListOf(snapshot.child("telefono").value.toString())
        firebaseDatabaseUsu = FirebaseDatabase.getInstance()
        databaseUsu = firebaseDatabaseUsu.getReference("UsuariosR").child(snapshot.child("telefono").value.toString()).child("MisDatos")

        databaseUsu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
              println("aqui 139 " + snapshot.value)
                println(" datos101: " + snapshot.getValue())
                val u2 = snapshot.getValue(usuarios::class.java)
                Miadapter.agregarUsuario(u2!!)

                val mGestureDetector = GestureDetector(requireContext(), object : SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean {
                        return true
                    }
                })
                reciclerView.addOnItemTouchListener(object : OnItemTouchListener {
                    override fun onRequestDisallowInterceptTouchEvent(b: Boolean) {}
                    override fun onInterceptTouchEvent(
                        recyclerView: RecyclerView, motionEvent: MotionEvent): Boolean {
                        try {
                            val child = recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)
                            if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {
                                val position = recyclerView.getChildAdapterPosition(child)
                                val correo = Miadapter.lista[position].correo
                                val nombre = Miadapter.lista[position].nombre
                                val telefono = Miadapter.lista[position].telefono
                                val urlfoto = Miadapter.lista[position].foto
                                //Toast.makeText(requireContext(),"$telefono",Toast.LENGTH_SHORT).show()
                                val intent = Intent(requireContext(), MainActivityChats::class.java)
                                b!!.putString("nombreCompletoK", nombre)
                                b!!.putString("correoK", correo)
                                b!!.putString("telefonok",telefono)
                                b!!.putString("telefonoCliente", telefonoCliente)
                                //pendiente
                                b!!.putString("urlFotoKerkly",urlfoto)
                                intent.putExtras(b!!)
                                startActivity(intent)
                                return true
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return false
                    }
                    override fun onTouchEvent(
                        recyclerView: RecyclerView,
                        motionEvent: MotionEvent
                    ) {
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })

    }
}