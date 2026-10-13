package com.example.oftapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListadoBusquedaScreen(onExamSelected: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    val examenes = remember {
        listOf(

        )
    }
    val examenesFiltrados = remember(searchQuery, examenes){
        if(searchQuery.isBlank()){
            examenes
        }else{
            examenes.filter{
                it.paciente.contains(searchQuery, ignoreCase=true)||
                        it.tipoExamen.contains(searchQuery, ignoreCase=true)||
                        it.id.contains(searchQuery, ignoreCase=true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {Text("Buscar por paciente, examen o ID...")}
            leadingIcon =
        )
    }

}