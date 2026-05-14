package com.example.beerzaao.util

data class FundPerformance(
    val fundCode: String,
    val yearlyRate: String = "",
    val week1Rate: String = "",
    val month1Rate: String = "",
    val month3Rate: String = "",
    val month6Rate: String = "",
    val year1Rate: String = "",
    val year2Rate: String = "",
    val year3Rate: String = "",
    val year5Rate: String = "",
    val sinceFoundRate: String = ""
)

object HtmlParser {
    fun parsePerformance(html: String): FundPerformance {
        var result = FundPerformance(fundCode = "")
        val ulPattern = Regex("<ul[^>]*>(.*?)</ul>", RegexOption.DOT_MATCHES_ALL)
        val periodRegex = Regex("<li class='title'>(.*?)</li>")
        val rateRegex = Regex("<li class='tor[^']*'[^>]*>(.*?)</li>")

        val uls = ulPattern.findAll(html)
        uls.forEach { ul ->
            val period = periodRegex.find(ul.value)?.groupValues?.getOrNull(1)?.trim() ?: return@forEach
            val rate = rateRegex.find(ul.value)?.groupValues?.getOrNull(1)
                ?.replace(Regex("<[^>]+>"), "")?.trim()?.replace("%", "") ?: return@forEach
            result = when (period) {
                "今年来" -> result.copy(yearlyRate = rate)
                "近1周" -> result.copy(week1Rate = rate)
                "近1月" -> result.copy(month1Rate = rate)
                "近3月" -> result.copy(month3Rate = rate)
                "近6月" -> result.copy(month6Rate = rate)
                "近1年" -> result.copy(year1Rate = rate)
                "近2年" -> result.copy(year2Rate = rate)
                "近3年" -> result.copy(year3Rate = rate)
                "近5年" -> result.copy(year5Rate = rate)
                "成立来" -> result.copy(sinceFoundRate = rate)
                else -> result
            }
        }

        return result
    }

    fun parseHoldings(html: String): List<Map<String, String>> {
        val holdings = mutableListOf<Map<String, String>>()

        val tbodyMatch = Regex("<tbody[^>]*>(.*?)</tbody>", RegexOption.DOT_MATCHES_ALL)
            .find(html)
        val tableBody = tbodyMatch?.groupValues?.getOrNull(1) ?: html

        val trPattern = Regex("<tr[^>]*>(.*?)</tr>", RegexOption.DOT_MATCHES_ALL)
        val tdPattern = Regex("<td[^>]*>(.*?)</td>", RegexOption.DOT_MATCHES_ALL)

        val trMatches = trPattern.findAll(tableBody)
        trMatches.forEach { trMatch ->
            val row = trMatch.groupValues[1]
            val tdMatches = tdPattern.findAll(row).toList()

            if (tdMatches.size >= 7) {
                val stockCode = tdMatches[1].groupValues[1]
                    .replace(Regex("<[^>]+>"), "").trim()
                val stockName = tdMatches[2].groupValues[1]
                    .replace(Regex("<[^>]+>"), "").trim()
                val holdingRate = tdMatches[6].groupValues[1]
                    .replace(Regex("<[^>]+>"), "").trim()
                    .replace("%", "")

                if (stockCode.isNotEmpty() && stockName.isNotEmpty()) {
                    holdings.add(
                        mapOf(
                            "stockCode" to stockCode,
                            "stockName" to stockName,
                            "holdingRate" to holdingRate,
                            "changeRate" to "",
                            "type" to "stock"
                        )
                    )
                }
            }
        }

        return holdings
    }

    fun parseBondHoldings(html: String): List<Map<String, String>> {
        val bonds = mutableListOf<Map<String, String>>()

        val tbodyMatch = Regex("<tbody[^>]*>(.*?)</tbody>", RegexOption.DOT_MATCHES_ALL)
            .find(html)
        val tableBody = tbodyMatch?.groupValues?.getOrNull(1) ?: html

        val trPattern = Regex("<tr[^>]*>(.*?)</tr>", RegexOption.DOT_MATCHES_ALL)
        val tdPattern = Regex("<td[^>]*>(.*?)</td>", RegexOption.DOT_MATCHES_ALL)

        val trMatches = trPattern.findAll(tableBody)
        trMatches.forEach { trMatch ->
            val row = trMatch.groupValues[1]
            val tdMatches = tdPattern.findAll(row).toList()

            if (tdMatches.size >= 4) {
                val bondCode = tdMatches[1].groupValues[1]
                    .replace(Regex("<[^>]+>"), "").trim()
                val bondName = tdMatches[2].groupValues[1]
                    .replace(Regex("<[^>]+>"), "").trim()
                val holdingRate = tdMatches[3].groupValues[1]
                    .replace(Regex("<[^>]+>"), "").trim()
                    .replace("%", "")

                if (bondCode.isNotEmpty() && bondName.isNotEmpty()) {
                    bonds.add(
                        mapOf(
                            "stockCode" to bondCode,
                            "stockName" to bondName,
                            "holdingRate" to holdingRate,
                            "changeRate" to "",
                            "type" to "bond"
                        )
                    )
                }
            }
        }

        return bonds
    }
}
