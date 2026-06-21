package com.google.android.gms.auth.api.internal;

public class NativeAuth {
    static {
        try {
            System.loadLibrary("myauth");
        } catch (UnsatisfiedLinkError e) {
            // Library not found; native methods will fail. Fail gracefully.
            e.printStackTrace();
        }
    }

    public static native String exchangeCode(String code, String clientId, String clientSecret, String redirectUri);

    public static native String refreshToken(String refreshToken, String clientId, String clientSecret);
}
