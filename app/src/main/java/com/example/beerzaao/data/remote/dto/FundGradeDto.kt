package com.example.beerzaao.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FundGradeResponse(
    @SerializedName("Datas")
    val datas: List<FundGradeItem>?,
    @SerializedName("ErrCode")
    val errCode: Int,
    @SerializedName("ErrMsg")
    val errMsg: String?
)

data class FundGradeItem(
    @SerializedName("RDATE")
    val ratingDate: String?,
    @SerializedName("HTPJ")
    val haitongRating: String?,
    @SerializedName("ZSPJ")
    val zhaoshangRating: String?,
    @SerializedName("SZPJ3")
    val shanghai3YearRating: String?,
    @SerializedName("JAPJ")
    val jiaanRating: String?
)

data class FundGradeDto(
    val ratingDate: String?,
    val haitongRating: Int,
    val zhaoshangRating: Int,
    val shanghai3YearRating: Int,
    val jiaanRating: Int
)
