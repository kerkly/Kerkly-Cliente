package com.example.kerklyv5.controlador

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kerklyv5.R
import com.example.kerklyv5.modelo.serial.Kerkly

class AdapterKerkly(val datset: ArrayList<Kerkly>):
    RecyclerView.Adapter<AdapterKerkly.ViewHolder>(), View.OnClickListener {

    private lateinit var listener: View.OnClickListener

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNombre: TextView = view.findViewById(R.id.nombre_kekrly_txt)

        val txt_tiempo: TextView = view.findViewById(R.id.tiempo_kerkly)

        init {}

    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: AdapterKerkly.ViewHolder, position: Int) {
        val n = datset[position].Nombre
        val ap = datset[position].Apellido_Paterno
        val am = datset[position].Apellido_Materno
        val nombre = "$n $ap $am"

        viewHolder.txtNombre.text = nombre
        //var h = datset[position].hora
       // var m = datset[position].minutos

        var h = ""
        var m = ""

        if (datset[position].hora  < 10) {
            h = "0${datset[position].hora}"
        } else {
            h = "${datset[position].hora}"
        }

        if (datset[position].minutos  < 10) {
            m = "0${datset[position].minutos}"
        } else {
            m = "${datset[position].minutos}"
        }
        viewHolder.txt_tiempo.text = "$h:$m"


    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler_kerkly, viewGroup, false)

        view.setOnClickListener(this)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datset.size
    }

    fun setOnClickListener(l: View.OnClickListener) {
        this.listener = l

    }

    override fun onClick(v: View?) {
        listener.onClick(v)
    }

}