package com.fr0z863xf.fudisk.ui.settings



import android.content.Context
import android.content.Intent
import android.net.Uri
import com.fr0z863xf.fudisk.R
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.webkit.WebSettings
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import com.fr0z863xf.fudisk.Utils.AccountManager
import com.fr0z863xf.fudisk.Utils.SettingsManager
import com.google.android.material.internal.ContextUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun SettingsView(fragment : SettingsFragment, settingsManager: SettingsManager) {

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {

        val cloudStorageMode by settingsManager.cloudStorageModeFlow.collectAsState(initial = settingsManager.cloudStorageMode)
        val stegLevel by settingsManager.stegLevelFlow.collectAsState(initial = settingsManager.stegLevel)
        val autoRefresh by settingsManager.autoRefreshFlow.collectAsState(initial = settingsManager.autoRefresh)
        val fileDisplayMode by settingsManager.fileDisplayModeFlow.collectAsState(initial = settingsManager.fileDisplayMode)
        Text("帐号", style = MaterialTheme.typography.headlineMedium)

        // 配置帐号
        AccountConfigurationItem()

        // 自动刷新帐号
        AutoRefreshItem(autoRefresh = autoRefresh)

        Spacer(modifier = Modifier.height(24.dp))
        Text("常规", style = MaterialTheme.typography.headlineMedium)

        // 文件显示模式
        FileDisplayModeItem(fileDisplayMode = fileDisplayMode,
            onFileDisplayModeChange = { mode ->
                scope.launch {
                    settingsManager.setFileDisplayMode(mode)
                }
            })

        // 下载目录
        DownloadDirectoryItem(fragment)

        // 导出/备份云端文件索引
        ExportBackupItem(fragment)

        Spacer(modifier = Modifier.height(24.dp))
        Text("高级", style = MaterialTheme.typography.headlineMedium)

        // 云端储存模式
        CloudStorageModeItem(cloudStorageMode = cloudStorageMode,
            onCloudStorageModeChange = { mode ->
                scope.launch {
                    settingsManager.setCloudStorageMode(mode)
                }
            })

        // Steg等级

        StegLevelItem(cloudStorageMode = cloudStorageMode,
            stegLevel = stegLevel,
            onStegLevelChange = { level ->
                scope.launch {
                    settingsManager.setStegLevel(level)
                }
            })
        appVersion(fragment)

    }
}

@Composable
fun AccountConfigurationItem() {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        AccountConfigDialog(onDismiss = { showDialog = false })
    }

    var accountManager = AccountManager.getInstance(null)

    Log.i("SettingsView", "accountStatus: ${accountManager.accountStatus.value}")

    ListItem(
        headlineContent = { Text("配置帐号") },
        supportingContent = { Text( if (accountManager.accountStatus.value == 0) "未配置" else "已配置" ) },
        modifier = Modifier.clickable { showDialog = true }
    )
}

@Composable
fun AccountConfigDialog(onDismiss: () -> Unit) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf(0) }
    val options = listOf("领创", "航志（不完全支持）", "华为（完全不支持，但你可以试试）")


    val accountManager = AccountManager.getInstance(null)
    if (accountManager.accountStatus.value != 0) {
        account = accountManager.account
        selectedOption = accountManager.gtype ?: 0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {

                accountManager.setAccount(account, password, serialNumber, selectedOption)
                onDismiss()
            }) { Text("保存") }
        },

        title = { Text(text = "配置帐号") },
        text = {
            Column {
                TextField(value = account, onValueChange = { account = it }, label = { Text("帐号") })
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    visualTransformation = PasswordVisualTransformation()
                )
                TextField(value = serialNumber, onValueChange = { serialNumber = it }, label = { Text("序列号") })
                Text("管控环境：", style = MaterialTheme.typography.labelLarge)
                options.forEachIndexed { index, text ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedOption == index, onClick = { selectedOption = index })
                        Text(text = text)
                    }
                }
            }
        }
    )
}

@Composable
fun AutoRefreshItem(autoRefresh: Boolean) {
    //var isAutoRefreshEnabled by remember { mutableStateOf(true) }
    var scope = rememberCoroutineScope()
    ListItem(
        headlineContent = { Text("自动刷新帐号") },
        supportingContent = { Text("在应用启动时自动刷新帐号信息") },
        trailingContent = {
            Switch(
                checked = autoRefresh,
                onCheckedChange = {
                    //isAutoRefreshEnabled = it
                    val settingsManager = SettingsManager.getInstance(null)
                    scope.launch(Dispatchers.IO) {
                    settingsManager.setAutoRefresh(it)}


                }
            )
        }
    )
}

@Composable
fun FileDisplayModeItem(fileDisplayMode: Int,
                        onFileDisplayModeChange: (Int) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        FileDisplayModeDialog(onDismiss = { showDialog = false },fileDisplayMode,onFileDisplayModeChange)
    }

    ListItem(
        headlineContent = { Text("文件显示模式") },
        modifier = Modifier.clickable { showDialog = true }
    )
    //Text(if (fileDisplayMode == 1) "卡片" else "列表")
}

@Composable
fun FileDisplayModeDialog(onDismiss: () -> Unit, fileDisplayMode: Int, onFileDisplayModeChange : (Int) -> Unit) {
    var selectedMode by remember { mutableStateOf(fileDisplayMode) }
    val modes = listOf("列表（暂未实现，选了也没用）", "卡片")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onFileDisplayModeChange(selectedMode)
                onDismiss()
            }) { Text("确定") }
        },
        title = { Text("文件显示模式") },
        text = {
            Column {
                modes.forEachIndexed { index, text ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedMode == index, onClick = { selectedMode = index })
                        Text(text = text)
                    }
                }
            }
        }
    )
}

@Composable
fun DownloadDirectoryItem(fragment: SettingsFragment) {
    ListItem(
        headlineContent = { Text("下载目录") },
        supportingContent = { Text("系统默认下载目录，即" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/FuDisk") },
        modifier = Modifier.clickable {
            //val intent =Intent(Intent.ACTION_VIEW,Uri.parse("content://downloads/public_downloads/"))
            //fragment.startActivity(intent)
            Toast.makeText(fragment.context, "暂不支持修改", Toast.LENGTH_SHORT).show()
        }
    )
}

@Composable
fun ExportBackupItem(fragment: SettingsFragment) {
    ListItem(
        headlineContent = { Text("导出/备份云端文件索引") },
        supportingContent = { Text("导出云端文件信息") },
        modifier = Modifier.clickable {
            /* Not allowing modifications for now */
            Toast.makeText(fragment.context, "暂不支持", Toast.LENGTH_SHORT).show()

        }
    )
}

@Composable
fun CloudStorageModeItem(cloudStorageMode: String,
                         onCloudStorageModeChange: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        CloudStorageModeDialog(onDismiss = { showDialog = false },cloudStorageMode,onCloudStorageModeChange)
    }

    ListItem(
        headlineContent = { Text("云端储存模式") },
        modifier = Modifier.clickable { showDialog = true }
    )
    //Text(cloudStorageMode)
}

@Composable
fun CloudStorageModeDialog(onDismiss: () -> Unit, cloudStorageMode: String, onCloudStorageModeChange: (String) -> Unit) {
    var selectedMode by remember { mutableStateOf(if(cloudStorageMode == "Direct") 0 else 1) }
    val modes = listOf("Direct")//, "Steg")
    //val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onCloudStorageModeChange(modes[selectedMode])
                onDismiss()
            }) { Text("确定") }
        },
        title = { Text("云端储存模式") },
        text = {
            Column {
                modes.forEachIndexed { index, text ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedMode == index, onClick = { selectedMode = index })
                        Text(text = text)
                    }
                }
            }
        }
    )
}

@Composable
fun StegLevelItem(cloudStorageMode: String,
                  stegLevel: Int,
                  onStegLevelChange: (Int) -> Unit) {
    val settingsManager = SettingsManager.getInstance(null)
    val cloudStorageMode by settingsManager.cloudStorageModeFlow.collectAsState(initial = "Direct")
    if (cloudStorageMode != "Steg") return

    var selectedLevel by remember { mutableStateOf(1) }
    var expanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Box {
        ListItem(
            headlineContent = { Text("Steg等级") },
            supportingContent = { Text("选择数字, 1-5") },
            trailingContent = {
                Text(
                    text = selectedLevel.toString(),
                    modifier = Modifier.clickable { expanded = true }
                )
            },
            modifier = Modifier.clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            (1..5).forEach { level ->
                DropdownMenuItem(text = { Text(text = level.toString()) },onClick = {
                    selectedLevel = level
                    expanded = false
                    onStegLevelChange(level)
                })
            }
        }
    }
}

@Composable
fun appVersion(fragment: Fragment) {
        val appVersion = fragment.context?.packageManager?.getPackageInfo(fragment.context?.packageName
            ?: "", 0)?.versionName?:""
    ListItem(
        headlineContent = { Text("当前版本") },
        supportingContent = { Text(appVersion) },
        modifier = Modifier.clickable {
            Toast.makeText(fragment.context, "当前版本：$appVersion，重启app以检查更新", Toast.LENGTH_SHORT).show()
        }
    )
}