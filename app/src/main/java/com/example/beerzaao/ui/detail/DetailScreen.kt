package com.example.beerzaao.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.beerzaao.ui.theme.CardBg
import com.example.beerzaao.ui.theme.CardBorder
import com.example.beerzaao.ui.theme.StockDown
import com.example.beerzaao.ui.theme.StockUp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    fundCode: String,
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(fundCode) {
        viewModel.loadFundDetail(fundCode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.fundName.ifEmpty { fundCode }) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        val swipeRefreshState = rememberSwipeRefreshState(uiState.isEstimateLoading)
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refreshEstimate() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                EstimateCard(
                    fundCode = fundCode,
                    estimate = uiState.estimate,
                    isLoading = uiState.isEstimateLoading,
                    error = uiState.estimateError,
                    weightedEstimateRate = uiState.weightedEstimateRate,
                )

                uiState.performance?.let { perf ->
                    Spacer(modifier = Modifier.height(16.dp))
                    PerformanceCard(performance = perf)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isHoldingsLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.holdingsError != null) {
                    Text(
                        text = "持仓加载失败: ${uiState.holdingsError}",
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (uiState.holdings.isNotEmpty()) {
                    HoldingList(holdings = uiState.holdings)
                }
            }
        }
    }
}

@Composable
private fun EstimateCard(
    fundCode: String,
    estimate: com.example.beerzaao.data.remote.dto.FundEstimateDto?,
    isLoading: Boolean,
    error: String?,
    weightedEstimateRate: Double?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = fundCode,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Monospace
            )
            if (estimate != null) {
                Text(
                    text = estimate.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (estimate != null) {
                val rate = estimate.estimateRate.toDoubleOrNull() ?: 0.0
                val color = if (rate >= 0) StockUp else StockDown
                val sign = if (rate >= 0) "+" else ""

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$sign${estimate.estimateRate}%",
                        style = MaterialTheme.typography.headlineLarge,
                        color = color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = estimate.estimateTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            } else if (weightedEstimateRate != null) {
                val rate = weightedEstimateRate
                val color = if (rate >= 0) StockUp else StockDown
                val sign = if (rate >= 0) "+" else ""

                Column {
                    Text(
                        text = "持仓加权估算",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$sign${String.format("%.2f", rate)}%",
                        style = MaterialTheme.typography.headlineLarge,
                        color = color
                    )
                }
            } else if (error != null && weightedEstimateRate == null) {
                Text(
                    text = "暂无估值",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PerformanceCard(
    performance: com.example.beerzaao.util.FundPerformance,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
            Text(
                text = "历史业绩",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PerformanceItem("近1月", performance.month1Rate)
                PerformanceItem("近3月", performance.month3Rate)
                PerformanceItem("近6月", performance.month6Rate)
                PerformanceItem("近1年", performance.year1Rate)
                PerformanceItem("近3年", performance.year3Rate)
            }
        }
    }
}

@Composable
private fun PerformanceItem(label: String, rateText: String) {
    val rate = rateText.toDoubleOrNull()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (rate != null) {
            val color = if (rate >= 0) StockUp else StockDown
            val sign = if (rate >= 0) "+" else ""
            Text(
                text = "$sign${String.format("%.2f", rate)}%",
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        } else {
            Text(
                text = rateText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
