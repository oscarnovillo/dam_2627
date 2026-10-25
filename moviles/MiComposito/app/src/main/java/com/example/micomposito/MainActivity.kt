package com.example.micomposito

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices.NEXUS_7
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        var texto by rememberSaveable{ mutableStateOf("pepe") }
        Column(modifier= Modifier.padding(innerPadding)) {
            ElevatedButton(onClick = {
                texto= "${texto.length} lll"

            }) {
                Text("Elevated")
            }


            FilaDeMierda(
                text = texto,
                colorTexto = Color.Yellow,
                modifier = Modifier.weight(1F)
                    .background(Color.Magenta)
                    .clickable(onClick = {})
            )
            FilaDeMierda(
                colorTexto = Color.Blue,
                modifier = Modifier
                    .weight(2F)
                    .background(Color.Black)
            )
            FilaDeMierda(colorTexto = Color.Yellow,
                modifier = Modifier
                    .weight(1F)
                    .background(Color.Black))

        }
    }
    
}

@Composable
fun FilaDeMierda(text : String = "Android",colorTexto : Color,modifier: Modifier = Modifier) {
    (1..3).forEach {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        )
        {
            Greeting(
                name = text,
                modifier = modifier
                    .background(colorTexto, CircleShape)



            )
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.error).weight(0.8F)) {
                Greeting(
                    name = "TuPutaMadre",
                    modifier = Modifier.align(Alignment.BottomCenter)

                )
                Greeting(
                    name = "TuPutaMadre2, el regreo",

                    )
            }
        }
    }
    
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        style= MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

@Preview(showBackground = true, device = "spec:parent=medium_phone,orientation=portrait",
    showSystemUi = true
)
@Composable
fun GreetingPreview() {

        MainPantalla()

}