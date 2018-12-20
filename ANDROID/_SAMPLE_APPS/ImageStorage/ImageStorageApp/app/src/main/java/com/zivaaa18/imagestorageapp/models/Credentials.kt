package com.zivaaa18.imagestorageapp.models

import com.google.gson.annotations.SerializedName

data class Credentials(
    @SerializedName("email") var email : String,
    @SerializedName("password") var password : String
)