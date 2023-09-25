package com.example.kerklyv5.vista.fragmentos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kerklyv5.MainActivityAceptarServicio
import com.example.kerklyv5.MainActivityChats
import com.example.kerklyv5.R
import com.example.kerklyv5.controlador.AdapterOrdenPendiente
import com.example.kerklyv5.controlador.setProgressDialog
import com.example.kerklyv5.express.FormaPagoExrpess
import com.example.kerklyv5.express.MensajesExpress
import com.example.kerklyv5.interfaces.ObtenerOrdenPendienteInterface
import com.example.kerklyv5.modelo.Pdf
import com.example.kerklyv5.modelo.serial.OrdenPendiente
import com.example.kerklyv5.url.Url
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class OrdenesPendientesFragment : Fragment() {
    private lateinit var telefono: String
    private lateinit var b: Bundle
    private lateinit var nombreCompletoCliente: String
  //  private  var header: ArrayList<String> = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_ordenes_pendientes, container, false)


        b = requireArguments()

        telefono = b.getString("Telefono").toString()
        nombreCompletoCliente = b.getString("nombreCompletoCliente")!!

        val btnSolicitudNormal = v.findViewById<MaterialButton>(R.id.buttonSolicitudNormal)
        btnSolicitudNormal.setOnClickListener {
            val intent = Intent(requireContext(), MainActivityMostrarSolicitudes::class.java)
            b!!.putString("TipoDeSolicitud","normal")
            intent.putExtras(b!!)
            startActivity(intent)
        }

        val btnSolicitudUrgente = v.findViewById<MaterialButton>(R.id.buttonSolicitudUrgente)
        btnSolicitudUrgente.setOnClickListener {
            val intent = Intent(requireContext(), MainActivityMostrarSolicitudes::class.java)
            b!!.putString("TipoDeSolicitud","urgente")
            intent.putExtras(b!!)
            startActivity(intent)
        }
        return v
    }



    // private fun setChats(n: String, ap: String, nCompleto: String, id: Int) {
    private fun setChats(b_: Bundle) {
       val f = ContactosFragment()
       f.arguments = b_
       /* f.arguments?.putInt("IdContrato", id)
        f.arguments?.putString("Nombre Kerkly", n)
        f.arguments?.putString("Ap Kerkly", ap)
        f.arguments?.putString("Nombre completo Kerkly", nCompleto)*/
        var fm = requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_content_solicitar_servicio,f).commit()
        }
    }
}