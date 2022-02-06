package com.example.kerklyv5.controlador

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kerklyv5.R
import com.example.kerklyv5.modelo.Mensaje

class AdapterChat(c: Context): RecyclerView.Adapter<AdapterChat.ViewHolder>() {

    private var lista = ArrayList<Mensaje>()
    var context = c

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

       val txt_mensaje = view.findViewById<TextView>(R.id.txt_mensaje_chat)

        val txt_fecha = view.findViewById<TextView>(R.id.txt_fechaMensaje_chat)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_mensaje, parent, false)


        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txt_mensaje.text = lista[position].mensaje
        holder.txt_fecha.text = lista[position].hora
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    fun addMensaje(m: Mensaje) {
        lista.add(m)
        notifyItemInserted(lista.size)
    }
}