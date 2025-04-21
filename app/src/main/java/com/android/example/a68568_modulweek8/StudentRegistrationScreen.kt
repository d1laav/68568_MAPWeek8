package com.android.example.a68568_modulweek8

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StudentRegistrationScreen(viewModel: StudentViewModel = viewModel()) {
    var studentId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var program by remember { mutableStateOf("") }
    var currentPhone by remember { mutableStateOf("") }
    var phoneList by remember { mutableStateOf(listOf<String>()) }

    // for edited
    var isEditDialogOpen by remember { mutableStateOf(false) }
    var editedStudent by remember { mutableStateOf<Student?>(null) }
    var editedName by remember { mutableStateOf("") }
    var editedProgram by remember { mutableStateOf("") }
    var editedPhones by remember { mutableStateOf<List<String>>(emptyList()) }


    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {

        TextField(
            value = studentId,
            onValueChange = { studentId = it },
            label = { Text("Student ID") }
        )
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") }
        )
        TextField(
            value = program,
            onValueChange = { program = it },
            label = { Text("Program") }
        )

        // row buat nambahin nomor telepon
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = currentPhone,
                onValueChange = { currentPhone = it },
                label = { Text("Phone Number") },
            )
            Button(
                onClick = {
                    if (currentPhone.isNotBlank()) {
                        phoneList = phoneList + currentPhone
                        currentPhone = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Add")
            }
        }

        if (phoneList.isNotEmpty()) {
            Text("Phone Numbers:", style = MaterialTheme.typography.labelLarge)
            phoneList.forEach {
                Text("- $it")
            }
        }

        Button(
            onClick = {
                viewModel.addStudent(Student(studentId, name, program, phoneList))
                studentId = ""
                name = ""
                program = ""
                currentPhone = ""
                phoneList = listOf()
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Submit")
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        Text("Student List", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(viewModel.students) { student ->
                Text("${student.id} - ${student.name} - ${student.program}")
                if (student.phones.isNotEmpty()) {
                    Text("Phones:")
                    student.phones.forEach {
                        Text("- $it", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Row {
                    Button(onClick = {
                        editedStudent = student
                        editedName = student.name
                        editedProgram = student.program
                        editedPhones = student.phones
                        isEditDialogOpen = true
                    }, modifier = Modifier.padding(end = 8.dp)) {
                        Text("Edit")
                    }

                    Button(onClick = {
                        viewModel.deleteStudent(student.id)
                    }) {
                        Text("Delete")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        if (isEditDialogOpen && editedStudent != null) {
            AlertDialog(
                onDismissRequest = { isEditDialogOpen = false },
                title = { Text("Edit Student") },
                text = {
                    Column {
                        TextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = { Text("Name") }
                        )
                        TextField(
                            value = editedProgram,
                            onValueChange = { editedProgram = it },
                            label = { Text("Program") }
                        )

                        Text("Phone Numbers:")
                        editedPhones.forEachIndexed { index, phone ->
                            TextField(
                                value = phone,
                                onValueChange = { newPhone ->
                                    editedPhones = editedPhones.toMutableList().also { it[index] = newPhone }
                                },
                                label = { Text("Phone ${index + 1}") },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.updateStudent(
                            studentId = editedStudent!!.id,
                            newName = editedName,
                            newProgram = editedProgram,
                            newPhones = editedPhones
                        )
                        isEditDialogOpen = false
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        isEditDialogOpen = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

    }
}