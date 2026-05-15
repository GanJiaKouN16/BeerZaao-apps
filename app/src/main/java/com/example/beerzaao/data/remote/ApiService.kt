package com.example.beerzaao.data.remote

import com.example.beerzaao.data.remote.dto.FundGradeResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    @GET
    suspend fun getFundEstimate(@Url url: String): Response<ResponseBody>

    @GET
    suspend fun getFundHoldings(@Url url: String): Response<String>

    @GET
    suspend fun getTencentQuotes(@Url url: String): Response<ResponseBody>

    @GET
    suspend fun getFundGrade(@Url url: String): Response<FundGradeResponse>
}