package com.example.beerzaao.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StockQuoteResponse(
    @SerializedName("data")
    val data: StockQuoteData?
)

data class StockQuoteData(
    @SerializedName("diff")
    val diff: List<StockQuoteItem>?
)

data class StockQuoteItem(
    @SerializedName("f2")
    val currentPrice: Double?,
    @SerializedName("f3")
    val changeRate: Double?,
    @SerializedName("f4")
    val changeAmount: Double?,
    @SerializedName("f12")
    val stockCode: String?,
    @SerializedName("f14")
    val stockName: String?
)