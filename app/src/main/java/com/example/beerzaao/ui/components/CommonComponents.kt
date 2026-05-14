package com.example.beerzaao.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.beerzaao.ui.theme.Brand
import com.example.beerzaao.ui.theme.CardBorder

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Brand,
            contentColor = Color.White,
            disabledContainerColor = CardBorder,
            disabledContentColor = Color.White
        ),
        border = BorderStroke(1.dp, Brand)
    ) {
        content()
    }
}
