package com.zivaaa18.imagestorageapp.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "accesstoken")
data class AccessToken constructor(
    @Expose
    @NonNull
    @SerializedName("id")
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id", typeAffinity = ColumnInfo.TEXT)
    var id : String = ""
)