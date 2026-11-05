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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.micomposito.domain.Pirata
import com.example.micomposito.ui.theme.MiCompositoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent   {
            MiCompositoTheme {
                MainPantalla()
            }
        }
    }
}


@Composable
fun MainPantalla(modifier: Modifier = Modifier,
                 viewModel : MainViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()


    MainPantallaContent(
        modifier = modifier,
        state = state,
        onChangePirata = viewModel::onChangePirata, onAddPirata = viewModel::onAddPirata,
    )
}


@Composable
fun MainPantallaContent(
    modifier: Modifier = Modifier,
    state: MainState,
    onChangePirata: (Pirata) -> Unit,
    onAddPirata: () -> Unit,

    ) {

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

        Column(modifier= Modifier.padding(innerPadding)) {

            Row {
                val stateTexto =TextFieldState(state.texto)
                ElevatedButton(onClick = {

                    onAddPirata()
                }) {
                    Text("Add")
                }
                if (state.loading){
                    CircularProgressIndicator()
                }
                Column() {
                    OutlinedTextField(
                        value = state.pirata.nombre,
                        onValueChange = { texto: String -> onChangePirata(state.pirata.copy(nombre = texto)) },
                    )
                    OutlinedTextField(
                        state = state.textStatePirata,

                        )
                }


            }

            FilaDeMierda(
                text = state.texto,
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
            Box(modifier = Modifier
                .background(MaterialTheme.colorScheme.error)
                .weight(0.8F)) {
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
    showSystemUi = true, name = "loading"
)
@Composable
fun GreetingPreview() {
    MiCompositoTheme {
        MainPantallaContent(
            state = MainState(
                loading = true,
                textStatePirata = TextFieldState("mierda"),
                pirata = Pirata("Zoro", "Ronoroa")
            ),
            onChangePirata = TODO(),
        ) { }
    }

}


@Preview(showBackground = true, device = "spec:parent=medium_phone,orientation=portrait",
    showSystemUi = true, name = "not loading"
)
@Composable
fun GreetingPreviewNotLoading() {
    MiCompositoTheme {
        MainPantallaContent(
            state = MainState(
                texto = "uno",
                loading = false,
                textStatePirata = TextFieldState("mierda"),
                pirata = Pirata("Nami", "")
            ),

            onChangePirata = TODO(),
        ) { }
    }

}