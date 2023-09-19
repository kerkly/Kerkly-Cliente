package com.example.kerklyv5.BaseDatosEspacial

import android.os.Parcel
import android.os.Parcelable

class geom(id_0: Int,geom: String): Parcelable {
    var id_0 : Int = id_0
    var geom: String = geom

    constructor() : this(0,"")


    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().toString(),)


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id_0)
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