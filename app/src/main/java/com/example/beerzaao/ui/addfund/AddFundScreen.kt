package com.example.beerzaao.ui.addfund

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.beerzaao.ui.components.AppButton
import com.example.beerzaao.ui.theme.Brand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFundScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddFundViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加基金") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = uiState.fundCode,
                onValueChange = { viewModel.updateFundCode(it) },
                label = { Text("基金代码") },
                placeholder = { Text("请输入6位数字") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.error != null,
                supportingText = if (uiState.error != null) {
                    { Text(uiState.error!!) }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Brand,
                    focusedLabelColor = Brand,
                    cursorColor = Brand
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.fundCode.length == 6 && !uiState.isValidating && !uiState.hasValidated) {
                AppButton(
                    onClick = { viewModel.validateFundCode() }
                ) {
                    Icon(Icons.Default.Check, contentDescription = "确认")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("确认查询")
                }
            }

            if (uiState.isValidating) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("正在验证...")
                }
            }

            if (uiState.hasValidated) {
                Spacer(modifier = Modifier.height(16.dp))
                if (uiState.fundName != null) {
                    Text(
                        text = uiState.fundName!!,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                AppButton(
                    onClick = { viewModel.addFund() },
                    enabled = !uiState.isAdding
                ) {
                    if (uiState.isAdding) {
                        CircularProgressIndicator()
                    } else {
                        Text("加入自选")
                    }
                }
            }

            if (uiState.isDuplicate) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "基金已在自选列表中",
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppButton(
                    onClick = { viewModel.resetState() }
                ) {
                    Text("重新输入")
                }
            }
        }
    }
}
