package com.example.kerklyv5.controlador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kerklyv5.R
import com.example.kerklyv5.modelo.serial.MensajesDatoss

class AdapterMensajes(val datset: ArrayList<MensajesDatoss>):
    RecyclerView.Adapter<AdapterMensajes.ViewHolder>(), View.OnClickListener {

    private lateinit var listener: View.OnClickListener

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtFolio: TextView = view.findViewById(R.id.tvfolio)

        init {}


        val txtFecha: TextView = view.findViewById(R.id.tvfecha)

        init {}

        val image: ImageView = view.findViewById(R.id.ivimage)

        val txtproblema: TextView = view.findViewById(R.id.txtProblema)
        val txtNombreOficio: TextView = view.findViewById(R.id.txtNombreOficio)

    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: AdapterMensajes.ViewHolder, position: Int) {
        viewHolder.txtFolio.text = datset[position].idPresupuestoNoRegistrado.toString()
        viewHolder.txtFecha.text = datset[position].fechaPresupuesto.toString()
        viewHolder.txtproblema.text = datset[position].problema
        viewHolder.txtNombreOficio.text = datset[position].nombreO
        /*if (datset[position].estaPagado == "1") {
            viewHolder.image.setImageResource(R.drawable.ic_clarity_email_outline_alerted)
        } else {
            viewHolder.image.setImageResource(R.drawable.ic_clarity_email_outline_badged)

        }*/
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.mensajes_recycler, viewGroup, false)

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