package com.zivaaa18.imagestorageapp.httpClient.responses

import com.zivaaa18.imagestorageapp.models.SharedImage

//class SharedImageResponse : BaseResponse<List<SharedImage>>() {
//
//}

class SharedImageResponse {
    var statusCode : Int = 0
    var result : List<SharedImage>? = null
}

class SimpleSuccessResponse {
    var statusCode : Int = 0
}