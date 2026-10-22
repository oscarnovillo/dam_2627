package com.example.micomposito

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices.NEXUS_7
import androidx.compose.ui.tooling.preview.Preview
import com.example.micomposito.ui.theme.MiCompositoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent( content={
            MiCompositoTheme {
                MainPantalla()
            }
        })
    }
}

@Composable
fun MainPantalla(modifier: Modifier = Modifier) {


    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize())
        {
            Greeting(
                name = "Android",
                modifier = Modifier.padding(innerPadding)
            )
            Greeting(
                name = "TuPutaMadre",
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
    
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true, device = "spec:parent=Nexus 7,orientation=landscape",
    showSystemUi = true
)
@Composable
fun GreetingPreview() {
    MiCompositoTheme {
        MainPantalla()
    }
}