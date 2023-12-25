package com.example.kerklyv5.BaseDatosEspacial

import android.os.Parcel
import android.os.Parcelable

data class Kerkly(
    val idKerkly: String,
    val uidKerkly: String,
    var distancia: String,
    val latitud: Double,
    val longitud: Double

):Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble()
    )
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, p1: Int) {
        parcel.writeString(idKerkly)
        parcel.writeString(uidKerkly)
        parcel.writeString(distancia)
        parcel.writeDouble(latitud)
        parcel.writeDouble(longitud)
    }
    companion object CREATOR : Parcelable.Creator<Kerkly> {
        override fun createFromParcel(parcel: Parcel): Kerkly {
            return Kerkly(parcel)
        }

        override fun newArray(size: Int): Array<Kerkly?> {
            return arrayOfNulls(size)
        }
    }
}