package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private const val APP_NAME = "FuncityResortApp"

    const val PROJECT_ID = "funcityresort-a712a"
    const val API_KEY = "AIzaSyAiyGpl4rv-h2wBWXAM6rUpmBNgj704oOE"
    const val APP_ID = "1:352726123068:web:0352f4ad77bdb05d453939"
    const val STORAGE_BUCKET = "funcityresort-a712a.firebasestorage.app"
    const val GCM_SENDER_ID = "352726123068"

    private var firebaseApp: FirebaseApp? = null
    private var firestore: FirebaseFirestore? = null

    fun initialize(context: Context) {
        if (firebaseApp != null && firestore != null) return

        try {
            val existingApps = FirebaseApp.getApps(context)
            firebaseApp = existingApps.firstOrNull { it.name == APP_NAME || it.name == FirebaseApp.DEFAULT_APP_NAME }

            if (firebaseApp == null) {
                val options = FirebaseOptions.Builder()
                    .setApiKey(API_KEY)
                    .setApplicationId(APP_ID)
                    .setProjectId(PROJECT_ID)
                    .setStorageBucket(STORAGE_BUCKET)
                    .setGcmSenderId(GCM_SENDER_ID)
                    .build()

                firebaseApp = try {
                    FirebaseApp.initializeApp(context, options, APP_NAME)
                } catch (e: Exception) {
                    if (existingApps.isNotEmpty()) {
                        existingApps.first()
                    } else {
                        FirebaseApp.initializeApp(context, options)
                    }
                }
            }

            firebaseApp?.let { app ->
                firestore = FirebaseFirestore.getInstance(app).apply {
                    firestoreSettings = FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build()
                }
                Log.d(TAG, "Firebase initialized successfully with project: $PROJECT_ID")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase: ${e.message}", e)
        }
    }

    fun getFirestore(): FirebaseFirestore? {
        return firestore
    }

    fun isConnected(): Boolean = firestore != null
}
