package com.example.kerklyv5.modelo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.example.kerklyv5.R
import com.squareup.picasso.Picasso
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.lang.Exception


class adapterUsuarios(c: Context): RecyclerView.Adapter<adapterUsuarios.ViewHolder>() {
    var lista = ArrayList<usuarios>()
    private var context = c


    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
       val Correo = view.findViewById<TextView>(R.id.textViewCorreo)
       val nombre = view.findViewById<TextView>(R.id.textViewNombre)
       val activo = view.findViewById<TextView>(R.id.txtactivo)
       val imageViewFoto = view.findViewById<ImageView>(R.id.imageViewUsuarios)
        // System.out.println("adapter " + use.getNombre())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_usuarios, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {

        return lista.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.Correo.text = lista[position].correo.trim()
        holder.nombre.text = lista[position].nombre.trim()
        holder.activo.text = lista[position].fechaHora.trim()

        val photoUrl = Uri.parse(lista[position].foto.trim())
        println("nombre45 ${lista[position].nombre.trim()}")

        Picasso.get().load(photoUrl).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                System.out.println("Respuesta 1 ")
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                TODO("Not yet implemented")
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                val multi = MultiTransformation<Bitmap>(RoundedCornersTransformation(128, 0, RoundedCornersTransformation.CornerType.ALL))

                Glide.with(context).load(photoUrl)
                    .apply(RequestOptions.bitmapTransform(multi))
                    .into(holder.imageViewFoto)

            }

        })


    }


    fun agregarUsuario(usuarios: usuarios){
        lista.add(usuarios)
        println("tamaño de la lista: ${lista.size}")
        notifyItemInserted(lista.size)
    }

}