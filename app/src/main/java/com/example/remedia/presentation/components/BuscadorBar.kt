package com.example.remedia.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction

/**
 * Barra de búsqueda sin estado propio: el texto lo guarda quien la usa
 * (con remember en la pantalla) y aquí solo se reportan los cambios y el "buscar".
 */
@Composable
fun BuscadorBar(
    texto: String,
    onTextoChange: (String) -> Unit,
    onBuscar: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Nombre del medicamento"
) {
    OutlinedTextField(
        value = texto,
        onValueChange = onTextoChange,
        modifier = modifier,
        singleLine = true,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
        trailingIcon = {
            if (texto.isNotEmpty()) {
                IconButton(onClick = { onTextoChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Borrar texto")
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onBuscar() })
    )
}
