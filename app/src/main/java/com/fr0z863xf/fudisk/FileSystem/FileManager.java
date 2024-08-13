package com.fr0z863xf.fudisk.FileSystem;


import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.fr0z863xf.fudisk.Utils.AccountManager;
import com.fr0z863xf.fudisk.Utils.FuuleaApi;
import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.object.PutObjectRequest;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.qcloud.core.auth.SessionQCloudCredentials;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FileManager {

    private static FileManager instance;
    private CosXmlService cosXmlService;
    TransferConfig transferConfig;
    private Application application;
    public File dataDir;

    public MutableLiveData<Integer> uploadStatus=new MutableLiveData<>(0);  // 0:无 1:上传中 2:上传成功 3:上传失败
    public MutableLiveData<List<Integer>> uploadTasks = new MutableLiveData<>(new ArrayList<>());



    private FileManager(Application application) {
        this.application = application;
        this.dataDir = application.getApplicationContext().getFilesDir();
        File indexDir = new File(dataDir,"cloud_index");
        if(!indexDir.exists()) {
            if(!indexDir.mkdirs()) {
                Log.e("FileManager","私有目录下创建索引文件夹失败");
            } else {
                Log.i("FileManager","私有目录下创建索引文件夹成功");
                this.dataDir = indexDir;
            }
        } else {
            Log.i("FileManager","私有目录下cloud_index已存在");
            if(!indexDir.isDirectory()) {
                Log.w("FileManager","私有目录下cloud_index不是文件夹，删除");
                if(!indexDir.delete()) {
                    Log.e("FileManager","删除失败");
                } else {
                    if(!indexDir.mkdirs()) {
                        Log.e("FileManager","私有目录下创建索引文件夹失败");
                    } else {
                        Log.i("FileManager","私有目录下创建索引文件夹成功");
                        this.dataDir = indexDir;
                    }
                }
            } else {
                this.dataDir = indexDir;
            }
        }
        File testFile = new File(dataDir,"基准测试.fudisk");
        if(!testFile.exists()) {
            try {
                testFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.i("FileManager",this.dataDir.toString());
        this.cosXmlService = new CosXmlService(application.getApplicationContext(),new CosXmlServiceConfig.Builder()
                .setRegion("ap-shanghai")
                .isHttps(true)
                .builder());
        this.transferConfig = new TransferConfig.Builder().setForceSimpleUpload(true).build();
    }

    public File[] getFiles() {
        Log.i("FileManager","获取文件列表");
        return dataDir.listFiles((dir, name) -> name.endsWith(".fudisk"));
    }

    public static boolean saveFile() {
        return false;
    }

    public static FileManager getInstance(@Nullable Application application) {
        if (instance == null && application != null) {
            synchronized (AccountManager.class) {
                if (instance == null) {
                    instance = new FileManager(application);
                    Log.i("FileManager", "Instance created");
                }
            }
        } else if (instance == null) {
            throw new RuntimeException("FileManager: Instance have not been initialized but Application is null");
        }
        return instance;
    }

    public boolean upload(File file) {

        return false;
    }

    public void prepareUpload(Uri uri) {
        Integer tid = FuuleaApi.ramdomTid();
        List<Integer> taskList = Objects.requireNonNull(this.uploadTasks.getValue());
        taskList.add(1);
        this.uploadTasks.postValue(taskList);
        Calendar calendar = Calendar.getInstance();
        long ts = System.currentTimeMillis() / 1000;
        CloudFile file = CloudFile.newFile((byte) 0x00,false,false,"s","ap-shanghai",calendar.get(Calendar.YEAR),(short) (calendar.get(Calendar.MONTH) + 1),(short) calendar.get(Calendar.DAY_OF_MONTH),tid, Collections.singletonList(ts));
        String fileName = new File(uri.getPath()).getName();;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = this.application.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            }
        }
        try {
            file.serializeToFile(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            taskList.set(taskList.size() - 1, 2);
            this.uploadTasks.postValue(taskList);
            taskList.remove(taskList.size() - 1);
            this.uploadTasks.postValue(taskList);
            return;
        }
        this.upload(uri,String.valueOf(tid));
    }

    public boolean upload(Uri uri,String tid) {
        Log.i("FileManager", "开始上传: " + uri.toString());
        String bucket = "media-1251808174";
        String cosPath = "media/tk/exercise/"+new SimpleDateFormat("yyyy/MM-dd", Locale.CHINA).format(new Date())+"/t" + tid + "/";
//        try {
//            byte[] file;
//            if (Build.VERSION.SDK_INT >= 33) file = this.application.getContentResolver().openInputStream(uri).readAllBytes();
//            else {
//                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//                int nRead;
//                byte[] data = new byte[1024];
//                InputStream inputStream = this.application.getContentResolver().openInputStream(uri);
//                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
//                    buffer.write(data, 0, nRead);
//                }
//                buffer.flush();
//                file = buffer.toByteArray();
//            }
//        } catch (Exception e) {
//            return false;
//        }
        InputStream fileStream;
        try {
            fileStream = this.application.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }



        AccountManager aM = AccountManager.getInstance(null);
        aM.refreshCOS(tid);
        SessionQCloudCredentials sessionQCloudCredentials = new SessionQCloudCredentials(aM.sid, aM.skey,
                aM.stoken, 0L, 999999999999L);
        PutObjectRequest pR = new PutObjectRequest(bucket, cosPath + new File(uri.getPath()).getName(),fileStream);
        pR.setCredential(sessionQCloudCredentials);
        COSXMLUploadTask cosxmlUploadTask = (new TransferManager(this.cosXmlService,
                transferConfig))
                .upload(pR,null);
        cosxmlUploadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                COSXMLUploadTask.COSXMLUploadTaskResult uploadResult =
                        (COSXMLUploadTask.COSXMLUploadTaskResult) result;
                //FileManager.getInstance(null).uploadStatus.postValue(2);
                List<Integer> taskList = FileManager.getInstance(null).uploadTasks.getValue();
                taskList.set(taskList.size() - 1, 2);
                FileManager.getInstance(null).uploadTasks.postValue(taskList);
                taskList.remove(taskList.size() - 1);
                FileManager.getInstance(null).uploadTasks.postValue(taskList);
            }


            @Override
            public void onFail(CosXmlRequest request,
                               @Nullable CosXmlClientException clientException,
                               @Nullable CosXmlServiceException serviceException) {
                if (clientException != null) {
                    clientException.printStackTrace();
                } else {
                    serviceException.printStackTrace();
                }
                List<Integer> taskList = FileManager.getInstance(null).uploadTasks.getValue();
                taskList.set(taskList.size() - 1, 3);
                FileManager.getInstance(null).uploadTasks.postValue(taskList);
                taskList.remove(taskList.size() - 1);
                FileManager.getInstance(null).uploadTasks.postValue(taskList);
            }
        });
        return false;
    }
}
