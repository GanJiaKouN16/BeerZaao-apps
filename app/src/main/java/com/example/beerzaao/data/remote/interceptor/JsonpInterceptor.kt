package com.example.beerzaao.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class JsonpInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val body = response.body?.string() ?: return response

        val trimmed = body.trim()
        val mediaType = response.body?.contentType()

        // 只处理 jsonpgz(...) 格式的 JSONP 响应
        if (trimmed.startsWith("jsonpgz(")) {
            val json = trimmed
                .removePrefix("jsonpgz(")
                .removeSuffix(");")

            val jsonMediaType = "application/json; charset=utf-8".toMediaType()
            return response.newBuilder()
                .body(json.toResponseBody(jsonMediaType))
                .build()
        }

        // 非 JSONP 响应，重新包装 body 避免已被消费
        return response.newBuilder()
            .body(body.toResponseBody(mediaType))
            .build()
    }
}