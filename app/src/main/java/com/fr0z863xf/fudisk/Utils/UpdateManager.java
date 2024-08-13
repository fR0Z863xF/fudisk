package com.fr0z863xf.fudisk.Utils;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.compose.material3.AlertDialogDefaults;
import androidx.compose.material3.AlertDialogKt;
import androidx.window.core.BuildConfig;

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
            Log.i("UpdateManager", "Latest version: " + updateResponse.tag_name);
            String newVersion = Objects.requireNonNullElse(updateResponse.tag_name, this.application.getPackageManager().getPackageInfo(this.application.getPackageName(),0).versionName);
            if (!newVersion.equals(this.application.getPackageManager().getPackageInfo(this.application.getPackageName(),0).versionName)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this.application);
                builder.setTitle("软件更新");
                builder.setMessage("发现新版本，更新日志：\n" + updateResponse.body );
                builder.setCancelable(false);
                builder.setPositiveButton("立即更新", (dialog, which) -> {
                    //复制链接
                    ClipboardManager clipboard = (ClipboardManager) this.application.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", Objects.requireNonNullElse(updateResponse.html_url, "https://github.com/fR0Z863xF/fudisk/releases/latest"));
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(application, "更新链接已复制到剪贴板", Toast.LENGTH_SHORT).show();
                    this.application.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Objects.requireNonNullElse(updateResponse.html_url, "https://github.com/fR0Z863xF/fudisk/releases/latest"))));
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}


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