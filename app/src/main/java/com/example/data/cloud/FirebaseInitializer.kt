package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseInitializer {
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:517721338245:android:b7cc987f0d8e36bcd7d4ec")
                    .setProjectId("marwadi-wedding-planner")
                    .setApiKey("AIzaSyA80_pKO3_1d3nTB8_8Sd0EZZB4x0ftApw")
                    .setDatabaseUrl("https://marwadi-wedding-planner-default-rtdb.firebaseio.com")
                    .setStorageBucket("marwadi-wedding-planner.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(context.applicationContext, options)
                Log.d("FirebaseInitializer", "FirebaseApp initialized programmatically with active project")
            } else {
                Log.d("FirebaseInitializer", "FirebaseApp already initialized")
            }
            initialized = true
        } catch (e: Exception) {
            Log.w("FirebaseInitializer", "Firebase init error (graceful fallback): ${e.message}")
        }
    }
}
