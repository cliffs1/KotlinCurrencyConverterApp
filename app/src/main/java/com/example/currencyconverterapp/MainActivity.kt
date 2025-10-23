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
    // UI state
    var amount by remember { mutableStateOf(300.0) }
    var rate by remember { mutableStateOf(11.50899) }
    var converted by remember { mutableStateOf(amount * rate) }

    Scaffold(
        topBar = {
            SimpleTopBar(title = "Currency Converter")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // From card
            SendingCard(
                countryName = "Poland",
                currencyCode = "PLN",
                amountText = "%.2f".format(amount),
                flagResId = R.drawable.flag_pl
            )

            // Conversion rate info
            Text(
                text = "Conversion rate: %.4f".format(rate),
                style = MaterialTheme.typography.bodyLarge
            )

            // To card
            SendingCard(
                countryName = "Ukraine",
                currencyCode = "UAH",
                amountText = "%.2f".format(converted),
                flagResId = R.drawable.flag_ua
            )
        }
    }
}

@Composable
fun SendingCard(
    countryName: String,
    currencyCode: String,
    amountText: String,
    flagResId: Int? = null,  // Use this for local drawable
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val flagModifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEFEFEF))

                if (flagResId != null) {
                    Image(
                        painter = painterResource(id = flagResId),
                        contentDescription = "$countryName flag",
                        modifier = flagModifier,
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = countryName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = currencyCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = amountText,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
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