package com.example.beerzaao.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.beerzaao.ui.theme.CardBg
import com.example.beerzaao.ui.theme.CardBorder
import com.example.beerzaao.ui.theme.StockDown
import com.example.beerzaao.ui.theme.StockUp

@Composable
fun HoldingList(
    holdings: List<HoldingItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "持仓明细",
            style = MaterialTheme.typography.titleMedium
        )
        holdings.forEach { holding ->
            HoldingCard(holding = holding)
        }
    }
}

@Composable
private fun HoldingCard(holding: HoldingItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = holding.stockCode + if (holding.type == "bond") " 债券" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = holding.stockName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "持仓: ${holding.holdingRate}%",
                    style = MaterialTheme.typography.bodySmall
                )

                if (holding.type == "stock") {
                    val changeRate = holding.quote?.changeRate ?: holding.changeRate.toDoubleOrNull()
                    if (changeRate != null) {
                        val color = if (changeRate >= 0) StockUp else StockDown
                        val sign = if (changeRate >= 0) "+" else ""
                        Text(
                            text = "$sign${String.format("%.2f", changeRate)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = color
                        )
                    }
                }
            }
        }
    }
}
