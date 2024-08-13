package com.fr0z863xf.fudisk.Worker.AccountWorker;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fr0z863xf.fudisk.FileSystem.FileManager;
import com.fr0z863xf.fudisk.Utils.AccountManager;

public class UploadWorker extends Worker {
    private FileManager fileManager;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        fileManager = FileManager.getInstance(null);
    }

    @NonNull
    @Override
    public Result doWork() {
        Uri uri = Uri.parse(getInputData().getString("uri"));
        FileManager.getInstance(null).prepareUpload(uri);
        return Result.success(new Data.Builder()
                .putInt("finished", -1)
                .build());
    }
}
