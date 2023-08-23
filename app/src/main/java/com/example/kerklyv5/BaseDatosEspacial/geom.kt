package com.example.kerklyv5.BaseDatosEspacial

import android.os.Parcel
import android.os.Parcelable

class geom(id_0: String,geom: String): Parcelable {
    var id_0 : String = id_0
    var geom: String = geom

    constructor() : this("","")


    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),)


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id_0)
        parcel.writeString(geom)
    }
    override fun describeContents(): Int {
        return 0
    }
    companion object CREATOR : Parcelable.Creator<geom> {
        override fun createFromParcel(parcel: Parcel): geom {
            return geom(parcel)
        }

        override fun newArray(size: Int): Array<geom?> {
            return arrayOfNulls(size)
        }
    }


}