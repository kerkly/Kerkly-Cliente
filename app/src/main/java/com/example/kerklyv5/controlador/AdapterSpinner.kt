package com.example.kerklyv5.controlador

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.TextView
import com.example.kerklyv5.R
import com.example.kerklyv5.modelo.serial.Oficio
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors

class AdapterSpinner(c:Context, l: List<Oficio>): BaseAdapter() {
    var contexto = c
    var lista = l

    override fun getCount(): Int {
        return lista.size
    }

    override fun getItem(position: Int): Any {
        return lista[position].nombreO
    }

    override fun getItemId(position: Int): Long {
        return 0

    }

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        var layout = LayoutInflater.from(contexto)
        view = layout.inflate(R.layout.spinner_s, null)

        var txt = view.findViewById<TextView>(R.id.tv_oficio)
        txt.text = lista[position].nombreO

        return view

    }

    fun filtrado(txtBuscar: String) {
        val inicio = "(?i)(\\W|^)("
        var pal = ""
        val final ="\\smía|ostras)(\\W|\$)"
        var oficio = ""
        for (i in 0 until lista!!.size){
            oficio = lista[i].nombreO
            pal = pal+ oficio +"|"
        }
        val expresion = "$inicio$pal$final"
        println("expresion armada $inicio"+pal+final)
        val patron: Pattern = Pattern.compile(expresion)
        val emparejador: Matcher = patron.matcher(txtBuscar)
        val esCoincidente = emparejador.find()
        if (esCoincidente) {
            println("texto Reconocido: ")
            val collecion: List<Oficio> = lista
            notifyDataSetChanged()

        }else{
            println("no reconocido: ")
        }
    }

}