package com.example.kerklyv5.vista.fragmentos

import android.app.Activity
import android.content.Intent
import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.kerklyv5.R
import com.example.kerklyv5.ui.home.HomeFragment
import com.google.android.material.button.MaterialButton

class OrdenesPendientesFragment : Fragment() {
    private lateinit var telefono: String
    private lateinit var b: Bundle
    private lateinit var nombreCompletoCliente: String
    private lateinit var uidCliente:String
    companion object {
        const val REQUEST_CODE_ORDENES_PENDIENTES = 123 // Puedes elegir cualquier número
    }

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
        uidCliente =  b.getString("uid").toString()

        val btnSolicitudNormal = v.findViewById<MaterialButton>(R.id.buttonSolicitudNormal)
        btnSolicitudNormal.setOnClickListener {
          /*val intent = Intent(requireContext(), MainActivityMostrarSolicitudes::class.java)
            val args = Bundle()
            args.putString("TipoDeSolicitud", "normal")
            args.putString("uidCliente", uidCliente)
            args.putString("Noti", "no")
            b!!.putString("Telefono",telefono)
            intent.putExtras(args)
          startActivityForResult(intent, REQUEST_CODE_ORDENES_PENDIENTES)*/
            val intent = Intent(requireContext(), MainActivityMostrarSolicitudes::class.java)
            b!!.putString("TipoDeSolicitud","normal")
            b!!.putString("uidCliente", uidCliente)
            b!!.putString("Noti", "no")
            b!!.putString("Telefono",telefono)
            intent.putExtras(b!!)
            startActivity(intent)
        }

        val btnSolicitudUrgente = v.findViewById<MaterialButton>(R.id.buttonSolicitudUrgente)
        btnSolicitudUrgente.setOnClickListener {
            val intent = Intent(requireContext(), MainActivityMostrarSolicitudes::class.java)
            b!!.putString("TipoDeSolicitud","urgente")
            b!!.putString("uidCliente", uidCliente)
            b!!.putString("Noti", "no")
            b!!.putString("Telefono",telefono)
            intent.putExtras(b!!)
            startActivity(intent)
        }
        return v
    }


  /*  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_ORDENES_PENDIENTES && resultCode == Activity.RESULT_OK) {
            telefono = b.getString("Telefono").toString()
            nombreCompletoCliente = b.getString("nombreCompletoCliente")!!
            uidCliente =  b.getString("uid").toString()
        }
    }*/

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