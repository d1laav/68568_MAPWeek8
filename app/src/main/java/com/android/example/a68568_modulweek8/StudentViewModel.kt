package com.android.example.a68568_modulweek8

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class StudentViewModel : ViewModel() {
    private val db = Firebase.firestore
    var students by mutableStateOf(listOf<Student>())
        private set

    init {
        fetchStudents()
    }

    fun addStudent(student: Student) {
        val studentMap = hashMapOf(
            "id" to student.id,
            "name" to student.name,
            "program" to student.program
        )

        db.collection("students")
            .add(studentMap)
            .addOnSuccessListener { docRef ->
                Log.d("Firestore", "DocumentSnapshot added with ID: ${docRef.id}")

                // new subcollection for phones
                student.phones.forEach { phone ->
                    val phoneMap = hashMapOf("number" to phone)
                    docRef.collection("phones")
                        .add(phoneMap)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Phone added: $phone")
                        }
                        .addOnFailureListener { e: Exception ->
                            Log.e("Firestore", "Failed to add phone: $phone", e)
                        }
                }

                fetchStudents()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
            }
    }

    private fun fetchStudents() {
        db.collection("students")
            .get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<Student>()
                for (document in result) {
                    val id = document.getString("id") ?: ""
                    val name = document.getString("name") ?: ""
                    val program = document.getString("program") ?: ""
                    val docRef = document.reference

                    // subcollection phones
                    docRef.collection("phones")
                        .get()
                        .addOnSuccessListener { phoneDocs ->
                            val phoneList = phoneDocs.mapNotNull { it.getString("number") }
                            list.add(Student(id, name, program, phoneList))
                            // sort by id (awalnya name)
                            students = list.sortedBy { it.id }
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents.", exception)
            }
    }
}