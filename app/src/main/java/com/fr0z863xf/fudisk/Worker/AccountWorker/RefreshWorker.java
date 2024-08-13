package com.fr0z863xf.fudisk.Worker.AccountWorker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fr0z863xf.fudisk.Utils.AccountManager;

public class RefreshWorker extends Worker {
    private AccountManager accountManager;

    public RefreshWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        accountManager = AccountManager.getInstance(null);
    }

    @NonNull
    @Override
    public Result doWork() {

        if (accountManager.accountStatus.getValue() == 4) {
            try {
                Log.i("RefreshWorker", "已有刷新任务正在进行，等待刷新任务完成");
                accountManager.wait();
                return Result.success(new Data.Builder()
                        .putInt("account_status", accountManager.accountStatus.getValue())
                        .build());
                // 等待正在进行的刷新任务完成
            } catch (InterruptedException e) {
                e.printStackTrace();
                return Result.failure();
            }
        }


        if (accountManager.accountStatus.getValue() == 0) {
            Log.i("RefreshWorker", "无帐号，跳过刷新");
            return Result.success(new Data.Builder()
                    .putInt("account_status", accountManager.accountStatus.getValue())
                    .build());
        }

        accountManager.accountStatus.postValue(4);
        Log.i("RefreshWorker", "开始刷新");

        String result = accountManager.refreshAccount();
        //accountManager.accountStatus.postValue(2);
        Log.i("RefreshWorker", "刷新结束");
        if ("ok".equals(result)) {
            accountManager.accountStatus.postValue(2);
            Log.i("RefreshWorker", "刷新成功");
        }
        return Result.success(new Data.Builder()
                .putInt("account_status", accountManager.accountStatus.getValue())
                .build());
    }
}
