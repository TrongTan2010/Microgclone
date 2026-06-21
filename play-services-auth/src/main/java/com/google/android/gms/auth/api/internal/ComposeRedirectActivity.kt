package com.google.android.gms.auth.api.internal

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ComposeRedirectActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data: Uri? = intent?.data
        if (data != null) {
            val code = data.getQueryParameter("code")
            val error = data.getQueryParameter("error")
            if (error != null) {
                Toast.makeText(this, "OAuth error: $error", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            if (code != null) {
                exchangeCodeForToken(code)
            } else {
                Toast.makeText(this, "No code in redirect", Toast.LENGTH_LONG).show()
                finish()
            }
        } else {
            finish()
        }
    }

    private fun exchangeCodeForToken(code: String) {
        val api = NetworkClient.getTokenRetrofit(this).create(GoogleAuthApi::class.java)
        val clientId = getString(R.string.oauth_client_id)
        val clientSecret = getString(R.string.oauth_client_secret)
        val redirectUri = getString(R.string.oauth_redirect_uri)

        val call: Call<LocalTokenResponse> = api.exchangeCodeForToken(code, clientId, clientSecret, redirectUri, "authorization_code")
        call.enqueue(object : Callback<LocalTokenResponse> {
            override fun onResponse(call: Call<LocalTokenResponse>, response: Response<LocalTokenResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val storage = AuthStorage(this@ComposeRedirectActivity)
                    storage.saveTokensFromResponse(body)
                    Toast.makeText(this@ComposeRedirectActivity, "Sign in successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ComposeRedirectActivity, "Token exchange failed: ${response.code()}", Toast.LENGTH_LONG).show()
                }
                finish()
            }

            override fun onFailure(call: Call<LocalTokenResponse>, t: Throwable) {
                Toast.makeText(this@ComposeRedirectActivity, "Token exchange error: ${t.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        })
    }
}
