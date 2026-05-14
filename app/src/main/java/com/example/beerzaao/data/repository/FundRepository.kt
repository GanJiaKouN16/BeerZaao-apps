package com.example.beerzaao.data.repository

import com.example.beerzaao.data.remote.ApiService
import com.example.beerzaao.data.remote.dto.FundEstimateDto
import com.example.beerzaao.data.remote.dto.StockQuoteItem
import com.example.beerzaao.data.remote.dto.StockQuoteData
import com.example.beerzaao.data.remote.dto.StockQuoteResponse
import com.example.beerzaao.util.FundPerformance
import com.example.beerzaao.util.HtmlParser
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FundRepository(private val apiService: ApiService) {

    suspend fun getFundEstimate(code: String): Result<FundEstimateDto> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://fundgz.1234567.com.cn/js/$code.js"
                val response = apiService.getFundEstimate(url)
                if (response.isSuccessful) {
                    val body = response.body()?.string() ?: ""
                    if (body.isBlank()) {
                        return@withContext Result.failure(Exception("暂无实时估值"))
                    }
                    val dto = Gson().fromJson(body, FundEstimateDto::class.java)
                    Result.success(dto)
                } else {
                    Result.failure(Exception("请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getFundHoldings(code: String): Result<List<Map<String, String>>> {
        return withContext(Dispatchers.IO) {
            try {
                val stockUrl = "https://fundf10.eastmoney.com/FundArchivesDatas.aspx?type=jjcc&code=$code&topline=20"
                val stockResponse = apiService.getFundHoldings(stockUrl)
                val holdings = if (stockResponse.isSuccessful) {
                    val raw = stockResponse.body() ?: ""
                    val html = extractHtmlContent(raw)
                    HtmlParser.parseHoldings(html).toMutableList()
                } else {
                    mutableListOf()
                }

                val bondUrl = "https://fundf10.eastmoney.com/FundArchivesDatas.aspx?type=zqcc&code=$code&topline=20"
                val bondResponse = apiService.getFundHoldings(bondUrl)
                if (bondResponse.isSuccessful) {
                    val raw = bondResponse.body() ?: ""
                    val html = extractHtmlContent(raw)
                    val bonds = HtmlParser.parseBondHoldings(html)
                    holdings.addAll(bonds)
                }

                Result.success(holdings)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getFundPerformance(code: String): Result<FundPerformance> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://fundf10.eastmoney.com/FundArchivesDatas.aspx?type=jdzf&code=$code&topline=5"
                val response = apiService.getFundHoldings(url)
                if (response.isSuccessful) {
                    val raw = response.body() ?: ""
                    val html = extractHtmlContent(raw)
                    val perf = HtmlParser.parsePerformance(html).copy(fundCode = code)
                    Result.success(perf)
                } else {
                    Result.failure(Exception("请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun extractHtmlContent(raw: String): String {
        val prefix = "content:\""
        val start = raw.indexOf(prefix)
        if (start < 0) return raw
        val contentStart = start + prefix.length
        val contentEnd = raw.indexOf("\",", contentStart)
        if (contentEnd < 0) return raw
        return raw.substring(contentStart, contentEnd)
    }

    suspend fun getStockQuotes(stockCodes: List<String>): Result<StockQuoteResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val query = stockCodes.joinToString(",") { convertToTencentCode(it) }
                val url = "https://qt.gtimg.cn/q=$query"
                val response = apiService.getTencentQuotes(url)
                if (response.isSuccessful) {
                    val body = response.body()?.string() ?: ""
                    val items = mutableListOf<StockQuoteItem>()
                    body.lineSequence().forEach { line ->
                        val trimmed = line.trim()
                        if (trimmed.startsWith("v_") && trimmed.contains("=")) {
                            val parts = trimmed.substringAfter("=").trim(' ', '"', ';')
                            val fields = parts.split("~")
                            if (fields.size >= 5) {
                                val code = fields[2].trim()
                                val name = fields[1].trim()
                                val current = fields[3].toDoubleOrNull() ?: 0.0
                                val lastClose = fields[4].toDoubleOrNull() ?: 0.0
                                val changeRate = if (lastClose > 0) {
                                    ((current - lastClose) / lastClose * 100)
                                } else 0.0
                                items.add(
                                    StockQuoteItem(
                                        currentPrice = current,
                                        changeRate = changeRate,
                                        changeAmount = current - lastClose,
                                        stockCode = code,
                                        stockName = name
                                    )
                                )
                            }
                        }
                    }
                    Result.success(StockQuoteResponse(data = StockQuoteData(diff = items)))
                } else {
                    Result.failure(Exception("请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun convertToTencentCode(stockCode: String): String {
        return when {
            stockCode.length == 5 -> "hk$stockCode"
            stockCode.startsWith("6") -> "sh$stockCode"
            else -> "sz$stockCode"
        }
    }

    suspend fun getFundName(code: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://qt.gtimg.cn/q=jj$code"
                val response = apiService.getTencentQuotes(url)
                if (response.isSuccessful) {
                    val body = response.body()?.string() ?: ""
                    val line = body.lineSequence().firstOrNull { it.trim().startsWith("v_jj$code") }
                    val parts = line?.trim()
                        ?.substringAfter("=")
                        ?.trim(' ', '"', ';')
                        ?.split("~") ?: emptyList()
                    val name = parts.getOrNull(1)?.trim()
                    if (!name.isNullOrBlank()) {
                        Result.success(name)
                    } else {
                        Result.failure(Exception("未找到基金"))
                    }
                } else {
                    Result.failure(Exception("请求失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
