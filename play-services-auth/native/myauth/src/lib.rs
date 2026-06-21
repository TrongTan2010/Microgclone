use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;
use serde::{Deserialize, Serialize};

#[derive(Deserialize, Serialize)]
struct TokenResp {
    access_token: Option<String>,
    refresh_token: Option<String>,
    expires_in: Option<u64>,
    id_token: Option<String>,
    token_type: Option<String>,
    scope: Option<String>,
    error: Option<String>,
    error_description: Option<String>,
}

fn build_error_json(message: &str) -> String {
    let tr = TokenResp {
        access_token: None,
        refresh_token: None,
        expires_in: None,
        id_token: None,
        token_type: None,
        scope: None,
        error: Some("internal_error".to_string()),
        error_description: Some(message.to_string()),
    };
    serde_json::to_string(&tr).unwrap_or_else(|_| "{\"error\":\"serialize_error\"}".to_string())
}

#[no_mangle]
pub extern "system" fn Java_com_google_android_gms_auth_api_internal_NativeAuth_exchangeCode(
    env: JNIEnv,
    _class: JClass,
    jcode: JString,
    jclient_id: JString,
    jclient_secret: JString,
    jredirect: JString,
) -> jstring {
    let code: String = match env.get_string(&jcode) {
        Ok(s) => s.into(),
        Err(e) => return env.new_string(build_error_json(&format!("invalid_code_string: {}", e))).unwrap().into_inner(),
    };
    let client_id: String = match env.get_string(&jclient_id) {
        Ok(s) => s.into(),
        Err(e) => return env.new_string(build_error_json(&format!("invalid_client_id: {}", e))).unwrap().into_inner(),
    };
    let client_secret: String = match env.get_string(&jclient_secret) {
        Ok(s) => s.into(),
        Err(e) => return env.new_string(build_error_json(&format!("invalid_client_secret: {}", e))).unwrap().into_inner(),
    };
    let redirect: String = match env.get_string(&jredirect) {
        Ok(s) => s.into(),
        Err(e) => return env.new_string(build_error_json(&format!("invalid_redirect: {}", e))).unwrap().into_inner(),
    };

    let client = match reqwest::blocking::Client::builder().build() {
        Ok(c) => c,
        Err(e) => return env.new_string(build_error_json(&format!("client_build_error: {}", e))).unwrap().into_inner(),
    };

    let res = client
        .post("https://oauth2.googleapis.com/token")
        .form(&[
            ("code", code.as_str()),
            ("client_id", client_id.as_str()),
            ("client_secret", client_secret.as_str()),
            ("redirect_uri", redirect.as_str()),
            ("grant_type", "authorization_code"),
        ])
        .send();

    let json_str = match res {
        Ok(r) => match r.text() {
            Ok(text) => text,
            Err(e) => build_error_json(&format!("read_error: {}", e)),
        },
        Err(e) => build_error_json(&format!("request_error: {}", e)),
    };

    env.new_string(json_str).unwrap().into_inner()
}

#[no_mangle]
pub extern "system" fn Java_com_google_android_gms_auth_api_internal_NativeAuth_refreshToken(
    env: JNIEnv,
    _class: JClass,
    jrefresh: JString,
    jclient_id: JString,
    jclient_secret: JString,
) -> jstring {
    let refresh: String = match env.get_string(&jrefresh) {
        Ok(s) => s.into(),
        Err(e) => return env.new_string(build_error_json(&format!("invalid_refresh: {}", e))).unwrap().into_inner(),
    };
    let client_id: String = match env.get_string(&jclient_id) {
        Ok(s) => s.into(),
        Err(e) => return env.new_string(build_error_json(&format!("invalid_client_id: {}", e))).unwrap().into_inner(),
    };
    let client_secret: String = match env.get_string(&jclient_secret) {
        Ok(s) => s.into(),
        Err(e) => return env.new_string(build_error_json(&format!("invalid_client_secret: {}", e))).unwrap().into_inner(),
    };

    let client = match reqwest::blocking::Client::builder().build() {
        Ok(c) => c,
        Err(e) => return env.new_string(build_error_json(&format!("client_build_error: {}", e))).unwrap().into_inner(),
    };

    let res = client
        .post("https://oauth2.googleapis.com/token")
        .form(&[
            ("refresh_token", refresh.as_str()),
            ("client_id", client_id.as_str()),
            ("client_secret", client_secret.as_str()),
            ("grant_type", "refresh_token"),
        ])
        .send();

    let json_str = match res {
        Ok(r) => match r.text() {
            Ok(text) => text,
            Err(e) => build_error_json(&format!("read_error: {}", e)),
        },
        Err(e) => build_error_json(&format!("request_error: {}", e)),
    };

    env.new_string(json_str).unwrap().into_inner()
}
