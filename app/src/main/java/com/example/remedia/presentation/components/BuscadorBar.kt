package com.example.remedia.presentation.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.example.remedia.ui.theme.Borde
import com.example.remedia.ui.theme.Gris400
import com.example.remedia.ui.theme.Superficie
import com.example.remedia.ui.theme.TealPrimary

/**
 * Barra de búsqueda en píldora, sin estado propio: el texto lo guarda quien la usa
 * (con remember en la pantalla) y aquí solo se reportan los cambios y el "buscar".
 */
@Composable
fun BuscadorBar(
    texto: String,
    onTextoChange: (String) -> Unit,
    onBuscar: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Escribe el nombre del medicamento"
) {
    OutlinedTextField(
        value = texto,
        onValueChange = onTextoChange,
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(50),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Superficie,
            unfocusedContainerColor = Superficie,
            focusedBorderColor = TealPrimary,
            unfocusedBorderColor = Borde
        ),
        placeholder = { Text(placeholder, color = Gris400) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Buscar", tint = Gris400) },
        trailingIcon = {
            if (texto.isNotEmpty()) {
                IconButton(onClick = { onTextoChange("") }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Borrar texto", tint = Gris400)
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onBuscar() })
    )
}