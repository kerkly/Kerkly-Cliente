package com.example.kerklyv5.ui.gallery

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.kerklyv5.R
import com.example.kerklyv5.databinding.FragmentGalleryBinding
import com.example.kerklyv5.vista.fragmentos.MainActivityMostrarSolicitudes
import com.google.android.material.button.MaterialButton

class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private var _binding: FragmentGalleryBinding? = null

    private lateinit var telefono: String
    //private lateinit var b: Bundle
    private lateinit var nombreCompletoCliente: String
    private lateinit var uidCliente:String
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        galleryViewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

       // b = requireArguments()

        telefono = arguments?.getString("telefono").toString()
        nombreCompletoCliente = arguments?.getString("nombreCompletoCliente").toString()
        uidCliente =  arguments?.getString("uid").toString()

        println("tel $telefono nom $nombreCompletoCliente uid $uidCliente")

        val btnSolicitudNormal = root.findViewById<MaterialButton>(R.id.buttonSolicitudNormal)
        btnSolicitudNormal.setOnClickListener {
            val intent = Intent(requireContext(), MainActivityMostrarSolicitudes::class.java)
            intent.putExtra("TipoDeSolicitud", "normal")
            intent.putExtra("uidCliente", uidCliente)
            intent.putExtra("Noti", "no")
            intent.putExtra("Telefono", telefono)
            intent.putExtra("nombreCompletoCliente", nombreCompletoCliente)
            startActivity(intent)

            // intent.putExtras(b!!)
           // startActivity(intent)
        }

        val btnSolicitudUrgente = root.findViewById<MaterialButton>(R.id.buttonSolicitudUrgente)
        btnSolicitudUrgente.setOnClickListener {
           /* val intent = Intent(requireContext(), MainActivityMostrarSolicitudes::class.java)
            b!!.putString("TipoDeSolicitud","urgente")
            b!!.putString("uidCliente", uidCliente)
            b!!.putString("Noti", "no")
            b!!.putString("Telefono",telefono)
            intent.putExtras(b!!)
            startActivity(intent)*/
            val intent = Intent(requireContext(), MainActivityMostrarSolicitudes::class.java)
            intent.putExtra("TipoDeSolicitud", "urgente")
            intent.putExtra("uidCliente", uidCliente)
            intent.putExtra("Noti", "no")
            intent.putExtra("Telefono", telefono)
            intent.putExtra("nombreCompletoCliente", nombreCompletoCliente)
            startActivity(intent)

        }
        return root
    }

   /* fun onBackPressed(): Boolean {
        // Lógica específica del fragmento para el evento de retroceso
        // Devuelve true si el evento ha sido manejado en el fragmento

        /*if () {
            // Hacer algo y devolver true para indicar que se manejó el evento
            return true
        }*/

        // Si no se manipula el retroceso, devuelve false
        return true
    }*/
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}