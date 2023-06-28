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
    private var lista = ArrayList<Mensaje>()
    var context = c

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
        holder.txt_mensaje.text = lista[position].mensaje
        holder.txt_fecha.text = lista[position].hora
        holder.txtMensajeLeido.text = lista[position].mensajeLeido

        var tipo_usuario = lista[position].tipo_usuario.trim()

        if (tipo_usuario == "cliente") {
                holder.txtMensajeLeido.visibility = View.VISIBLE
            if (lista[position].archivo == ""){
                println("no hay archivo")
            }else{
                if (lista[position].tipoArchivo == "imagen"){
                    holder.layoutArchivo.visibility = View.VISIBLE
                    holder.layoutArchivo.style {
                        this.layoutGravity(Gravity.END)
                    }
                    println("si hay archivo de imagen " + lista[position].archivo)
                    val photoUrl = Uri.parse(lista[position].archivo)
                    Picasso.get()
                        .load(photoUrl)
                        .resize(400,400)
                        .into(holder.imageViewArchivo)
                }
                if (lista[position].tipoArchivo == "pdf"){
                    println("si hay archivo pdf " + lista[position].archivo)
                    holder.layoutArchivo.visibility = View.VISIBLE
                    holder.layoutArchivo.style{
                        this.layoutGravity(Gravity.END)
                    }
                    holder.imageViewArchivo.setImageResource(R.drawable.icono_pdf)
                }
            }
                holder.layoutMensajeLeido.style {
                this.layoutGravity(Gravity.END)
                }
                holder.layoutmensaje.style {
                    this.backgroundRes(R.drawable.burbuja_chat_der)
                    this.layoutGravity(Gravity.END)
                }
                holder.layoutHora.style {
                    this.layoutGravity(Gravity.END)

                }

                if (position > 0) {
                    if (tipo_usuario == lista[position].tipo_usuario.trim()) {
                        holder.layoutmensaje.style {
                            this.layoutMarginTopDp(5)
                        }
                        holder.layoutHora.style {
                            this.layoutMarginTopDp(5)
                        }
                        holder.layoutMensajeLeido.style {
                            this.layoutMarginTopDp(5)
                        }
                    } else {
                        holder.layoutmensaje.style {
                            this.layoutMarginTopDp(5)
                        }
                        holder.layoutHora.style {
                            this.layoutMarginTopDp(5)
                        }
                        holder.layoutMensajeLeido.style {
                            this.layoutMarginTopDp(5)
                        }
                    }
                }

        }
        if (tipo_usuario == "Kerkly") {
            holder.txtMensajeLeido.visibility = View.GONE
            if (lista[position].archivo == ""){
                println("no hay archivo")
            }else{
                if (lista[position].tipoArchivo == "imagen"){
                    holder.layoutArchivo.visibility = View.VISIBLE
                    holder.layoutArchivo.style {
                        this.layoutGravity(Gravity.START)
                    }
                    val photoUrl = Uri.parse(lista[position].archivo)
                    println("si hay archivo $photoUrl")
                    Picasso.get()
                        .load(photoUrl)
                        .resize(400,400)
                        .into(holder.imageViewArchivo)
                }
                if (lista[position].tipoArchivo == "pdf"){
                    println("si hay archivo pdf " + lista[position].archivo)


                    holder.layoutArchivo.visibility = View.VISIBLE
                    holder.layoutArchivo.style{
                        this.layoutGravity(Gravity.START)
                }
                    holder.imageViewArchivo.setImageResource(R.drawable.icono_pdf)


                }
            }
                holder.layoutmensaje.style {
                    this.backgroundRes(R.drawable.burbuja_chat)
                    this.layoutGravity(Gravity.START)
                }
                holder.layoutHora.style{
                    this.layoutGravity(Gravity.START)
                }

                holder.layoutMensajeLeido.style{
                    this.layoutGravity(Gravity.START)
                }

                if (position > 0) {
                    if (tipo_usuario == lista[position-1].tipo_usuario.trim()) {
                        holder.layoutmensaje.style {
                            this.layoutMarginTopDp(5)
                        }
                        holder.layoutHora.style {
                            this.layoutMarginTopDp(5)
                        }
                        holder.layoutMensajeLeido.style {
                            this.layoutMarginTopDp(5)
                        }
                    } else {
                        holder.layoutmensaje.style {
                            this.layoutMarginTopDp(5)
                        }
                        holder.layoutHora.style {
                            this.layoutMarginTopDp(5)
                        }
                        holder.layoutMensajeLeido.style {
                            this.layoutMarginTopDp(5)
                        }
                    }
                }
        }
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
}