package com.fr0z863xf.fudisk.Utils;

import android.app.Application;

import android.util.Log;


import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateManager {

    public static UpdateManager instance;
    private Application application;
    public MutableLiveData<Boolean> needUpdate = new MutableLiveData<Boolean>(false);
    public String newVersion;
    public String releaseNotes;
    public String downloadLink;

    private UpdateManager(Application application) {
        this.application = application;
    }

    public static UpdateManager getInstance(@Nullable Application application) {
        if (instance == null && application != null) {
            instance = new UpdateManager(application);
        }
        return instance;
    }

    public void checkForUpdates() {
        // TODO: Implement update checking
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder().url("https://api.github.com/repos/fR0Z863xF/fudisk/releases/latest").get().build();
        try (Response response = client.newCall(request).execute()) {
            String res = response.body().string();
            if (!response.isSuccessful()) {
                Log.e("UpdateManager", "Failed to check for updates: " + res);
                throw new IOException("Unexpected code " + response);
            }
            UpdateResponse updateResponse = new Gson().fromJson(res, UpdateResponse.class);

            String newVersion = Objects.requireNonNullElse(updateResponse.tag_name, this.application.getPackageManager().getPackageInfo(this.application.getPackageName(),0).versionName);
            Log.i("UpdateManager", "Latest version: " + newVersion + "Current version: " + this.application.getPackageManager().getPackageInfo(this.application.getPackageName(),0).versionName);
            if (!newVersion.equals(this.application.getPackageManager().getPackageInfo(this.application.getPackageName(),0).versionName)) {
                this.needUpdate.postValue(true);
                this.newVersion = newVersion;
                this.releaseNotes = updateResponse.body;
                this.downloadLink = updateResponse.html_url;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

@Keep
class UpdateResponse {
    public String tag_name;
    public String html_url;
    public String body;


    public void setTag_name(String tag_name) {
        this.tag_name = tag_name;
    }
    public void setHtml_url(String html_url) {
        this.html_url = html_url;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public String getTag_name() {
        return tag_name;
    }
    public String getHtml_url() {
        return html_url;
    }
    public String getBody() {
        return body;
    }
}