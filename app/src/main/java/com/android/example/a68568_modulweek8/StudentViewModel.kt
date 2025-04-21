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

    fun deleteStudent(studentId: String) {
        db.collection("students")
            .whereEqualTo("id", studentId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val studentRef = document.reference

                    // delete subcollection phones
                    studentRef.collection("phones")
                        .get()
                        .addOnSuccessListener { phoneDocs ->
                            for (phoneDoc in phoneDocs) {
                                phoneDoc.reference.delete()
                            }
                            // delete student collection
                            studentRef.delete()
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Student deleted: $studentId")
                                    fetchStudents()
                                }
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error deleting student", e)
            }
    }

    fun updateStudent(studentId: String, newName: String, newProgram: String, newPhones: List<String>) {
        db.collection("students")
            .whereEqualTo("id", studentId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val ref = document.reference

                    // Update name & program
                    ref.update(mapOf("name" to newName, "program" to newProgram))
                        .addOnSuccessListener {
                            Log.d("Firestore", "Student updated: $studentId")

                            // Hapus phones lama lalu masukkan yang baru
                            ref.collection("phones")
                                .get()
                                .addOnSuccessListener { phoneDocs ->
                                    for (doc in phoneDocs) {
                                        doc.reference.delete()
                                    }

                                    // Tambahkan phones baru
                                    newPhones.forEach { phone ->
                                        val phoneMap = hashMapOf("number" to phone)
                                        ref.collection("phones").add(phoneMap)
                                    }

                                    fetchStudents()
                                }
                        }
                }
            }
    }

}