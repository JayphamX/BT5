package com.example.coursemanager.data

import com.google.gson.annotations.SerializedName

// Mọi trường đều có giá trị mặc định để Gson không gặp null khi MockAPI thiếu field.
// id = null khi tạo mới -> Gson bỏ qua, MockAPI tự sinh id.

data class Category(
    @SerializedName(value = "id", alternate = ["_id", "Id", "ID"])
    val id: String? = null,
    val name: String = "",
    val description: String = ""
)

data class Course(
    @SerializedName(value = "id", alternate = ["_id", "Id", "ID"])
    val id: String? = null,
    val title: String = "",
    val description: String = "",
    val categoryId: String = "",
    val instructor: String = "",
    val price: Long = 0,
    val duration: Int = 0,
    val thumbnail: String = "",
    val trailerUrl: String = "",
    val createdAt: String = ""
)
