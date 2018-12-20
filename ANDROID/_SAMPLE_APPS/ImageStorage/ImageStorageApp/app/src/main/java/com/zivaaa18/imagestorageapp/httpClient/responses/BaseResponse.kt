package com.zivaaa18.imagestorageapp.httpClient.responses

open class BaseResponse<T> {
    var statusCode : Int = 0
    var result : T? = null
}