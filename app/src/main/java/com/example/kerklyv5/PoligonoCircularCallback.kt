package com.example.kerklyv5

import com.example.kerklyv5.BaseDatosEspacial.geom

interface PoligonoCircularCallback {
    fun onConsultaIniciada()
    fun onConsultaFinalizada(resultados: List<geom>)
}