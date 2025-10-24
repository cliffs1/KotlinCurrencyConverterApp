package com.example.currencyconverterapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

private suspend fun getFxRate(from: String, to: String, amount: Double): Double {
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
    var fromCurrency by remember { mutableStateOf("PLN") }
    var toCurrency by remember { mutableStateOf("UAH") }
    var amount by remember { mutableStateOf("300.0") }
    var rate by remember { mutableStateOf(1.0) }

    // Fetch rate whenever user changes currency or amount
    LaunchedEffect(fromCurrency, toCurrency, amount) {
        val numericAmount = amount.toDoubleOrNull() ?: 0.0
        if (numericAmount > 0) {
            rate = getFxRate(fromCurrency, toCurrency, numericAmount)
        }
    }

    val converted = remember(amount, rate) {
        val numericAmount = amount.toDoubleOrNull() ?: 0.0
        numericAmount * rate
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
                selectedCurrency = fromCurrency,
                onCurrencySelected = { fromCurrency = it },
                amountText = amount,
                onAmountChange = { amount = it }
            )

            Text(
                text = "Conversion rate: %.4f".format(rate),
                style = MaterialTheme.typography.bodyLarge
            )

            SendingCard(
                selectedCurrency = toCurrency,
                onCurrencySelected = { toCurrency = it },
                amountText = "%.2f".format(converted)
            )
        }
    }
}

@Composable
fun SendingCardWithInput(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    amountText: String,
    onAmountChange: (String) -> Unit
) {
    val currencyFlags = mapOf(
        "PLN" to R.drawable.flag_pl,
        "EUR" to R.drawable.flag_ge,
        "GBP" to R.drawable.flag_gb,
        "UAH" to R.drawable.flag_ua
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CurrencyDropdown(
                selectedCurrency = selectedCurrency,
                onCurrencySelected = onCurrencySelected,
                flagResId = currencyFlags[selectedCurrency] ?: R.drawable.flag_pl
            )

            TextField(
                value = amountText,
                onValueChange = { onAmountChange(it) },
                singleLine = true,
                modifier = Modifier.width(120.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
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

@Composable
fun SendingCard(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    amountText: String
) {
    val currencyFlags = mapOf(
        "PLN" to R.drawable.flag_pl,
        "EUR" to R.drawable.flag_ge,
        "GBP" to R.drawable.flag_gb,
        "UAH" to R.drawable.flag_ua
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CurrencyDropdown(
                selectedCurrency = selectedCurrency,
                onCurrencySelected = onCurrencySelected,
                flagResId = currencyFlags[selectedCurrency] ?: R.drawable.flag_pl
            )

            Text(
                text = amountText,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
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

@Preview(showBackground = true, widthDp = 360)
@Composable
fun MainScreenPreview() {
    CurrencyConverterAppTheme {
        MainScreen()
    }
}