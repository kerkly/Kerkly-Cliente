package com.example.kerklyv5.controlador

import android.content.Context
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.paris.extensions.*
import com.example.kerklyv5.R
import com.example.kerklyv5.modelo.Mensaje
import com.squareup.picasso.Picasso

class AdapterChat(c: Context): RecyclerView.Adapter<AdapterChat.ViewHolder>() {
     var lista = ArrayList<Mensaje>()
    var context = c

    companion object {
        const val VIEW_TYPE_TEXTO = 1
        const val VIEW_TYPE_IMAGEN = 2
        const val VIEW_TYPE_PDF = 3
    }
    override fun getItemViewType(position: Int): Int {
        val mensaje = lista[position]
        return when {
            mensaje.tipoArchivo == "imagen" -> VIEW_TYPE_IMAGEN
            mensaje.tipoArchivo == "pdf" -> VIEW_TYPE_PDF
            else -> VIEW_TYPE_TEXTO
        }
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

       val txt_mensaje = view.findViewById<TextView>(R.id.txt_mensaje_chat)
        val txt_fecha = view.findViewById<TextView>(R.id.txt_fechaMensaje_chat)
        var layoutmensaje = view.findViewById<LinearLayout>(R.id.layout_mensaje_card)
        var layoutHora = view.findViewById<LinearLayout>(R.id.layout_hora_card)
        var txtMensajeLeido = view.findViewById<TextView>(R.id.txtMensajeLeido)
        var layoutMensajeLeido = view.findViewById<LinearLayout>(R.id.layout_MensajeLeido)
        var layoutArchivo = view.findViewById<LinearLayout>(R.id.layoutArchivo)
        var imageViewArchivo = view.findViewById<ImageView>(R.id.imageViewArchivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_mensaje, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mensaje = lista[position]

        holder.txt_mensaje.text = mensaje.mensaje
        holder.txt_fecha.text = mensaje.hora
        holder.txtMensajeLeido.text = mensaje.mensajeLeido

        var tipo_usuario = mensaje.tipo_usuario.trim()
        val esCliente = tipo_usuario == "cliente"

        with(holder.layoutmensaje) {
            setStyleAndVisibility(if (esCliente) Gravity.END else Gravity.START, 5)
            this.setBackgroundResource(if (esCliente) R.drawable.burbuja_chat_der else R.drawable.burbuja_chat)
        }
        with(holder.layoutHora) {
            setStyleAndVisibility(if (esCliente) Gravity.END else Gravity.START, 0)
        }
        with(holder.layoutMensajeLeido) {
            setStyleAndVisibility(if (esCliente) android.view.Gravity.END else android.view.Gravity.START, 0)
        }

        with(holder.txtMensajeLeido) {
            setStyleAndVisibility(if (esCliente) Gravity.END else Gravity.START, 0)
        }

        with(holder.layoutArchivo) {
            setVisibility(mensaje.archivo.isNotEmpty())
            setStyleAndVisibility(if (esCliente) Gravity.END else Gravity.START, 0)
        }

        holder.imageViewArchivo.setImageResource(
            when (mensaje.tipoArchivo) {
                "imagen" -> R.drawable.descargaimagen
                "pdf" -> R.drawable.icono_pdf
                else -> R.drawable.descargaimagen
            }
        )

    }

    override fun getItemCount(): Int {
        return lista.size
    }
    fun addMensaje(m: Mensaje) {
        lista.add(m)
        notifyItemInserted(lista.size)
    }

    fun addMensajeClear() {
        var tam = lista.size
        lista.remove(lista.get(tam-1))
        notifyItemInserted(lista.size)
    }

    private fun View.setStyleAndVisibility(gravity: Int, margin: Int) {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.gravity = gravity
        params.topMargin = margin // Ajusta el margen superior aquí
        this.layoutParams = params
    }



    private fun View.setVisibility(isVisible: Boolean) {
        this.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}