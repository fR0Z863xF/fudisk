package com.fr0z863xf.fudisk;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.fr0z863xf.fudisk.FileSystem.FileManager;
import com.fr0z863xf.fudisk.Worker.AccountWorker.RefreshWorker;
import com.fr0z863xf.fudisk.Worker.AccountWorker.UploadWorker;

import java.io.File;

public class OpenFileActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_open_file);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Log.i("OpenFileActivity", "文件选择start");
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                String filePath = uri.getPath();

                if (filePath != null) {
                    Log.i("OpenFileActivity", "文件打开成功：" + filePath + " 虚拟文件名：" + new File(filePath).getName());
                    //FileManager.getInstance(null).prepareUpload(uri);
                    OneTimeWorkRequest uploadWork = new OneTimeWorkRequest.Builder(UploadWorker.class)
                            .setInputData(new Data.Builder()
                                    .putString("uri",uri.toString()).build())
                            .build();
                    WorkManager.getInstance(this.getApplicationContext()).enqueue(uploadWork);
                } else {
                    Toast.makeText(this, "无法获取文件路径", Toast.LENGTH_SHORT).show();
                }
            }
        }
        finish();
    }

//    private void requestReadExternalStoragePermission() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                    PERMISSION_REQUEST_CODE);
//        } else {
//            selectFile();
//        }
//    }
}