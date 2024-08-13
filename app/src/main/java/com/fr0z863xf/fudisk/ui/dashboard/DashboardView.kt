package com.fr0z863xf.fudisk.ui.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable

import androidx.compose.foundation.layout.*
//import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.Fragment
import com.fr0z863xf.fudisk.FileSystem.CloudFile

import java.io.File

import com.fr0z863xf.fudisk.R
import com.fr0z863xf.fudisk.FileSystem.FileManager
import com.fr0z863xf.fudisk.MainActivity
import com.fr0z863xf.fudisk.OpenFileActivity
import com.fr0z863xf.fudisk.Utils.AccountManager


@Composable
fun DashboardView(fragment : Fragment) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<File?>(null) }


    val fileManager = FileManager.getInstance(null) // Using the Java Singleton
    val files = fileManager.getFiles()

    Column(modifier = Modifier.scrollable(state = rememberScrollState(), orientation = Orientation.Vertical, )) {
        // File Cards
        LazyColumn {
            items(files) { file ->
                FileCard(file,fragment)
            }
        }

        // File Details Dialog
        selectedFile?.let { file ->
            if (showDialog) {
                FileDetailsDialog(file, onDismiss = { showDialog = false},fragment)
            }

        }


    }


    Box(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp), contentAlignment = Alignment.BottomEnd) {
        ExtendedFloatingActionButton(
            onClick = {
                val accountManager = AccountManager.getInstance(null)
                if (accountManager.accountStatus.value != 2) {
                    Toast.makeText(fragment.context,"帐号配置不正确", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(fragment.context,"正在唤起文文件选择器", Toast.LENGTH_SHORT).show()
                    fragment.startActivity(Intent(fragment.activity, OpenFileActivity::class.java))
                }
            },
            text = { Text("上传文件") },
            icon = { Icon(Icons.Filled.Add, "上传文件" )}
        )
    }


}

@Composable
fun FileCard(file: File,fragment : Fragment) {

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        FileDetailsDialog(file,onDismiss = { showDialog = false },fragment)
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = { showDialog = true })

    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = R.drawable.baseline_file_open_24), contentDescription = null)
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.padding(16.dp)) {

                Text(text = file.name.split(".fudisk")[0], fontSize = 16.sp)
                //Text(text = file.path, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun FileDetailsDialog(file: File, onDismiss: () -> Unit,fragment : Fragment) {
    val cloudFile = CloudFile.parseFile(file.path)
    AlertDialog(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        onDismissRequest = { onDismiss() },
        title = { Text(text = "文件信息") },
        text = {
            Column {
                Text(text = "文件名: ${file.name.split(".fudisk")[0]}")
                Text(text = "上传日期: ${cloudFile.date}")
                Text(text = "下载链接: ${cloudFile.link}")
                Button(onClick = {
                    val clipboard = fragment.context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("label", cloudFile.link)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(fragment.context, "已复制", Toast.LENGTH_SHORT).show()
                }) {
                    Text("复制下载链接")
                }

            }

        },
        confirmButton = {
            TextButton(onClick = {
                Toast.makeText(fragment.context, "暂不支持", Toast.LENGTH_SHORT).show()
                onDismiss()

            }) {
                Text(text = "删除", color = Color(fragment.resources.getColor(R.color.md_theme_error)))
            }
            TextButton(onClick = { onDismiss() }) {
                Text("关闭")
            }
        }
    )
}

/*

@Composable
fun FileDetailsDialog(filePath: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "File Details") },
        text = { Text(text = "Details of file: $filePath") },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}



@Composable
fun onFileClick(file: File,onDismiss: () -> Unit) {
    FileDetailsDialog(file)
}

 */