package com.example.currencyconverterapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.currencyconverterapp.ui.theme.CurrencyConverterAppTheme
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapVert

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CurrencyConverterAppTheme {
                MainScreen()
            }
        }
    }
}

public suspend fun getFxRate(from: String, to: String, amount: Double): Double {
    return withContext(Dispatchers.IO) {
        try {
            val url = "https://my.transfergo.com/api/fx-rates?from=$from&to=$to&amount=$amount"
            val response = URL(url).readText()
            val json = JSONObject(response)
            json.getDouble("rate")
        } catch (e: Exception) {
            e.printStackTrace()
            1.0
        }
    }
}

@Composable
fun SimpleTopBar(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
fun MainScreen() {
    // State
    var fromCurrency by remember { mutableStateOf("PLN") }
    var toCurrency by remember { mutableStateOf("UAH") }
    var fromAmount by remember { mutableStateOf("300.0") }
    var toAmount by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf(11.50899) }
    var limitExceeded by remember { mutableStateOf(false) }
    var limitMessage by remember { mutableStateOf("") }

    fun updateFromAmount(newToAmount: String) {
        val numericTo = newToAmount.toDoubleOrNull() ?: 0.0
        fromAmount = if (rate != 0.0) "%.2f".format(numericTo / rate) else fromAmount
    }

    fun updateToAmount(newFromAmount: String) {
        val numericFrom = newFromAmount.toDoubleOrNull() ?: 0.0
        val limit = getLimitForCurrency(fromCurrency)

        if (numericFrom > limit) {
            limitExceeded = true
            limitMessage = "Limit for $fromCurrency is $limit"
        } else {
            limitExceeded = false
            limitMessage = ""
        }

        toAmount = "%.2f".format(numericFrom * rate)
    }

    LaunchedEffect(fromCurrency, toCurrency) {
        val numericAmount = fromAmount.toDoubleOrNull() ?: 1.0
        rate = getFxRate(fromCurrency, toCurrency, numericAmount)
        updateToAmount(fromAmount)
    }

    Scaffold(
        topBar = { SimpleTopBar(title = "Currency Converter") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SendingCardWithInput(
                labelText = "Sending from",
                currencyCode = fromCurrency,
                amountText = fromAmount,
                onAmountChange = {
                    fromAmount = it
                    updateToAmount(it)
                },
                onCurrencyChange = { newCurrency ->
                    fromCurrency = newCurrency
                    limitExceeded = false
                    limitMessage = ""
                },
                flagResId = getFlagForCurrency(fromCurrency),
                isLimitExceeded = limitExceeded
            )



            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        val tempCurrency = fromCurrency
                        fromCurrency = toCurrency
                        toCurrency = tempCurrency

                        val tempAmount = fromAmount
                        fromAmount = toAmount
                        toAmount = tempAmount
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap currencies",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    text = "1 $fromCurrency = %.4f $toCurrency".format(rate),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            SendingCardWithInput(
                labelText = "Receiver gets",
                currencyCode = toCurrency,
                amountText = toAmount,
                onAmountChange = {
                    toAmount = it
                    updateFromAmount(it)
                },
                onCurrencyChange = { newCurrency ->
                    toCurrency = newCurrency
                },
                flagResId = getFlagForCurrency(toCurrency)
            )
            if (limitExceeded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFE5E5))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = limitMessage,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SendingCardWithInput(
    labelText: String,
    currencyCode: String,
    amountText: String,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    flagResId: Int? = null,
    isLimitExceeded: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textColor = if (isLimitExceeded) Color.Red else Color.Unspecified

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        shape = RoundedCornerShape(12.dp),
        border = if (isLimitExceeded) BorderStroke(2.dp, Color.Red) else null,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = labelText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    if (flagResId != null) {
                        CurrencyDropdown(
                            selectedCurrency = currencyCode,
                            onCurrencySelected = onCurrencyChange,
                            flagResId = flagResId
                        )
                    }
                }

                TextField(
                    value = amountText,
                    onValueChange = { onAmountChange(it) },
                    singleLine = true,
                    modifier = Modifier.width(140.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
fun CurrencyDropdown(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    flagResId: Int
) {
    var expanded by remember { mutableStateOf(false) }
    val currencies = listOf("PLN", "EUR", "GBP", "UAH")

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { expanded = true }
        ) {
            Image(
                painter = painterResource(id = flagResId),
                contentDescription = "$selectedCurrency flag",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEFEFEF)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(selectedCurrency, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select currency")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencies.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency) },
                    onClick = {
                        onCurrencySelected(currency)
                        expanded = false
                    }
                )
            }
        }
    }
}
fun getFlagForCurrency(currency: String): Int {
    return when (currency) {
        "PLN" -> R.drawable.flag_pl
        "EUR" -> R.drawable.flag_ge
        "GBP" -> R.drawable.flag_gb
        "UAH" -> R.drawable.flag_ua
        else -> {return 0}
    }
}
fun getLimitForCurrency(currency: String): Double {
    return when (currency) {
        "PLN" -> 20000.0
        "EUR" -> 5000.0
        "GBP" -> 1000.0
        "UAH" -> 50000.0
        else -> Double.MAX_VALUE
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun MainScreenPreview() {
    CurrencyConverterAppTheme {
        MainScreen()
    }
}