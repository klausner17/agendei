package com.klausner.services

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import java.util.Collections

class GoogleAuthService(private val clientId: String) {
    
    private val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
        .setAudience(Collections.singletonList(clientId))
        .build()

    fun verifyIdToken(idTokenString: String): GoogleIdToken.Payload? {
        return try {
            val idToken = verifier.verify(idTokenString)
            idToken?.payload
        } catch (e: Exception) {
            null
        }
    }
}

