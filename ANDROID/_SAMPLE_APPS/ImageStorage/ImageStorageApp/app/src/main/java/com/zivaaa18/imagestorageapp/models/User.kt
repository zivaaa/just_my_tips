package com.zivaaa18.imagestorageapp.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class User {
    @Expose
    @SerializedName("id")
    var id : String = ""

    @Expose
    @SerializedName("name")
    var name : String = ""

    @Expose
    @SerializedName("email")
    var email : String = ""
}