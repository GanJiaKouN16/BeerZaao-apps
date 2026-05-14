package com.example.beerzaao.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FundEstimateDto(
    @SerializedName("fundcode")
    val fundCode: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("gsz")
    val currentUnitPrice: String,
    @SerializedName("gszzl")
    val estimateRate: String,
    @SerializedName("gztime")
    val estimateTime: String,
    @SerializedName("dwjz")
    val lastUnitPrice: String
)