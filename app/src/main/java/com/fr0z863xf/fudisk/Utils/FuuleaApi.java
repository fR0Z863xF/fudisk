package com.fr0z863xf.fudisk.Utils;

import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.*;


public class FuuleaApi {
    public static final String BASE_URL = "https://api.fuulea.com";
    public static final String c_version = "2.2.1";
    public static final String ua = "saturn/2.3.0 (Android 7.0/GOVENV) 2.3.0-base";

    public static boolean login(String username, String password,String sn,Integer gtype,String uuid) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        Request request = addCommonHeader(new Request.Builder()
                .url(BASE_URL + "/v2/auth/student/login/")
                .post(RequestBody.create("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}", MediaType.get("application/json; charset=utf-8"))), sn, mapGtype(gtype), uuid).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, IOException e) {
                Log.e("FuuleaApi", "登录失败：");
                AccountManager accountManager = AccountManager.getInstance(null);
                accountManager.accountStatus.postValue(2);
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, Response response) throws IOException {

                Log.d("FuuleaApi", response.toString());
                String res = response.body().string();
                Log.d("FuuleaApi", res);

                List<String> cookies = response.headers("set-cookie");

                LoginResponse loginResponse = new Gson().fromJson(res, LoginResponse.class);
                AccountManager accountManager = AccountManager.getInstance(null);
                if (!"".equals(loginResponse.getToken())) {
                    accountManager.setCredential(cookies.get(0)+"; "+cookies.get(1),loginResponse.token );
                    accountManager.accountStatus.postValue(2);
                    Log.i("FuuleaApi", "登录api成功");
                    return;
                }
                Log.w("FuuleaApi", "登录api失败:"+res);
                accountManager.accountStatus.postValue(3);
                return;


            }

        });



        return true;
    }

    public static void refresh(String cookies,String jwt,String username, String sn,Integer gtype,String uuid) {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        Request request = addAuthHeader(addCommonHeader(new Request.Builder()
                .url(BASE_URL + "/v2/auth/student/refresh/")
                .post(RequestBody.create("{}", MediaType.get("application/json; charset=utf-8"))), sn, mapGtype(gtype), uuid),cookies,jwt).build();


        try (Response response = client.newCall(request).execute()) {
            String res = response.body().string();
            if (!response.isSuccessful()) {
                Log.e("FuuleaApi", "refresh: " + request.headers());
                Log.w("FuuleaApi", "refresh: " + res);
                throw new IOException("Unexpected code " + response);
            }


            LoginResponse loginResponse = new Gson().fromJson(res, LoginResponse.class);
            AccountManager accountManager = AccountManager.getInstance(null);
            if (!"".equals(loginResponse.getToken())) {
                accountManager.setCredential(cookies,loginResponse.token );
                accountManager.accountStatus.postValue(2);
                Log.i("FuuleaApi", "刷新api成功");
                return;
            }
            Log.w("FuuleaApi", "刷新api失败:"+res);
            accountManager.accountStatus.postValue(3);
            return;
        } catch (IOException e) {
            Log.e("FuuleaApi", "刷新api失败");

            AccountManager accountManager = AccountManager.getInstance(null);
            accountManager.accountStatus.postValue(3);
            e.printStackTrace();

        }

    }

    public static void refreshCredential(String cookies,String jwt, String sn,Integer gtype,String uuid,String Tid) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        Request request = addAuthHeader(addCommonHeader(new Request.Builder()
                .url(BASE_URL + "/v2/cos-credential/?paperId=t" + Tid)
                .get(), sn, mapGtype(gtype), uuid),cookies,jwt).build();

        try (Response response = client.newCall(request).execute()) {
            String res = response.body().string();
            if (!response.isSuccessful()) {
                Log.e("FuuleaApi", "refreshCOS: " + request.headers());
                Log.w("FuuleaApi", "refreshCOS: " + res);
                throw new IOException("Unexpected code " + response);
            }
            CredentialsResponse cr = new Gson().fromJson(res, CredentialsResponse.class);
            AccountManager accountManager = AccountManager.getInstance(null);
            if("".equals(cr.credentials.getSessionToken()) || "".equals(cr.credentials.getTmpSecretId()) || "".equals(cr.credentials.getTmpSecretKey())) {
                Log.e("FuuleaApi", "刷新cos凭据失败：凭据为空");
            }
            accountManager.stoken = cr.credentials.getSessionToken();
            accountManager.sid = cr.credentials.getTmpSecretId();
            accountManager.skey = cr.credentials.getTmpSecretKey();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public static Request.Builder addCommonHeader(Request.Builder req, String sn,String gtype,String uuid) {
        return req.addHeader("User-Agent", ua.replace("GOVENV", gtype))
                .addHeader("serial", sn)
                .addHeader("Content-Type", "application/json")
                .addHeader("uuid", uuid)
                //.addHeader("fl-sec-sign",fl_sec_sign())
                .addHeader("app-version",c_version)
                .addHeader("version",c_version);
    }

    public static Request.Builder addAuthHeader(Request.Builder req,String cookie,String jwt) {
        return req.addHeader("Authorization", "jwt " + jwt)
                .addHeader("cookie", cookie);
    }

    private static String calculateMD5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(input.getBytes());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String fl_sec_sign() {

        /*
        之前逆向完写的源码丢了，先放个不能用的占位
         */

        long currentTimeMillis = System.currentTimeMillis();
        long seconds = currentTimeMillis / 1000;
        try {
            String a =  calculateMD5(String.valueOf(seconds));
            return a+","+String.valueOf(seconds);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "93cf46af70fcf71db51677654aab1b60,1132828292";
        }

    }

    public static String mapGtype(Integer gtype) {
        String a = "LingChuang";
        if (gtype == 1) {
            a = "HangZhi";
        } else if (gtype == 2) {
            a = "HEM";
        }
        return a;
    }

    public static Integer ramdomTid() {
        Integer a = (int) (Math.random() * 9000000 + 1000000);
        Log.i("FuuleaApi", "ramdomTid: " + a);
        return a;
    }
}

@Keep
class LoginResponse {
    public String token="";

    public String getToken() {
        Log.w("FuuleaApi", "token: " + Objects.requireNonNullElse(token, ""));
        return Objects.requireNonNullElse(token, "");
    }
    public void setToken(String token) {
        this.token = token;
    }

}

@Keep
class CredentialsResponse {
    public  Credentials credentials;

    public Credentials getCredentials() {
        return credentials;
    }
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
}

@Keep
class Credentials {
    public String sessionToken;
    public String tmpSecretId;
    public String tmpSecretKey;

    public String getSessionToken() {
        return Objects.requireNonNullElse(sessionToken, "");
    }
    public void setSessionToken(String sessionToken) {
         this.sessionToken = sessionToken;
    }

    public String getTmpSecretId() {
        return Objects.requireNonNullElse(tmpSecretId, "");
    }
    public void setTmpSecretId(String tmpSecretId) {
        this.tmpSecretId = tmpSecretId;
    }

    public String getTmpSecretKey() {
        return Objects.requireNonNullElse(tmpSecretKey, "");
    }
}