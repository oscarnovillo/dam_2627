package com.example.micomposito

import android.annotation.SuppressLint
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.micomposito.domain.Pirata
import com.example.micomposito.ui.theme.MiCompositoTheme
import kotlinx.coroutines.launch

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


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun MainPantalla(modifier: Modifier = Modifier,
                 viewModel : MainViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember() { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error)
            }
            viewModel.limpiarError()
        }
    }

    MainPantallaContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onChangePirata = viewModel::onChangePirata, onAddPirata = viewModel::onAddPirata,
        modifier = modifier,
        onBorrarPirata = viewModel::onBorrarPirata,
        onShowError = {
            scope.launch {
                snackbarHostState.showSnackbar(it)
            }
        },
    )
}


@Composable
fun MainPantallaContent(
    modifier: Modifier = Modifier,
    state: MainState,
    snackbarHostState : SnackbarHostState,
    onChangePirata: (Pirata) -> Unit,
    onAddPirata: () -> Unit,
    onBorrarPirata : (Pirata) -> Unit,
    onShowError : (String) -> Unit,

    ) {




    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },


        modifier = Modifier.fillMaxSize()) { innerPadding ->

        Column {
            Cabecera(
                state = state,
                onAddPirata = onAddPirata,
                onChangePirata = onChangePirata,
                onSacarSnackbar = { texto ->
                   onShowError(texto)
                }
            ) {
                Text("hola Mundo desde compasbel")
            }
            Column(modifier = Modifier.padding(innerPadding)) {

                Cabecera(
                    state = state,
                    onAddPirata = onAddPirata,
                    onChangePirata = onChangePirata,
                    modifier = Modifier.weight(0.15F)
                )
                ListadoPiratas(state, onBorrarPirata)

                FilaDeMierda(
                    colorTexto = Color.Yellow,
                    modifier = Modifier
                        .weight(0.2F)
                        .background(Color.Black)
                )

            }
        }
    }


}

@Composable
private fun Cabecera(
    state: MainState,
    onAddPirata: () -> Unit,
    onChangePirata: (Pirata) -> Unit,
    onSacarSnackbar: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    Row(modifier = modifier) {
        Column (modifier = Modifier.weight(0.3F)){
            content()
            val stateTexto = TextFieldState(state.texto)
            ElevatedButton(onClick = {
                onSacarSnackbar("texto")
                onAddPirata()
            }) {

                Text("Add")
                OutlinedTextField(
                    value = state.pirata.nombre,
                    onValueChange = { texto: String -> onChangePirata(state.pirata.copy(nombre = texto)) },
                )
            }
        }
        if (state.loading) {
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

}

@Composable
private fun ColumnScope.ListadoPiratas(
    state: MainState,
    onBorrarPirata: (Pirata) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(1.dp),
        modifier = Modifier
            .weight(0.65F)
            .background(Color.Blue)
            .fillMaxWidth()
    ) {
        items(
            items = state.piratas,
            key = { pirata -> pirata.nombre } // clave estable: evita recomposiciones innecesarias
        ) { pirata ->
            ItemPirata(pirata, onBorrarPirata)

        }
    }
}

@Composable
fun ItemPirata(
    pirata: Pirata,
    onBorrar: (Pirata) -> Unit
) {
    Card(modifier= Modifier.fillMaxSize()
        ){
        Text( pirata.nombre ,
            modifier = Modifier.clickable{
                onBorrar(pirata)
            }
            )
    }
}

@Composable
fun FilaDeMierda(text : String = "Android",colorTexto : Color,modifier: Modifier = Modifier) {
    Column(modifier = modifier){
    (1..3).forEach {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(5F)
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
            onChangePirata = { },
            onBorrarPirata = {},
            onAddPirata = { },
            onShowError = {},
            snackbarHostState = SnackbarHostState(),

        )
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

            onChangePirata = { },
            onBorrarPirata = {},

            onAddPirata = { },
            onShowError = {},

            snackbarHostState = SnackbarHostState(),
        )
    }

}