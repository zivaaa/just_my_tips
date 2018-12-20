package com.zivaaa18.imagestorageapp.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.zivaaa18.imagestorageapp.httpClient.ApiClient

class SharedImage(
//    @Expose
//    var id : Long? = null,

    @SerializedName(value = "id")
    var id : String = "",
//    var remoteId : String = "",

    var accessType : Int = 0
) {
    fun getApiPath() : String {
        return "${ApiClient.BASE_URL}images/source/${id}"
    }
}