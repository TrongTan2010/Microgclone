package com.google.android.gms.auth.api.internal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.internal.R

class ComposeLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = getString(R.string.app_name))

                        Button(
                            onClick = { startAuthFlow() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(text = "Sign in with Google")
                        }

                        Button(
                            onClick = { signOut() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(text = "Sign out")
                        }
                    }
                }
            }
        }
    }

    private fun startAuthFlow() {
        val clientId = getString(R.string.oauth_client_id)
        val redirectUri = getString(R.string.oauth_redirect_uri)
        val scope = "openid email profile"

        val authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
            + "?client_id=" + Uri.encode(clientId)
            + "&response_type=code"
            + "&scope=" + Uri.encode(scope)
            + "&redirect_uri=" + Uri.encode(redirectUri)
            + "&access_type=offline"
            + "&prompt=consent"

        val i = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
        startActivity(i)
    }

    private fun signOut() {
        val storage = AuthStorage(this)
        storage.clearAll()
        Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
    }
}
