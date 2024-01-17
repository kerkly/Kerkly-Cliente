package com.example.kerklyv5.ui.slideshow

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kerklyv5.MainActivityChats
import com.example.kerklyv5.R
import com.example.kerklyv5.databinding.FragmentSlideshowBinding
import com.example.kerklyv5.modelo.adapterUsuarios
import com.example.kerklyv5.modelo.usuarios
import com.example.kerklyv5.url.Instancias
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class SlideshowFragment : Fragment() {

    private lateinit var slideshowViewModel: SlideshowViewModel
    private var _binding: FragmentSlideshowBinding? = null

    private val binding get() = _binding!!

    private lateinit var reciclerView: RecyclerView
    private lateinit var instancias: Instancias
    private lateinit var uidCliente:String
    private lateinit var nombreCliente:String
    lateinit var  telefonoCliente: String
    var fechaHora: String? = null
    lateinit var fotoCliente: String
    private lateinit var tokenCliente: String
    private lateinit var arrayListdatos: ArrayList<usuarios>
    private lateinit var Miadapter: adapterUsuarios
    private lateinit var array: ArrayList<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        slideshowViewModel = ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root
        reciclerView = root.findViewById(R.id.recycler_Usuarios)

        instancias = Instancias()
        telefonoCliente =  arguments?.getString("telefono").toString()
        uidCliente =  arguments?.getString("uid").toString()
        fotoCliente =  arguments?.getString("fotoCliente").toString()
        tokenCliente =  arguments?.getString("tokenCliente").toString()
        nombreCliente =  arguments?.getString("nombreCliente").toString()
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

        array = ArrayList<String>()

        val databaseReference = instancias.referenciaListaDeUsuarios(uidCliente)
        databaseReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                println("id usuario ${snapshot.value}")
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
        return root
    }

    private fun setScrollBar() {
        reciclerView.scrollToPosition(Miadapter.itemCount-1)
    }


    private fun mostrarUsuarios (snapshot: DataSnapshot){
        array = arrayListOf(snapshot.value.toString())
        // println("lista de usu ${snapshot.child("uid").value}")
        val idkerkly = snapshot.child("uid").value.toString()
        val databaseUsu = instancias.referenciaInformacionDelKerkly(snapshot.child("uid").value.toString())
        //val  databaseUsu = firebaseDatabaseUsu.getReference("UsuariosR").child("kerkly").child(snapshot.child("uid").value.toString()).child("MisDatos")
        databaseUsu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //println("aqui 139 " + snapshot.value)
                //println(" datos101: " + snapshot.getValue())
                val u2 = snapshot.getValue(usuarios::class.java)
                if (u2 ==null){
                    Toast.makeText(requireContext(), "No Tienes Ningun Cotacto", Toast.LENGTH_SHORT).show()
                }else{
                    Miadapter.agregarUsuario(u2!!)
                }
                val mGestureDetector = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                    GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
                        override fun onSingleTapUp(e: MotionEvent): Boolean {
                            return true
                        }
                    })
                } else {
                    TODO("VERSION.SDK_INT < CUPCAKE")
                }
                reciclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                    override fun onRequestDisallowInterceptTouchEvent(b: Boolean) {}
                    override fun onInterceptTouchEvent(
                        recyclerView: RecyclerView, motionEvent: MotionEvent
                    ): Boolean {
                        try {
                            val child = recyclerView.findChildViewUnder(motionEvent.x, motionEvent.y)
                            if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {
                                val position = recyclerView.getChildAdapterPosition(child)
                                val correo = Miadapter.lista[position].correo
                                val nombre = Miadapter.lista[position].nombre
                                val telefono = Miadapter.lista[position].telefono
                                val urlfoto = Miadapter.lista[position].foto
                                val tokenKerkly = Miadapter.lista[position].token
                                val uidKerkly = Miadapter.lista[position].uid


                                val intent = Intent(requireContext(), MainActivityChats::class.java)
                                intent.putExtra("nombreCompletoK", nombre)
                                intent.putExtra("telefonok",telefono)
                                intent.putExtra("telefonoCliente", telefonoCliente)
                                intent.putExtra("tokenKerkly", tokenKerkly)
                                intent.putExtra("tokenCliente", tokenCliente)
                                intent.putExtra("nombreCompletoCliente", nombreCliente)
                                intent.putExtra("urlFotoKerkly",urlfoto)
                                intent.putExtra("urlFotoCliente",fotoCliente)
                                intent.putExtra("uidCliente",uidCliente)
                                intent.putExtra("uidKerkly",uidKerkly)

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
                System.out.println("Firebase: $error")

            }
        })

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}