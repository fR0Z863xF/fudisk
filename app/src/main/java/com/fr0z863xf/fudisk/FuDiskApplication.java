package com.fr0z863xf.fudisk;

import android.app.Application;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.fr0z863xf.fudisk.FileSystem.FileManager;
import com.fr0z863xf.fudisk.Utils.AccountManager;
import com.fr0z863xf.fudisk.Utils.FuuleaApi;
import com.fr0z863xf.fudisk.Utils.SettingsManager;
import com.fr0z863xf.fudisk.Utils.UpdateManager;
import com.fr0z863xf.fudisk.Worker.AccountWorker.RefreshWorker;
import com.fr0z863xf.fudisk.Worker.AccountWorker.UpdateWorker;

public class FuDiskApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        //Log.d("FuDiskGlobal",this.getApplicationContext().getFilesDir().toString());
        UpdateManager.getInstance(this);
        OneTimeWorkRequest updateWork = new OneTimeWorkRequest.Builder(UpdateWorker.class).build();
        WorkManager.getInstance(this).enqueue(updateWork);
        FileManager.getInstance(this);
        AccountManager.getInstance(this);
        SettingsManager.getInstance(this);
        OneTimeWorkRequest refreshAccountWork = new OneTimeWorkRequest.Builder(RefreshWorker.class).build();
        WorkManager.getInstance(this).enqueue(refreshAccountWork);

    }
}
