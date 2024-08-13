package com.fr0z863xf.fudisk.Worker.AccountWorker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fr0z863xf.fudisk.Utils.AccountManager;
import com.fr0z863xf.fudisk.Utils.UpdateManager;

public class UpdateWorker extends Worker {
    UpdateManager uM;
    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        uM = UpdateManager.getInstance(null);
    }

    @NonNull
    @Override
    public Result doWork() {
        uM.checkForUpdates();
        return Result.success();
    }
}
