package com.fr0z863xf.fudisk.Utils;


import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava2.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava2.RxDataStore;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.fr0z863xf.fudisk.Worker.AccountWorker.RefreshWorker;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import kotlinx.coroutines.sync.Mutex;

public class AccountManager {

    private static AccountManager instance;
    private Application application;
    public String account;
    private String password;
    private String cookies;
    private String jwt;
    private String sn;
    public Integer gtype;
    public String uuid;
    //下列凭证有效期30分钟，不写入datastore，仅上传时刷新
    public String sid;
    public String skey;
    public String stoken;
    public String cos_region;
    public String cos_bucket;


    private RxDataStore<Preferences> dataStore;

    private static final Preferences.Key<String> ACCOUNT_KEY = PreferencesKeys.stringKey("account");
    private static final Preferences.Key<String> PASSWORD_KEY = PreferencesKeys.stringKey("password");
    private static final Preferences.Key<String> SERIAL_NUMBER_KEY = PreferencesKeys.stringKey("serial_number");
    private static final Preferences.Key<String> COOKIES_KEY = PreferencesKeys.stringKey("cookies");
    private static final Preferences.Key<String> JWT_KEY = PreferencesKeys.stringKey("jwt");
    private static final Preferences.Key<Integer> GTYPE_KEY = PreferencesKeys.intKey("gtype");
    private static final Preferences.Key<String> UUID_KEY = PreferencesKeys.stringKey("uuid"); //fake uuid

    //public MutableLiveData<Integer> refreshStatus=new MutableLiveData<>(0);  // 0:空闲 1:刷新中
    public MutableLiveData<Integer> accountStatus=new MutableLiveData<>(0);    // 0:未配置 1:未登录 2:登录成功 3:登录失败 4.刷新中
    public MutableLiveData<Boolean> accountNeedRefresh=new MutableLiveData<>(false);    // 标志位，后台刷新时修改配置


    private AccountManager(Application application) {
        //单例模式，禁用直接实例化
        Log.i("AccountManager", "初始化帐号管理");
        this.application = application;
        this.dataStore = new RxPreferenceDataStoreBuilder(application.getApplicationContext(), /*name=*/ "account").build();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {

                Preferences preferences = dataStore.data().blockingFirst();
                this.account = preferences.get(ACCOUNT_KEY);
                if (this.account != null) {
                    Log.i("AccountManager", "检测到已帐号: " + this.account.substring(0,4)+"****");
                    this.accountStatus.postValue(1);
                }
                this.password = preferences.get(PASSWORD_KEY);
                this.sn = preferences.get(SERIAL_NUMBER_KEY);
                this.gtype = preferences.get(GTYPE_KEY);

                this.cookies = preferences.get(COOKIES_KEY);
                this.jwt = preferences.get(JWT_KEY);
                if (this.jwt != null && this.cookies != null) {
                    Log.i("AccountManager", "检测到已登录jwt和csrf："+this.jwt.substring(0,4)+"****");
                    this.accountStatus.postValue(2);
                }
                this.uuid = preferences.get(UUID_KEY);
                if (this.uuid == null) {
                    this.uuid = "";
                    String[] i = UUID.randomUUID().toString().split("-");
                    for (int j = 0; this.uuid.length()<17; j++) {
                        this.uuid += i[j];
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static AccountManager getInstance(@Nullable Application application) {
        if (instance == null && application != null) {
            synchronized (AccountManager.class) {
                if (instance == null) {
                    instance = new AccountManager(application);
                    Log.i("AccountManager", "Instance created");
                }
            }
        } else if (instance == null) {
            throw new RuntimeException("AccountManager: Instance have not been initialized but Application is null");
        }
        return instance;
    }
/*
    public LiveData<Integer> getRefreshStatus() {
        return refreshStatus;
    }
    public void setRefreshStatus(Integer i) {
        refreshStatus.setValue(i);
    }*/
    public LiveData<Integer> getAccountStatus() {
        return accountStatus;
    }
    public void setAccountStatus(Integer i) {
        accountStatus.setValue(i);
    }


    /*public boolean checkAccountExists() {
        // 检查帐号是否存在的逻辑
        Log.i("AccountManager", "检查帐号配置...");

        return false; // 或 false
    }*/

    public boolean setAccount(String account, String password, String sn, Integer gtype) {
        Log.i("AccountManager", "用户触发帐号配置保存");
        this.account = account;
        this.password = password;
        this.sn = sn;
        this.gtype = gtype;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Single<Preferences> updateResult = this.dataStore.updateDataAsync(prefsIn -> {
                    MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
                    mutablePreferences.set(ACCOUNT_KEY, this.account);
                    mutablePreferences.set(PASSWORD_KEY, this.password);
                    mutablePreferences.set(SERIAL_NUMBER_KEY, this.sn);
                    mutablePreferences.set(GTYPE_KEY, this.gtype);
                    return Single.just(mutablePreferences);
                });
                updateResult.blockingGet();
                Log.i("AccountManager", "帐号配置已写入DataStore");

            } catch (Exception e) {
                Log.e("AccountManager", "保存帐号配置失败");
                e.printStackTrace();
                //return false;
            }
        });
        Log.i("AccountManager", "帐号配置保存完毕，触发刷新Worker");
        /*OneTimeWorkRequest refreshAccountWork = new OneTimeWorkRequest.Builder(RefreshWorker.class).build();
        WorkManager.getInstance(null).enqueue(refreshAccountWork);*/
        this.accountStatus.setValue(1);
        FuuleaApi.login(this.account, this.password, this.sn, this.gtype, this.uuid);

        return true;
    }

    public String refreshAccount() {
        Log.i("AccountManager", "刷新帐号配置...");
        if (this.accountStatus.getValue() == 0) {
            Log.w("AccountManager", "刷新帐号配置:帐号不存在");
            return "账号不存在";
        }

        if (this.accountStatus.getValue() != 4) {

        }
        // 异步检测函数逻辑

        try {
            //TimeUnit.SECONDS.sleep(15);
            FuuleaApi.refresh(this.cookies, this.jwt, this.account, this.sn, this.gtype, this.uuid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i("AccountManager", "刷新帐号配置:完成");

        return "ok"; // 或其他错误信息
    }

    public void setCredential(String cookies,String jwt) {
        this.cookies = cookies;
        this.jwt = jwt;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Single<Preferences> updateResult = this.dataStore.updateDataAsync(prefsIn -> {
                    MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
                    mutablePreferences.set(COOKIES_KEY, cookies);
                    mutablePreferences.set(JWT_KEY, jwt);
                    return Single.just(mutablePreferences);
                });
                updateResult.blockingGet();
                Log.i("AccountManager", "帐号token已写入DataStore");

            } catch (Exception e) {
                Log.e("AccountManager", "保存帐号token失败");
                e.printStackTrace();
                //return false;
            }
        });

    }


    public void refreshCOS(String tid) {
        FuuleaApi.refreshCredential(this.cookies, this.jwt,this.sn,this.gtype,this.uuid,tid);
    }


    //public String ge


}
