package com.elsewhere.eyris.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

object FirestoreUtils {
    fun handleFirestoreError(e: Exception, operationType: OperationType, path: String?, auth: FirebaseAuth) {
        val authInfo = JSONObject().apply {
            put("userId", auth.currentUser?.uid)
            put("email", auth.currentUser?.email)
            put("emailVerified", auth.currentUser?.isEmailVerified)
            put("isAnonymous", auth.currentUser?.isAnonymous)
            put("tenantId", auth.currentUser?.tenantId)
            val providerInfo = JSONArray()
            auth.currentUser?.providerData?.forEach { provider ->
                providerInfo.put(JSONObject().apply {
                    put("providerId", provider.providerId)
                    put("email", provider.email)
                })
            }
            put("providerInfo", providerInfo)
        }

        val errInfo = JSONObject().apply {
            put("error", e.message ?: e.toString())
            put("operationType", operationType.value)
            put("path", path)
            put("authInfo", authInfo)
        }

        val jsonString = errInfo.toString()
        Log.e("FirestoreError", jsonString)
        throw Exception(jsonString)
    }
}
