/*
 * AuthInterceptor modified to call native refresh via NativeAuth when 401 encountered.
 */

package com.google.android.gms.auth.api.internal;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.Gson;

public class AuthInterceptor implements Interceptor {
    private final AuthStorage authStorage;
    private final Object refreshLock = new Object();
    private final String clientId;
    private final String clientSecret;

    public AuthInterceptor(AuthStorage authStorage, String clientId, String clientSecret) {
        this.authStorage = authStorage;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = authStorage.getToken();

        Request.Builder builder = original.newBuilder();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        Request request = builder.build();
        Response response = chain.proceed(request);

        if (response.code() == 401) {
            String refreshToken = authStorage.getRefreshToken();
            if (refreshToken != null && !refreshToken.isEmpty()) {
                synchronized (refreshLock) {
                    String newToken = authStorage.getToken();
                    if (newToken == null || newToken.equals(token)) {
                        try {
                            // Call native refresh
                            String json = NativeAuth.refreshToken(refreshToken, clientId, clientSecret);
                            Gson gson = new Gson();
                            LocalTokenResponse resp = gson.fromJson(json, LocalTokenResponse.class);
                            if (resp != null && resp.access_token != null) {
                                authStorage.saveTokensFromResponse(resp);
                                newToken = resp.access_token;
                            } else {
                                authStorage.clearAll();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            authStorage.clearAll();
                        }
                    }

                    String finalToken = authStorage.getToken();
                    if (finalToken != null && !finalToken.isEmpty()) {
                        Request newRequest = original.newBuilder()
                                .header("Authorization", "Bearer " + finalToken)
                                .build();
                        response.close();
                        return chain.proceed(newRequest);
                    }
                }
            }
        }

        return response;
    }
}
