package com.example.kerklyv5.controlador

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.kerklyv5.R
import com.example.kerklyv5.modelo.Mensaje
import com.example.kerklyv5.modelo.serial.Oficio
import com.example.kerklyv5.modelo.serial.OrdenPendiente
import java.lang.Character.toLowerCase
import java.util.Locale
import java.util.function.Predicate
import java.util.stream.Collectors

class AdapterOficios(val listaOficio: ArrayList<Oficio>,val listaOriginal: ArrayList<Oficio>):
    RecyclerView.Adapter<AdapterOficios.ViewHolder>(), View.OnClickListener{

    private lateinit var listener: View.OnClickListener

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)  {
        val txt_oficio = view.findViewById<TextView>(R.id.txt_Oficio)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txt_oficio!!.text = listaOficio[position].nombreO
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.card_view_oficios, null, false)
        return ViewHolder(view)
    }


    fun filtrado(txtBuscar: String) {
        val longitud = txtBuscar.length
        if (longitud==0){
            listaOficio.clear()
            listaOficio.addAll(listaOriginal)
            println("aqui  42")
        }else{
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
                val collecion: List<Oficio> = listaOficio.stream()
                    .filter(Predicate<Oficio> { i: Oficio ->
                        i.nombreO.toLowerCase()
                            .contains(txtBuscar.lowercase(Locale.CANADA))
                    })
                    .collect(Collectors.toList<Any>()) as List<Oficio>
                listaOficio.clear()
                listaOficio.addAll(collecion)
                println("aqui  52")

            }else{
                val ofi = Oficio()
                for (ofi in listaOriginal){
                    if (ofi.nombreO.toLowerCase().contains(txtBuscar.toLowerCase())){
                        listaOficio.addAll(listOf(ofi))
                        println("aqui  60")
                    }
                }
                println("aqui  63")
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return listaOficio.size
    }

    fun setOnClickListener(l: View.OnClickListener) {
        this.listener = l

    }

    override fun onClick(v: View?) {
        listener.onClick(v)
    }


}