package com.fr0z863xf.fudisk.Utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking


val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager private constructor(private val context: Context) {

    // 用于保存应用设置的成员变量
    var autoRefresh: Boolean = true
        private set
    var fileDisplayMode: Int = 1
        private set
    var cloudStorageMode: String = "Direct"
        private set
    var stegLevel: Int = 1
        private set

    private val dataStore = context.dataStore

    companion object {
        @Volatile
        private var INSTANCE: SettingsManager? = null

        // Java 兼容的初始化方法
        @JvmStatic
        fun getInstance(context: Context ?): SettingsManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SettingsManager(context?: throw IllegalArgumentException("Instance not initialized,but no context provided"))
                instance.loadInitialValues()
                INSTANCE = instance
                instance
            }
        }

        val KEY_AUTO_REFRESH = booleanPreferencesKey("auto_refresh")
        val KEY_FILE_DISPLAY_MODE = intPreferencesKey("file_display_mode")
        val KEY_CLOUD_STORAGE_MODE = stringPreferencesKey("cloud_storage_mode")
        val KEY_STEG_LEVEL = intPreferencesKey("steg_level")
    }

    // 加载初始值
    private fun loadInitialValues() {
        runBlocking {
            val preferences = dataStore.data.first()
            autoRefresh = preferences[KEY_AUTO_REFRESH] ?: true
            fileDisplayMode = preferences[KEY_FILE_DISPLAY_MODE] ?: 1
            cloudStorageMode = preferences[KEY_CLOUD_STORAGE_MODE] ?: "Direct"
            stegLevel = preferences[KEY_STEG_LEVEL] ?: 1
            Log.i("SettingsManager", "Settings read: autoRefresh=$autoRefresh, fileDisplayMode=$fileDisplayMode, cloudStorageMode=$cloudStorageMode, stegLevel=$stegLevel")
        }
    }

    // 获取自动刷新设置的Flow
    val autoRefreshFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[KEY_AUTO_REFRESH] ?: true
        }

    // 更新自动刷新设置
    suspend fun setAutoRefresh(value: Boolean) {
        Log.i("SettingsManager", "setAutoRefresh: $value")
        autoRefresh = value
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_REFRESH] = value
        }
    }

    // 获取文件显示模式设置的Flow
    val fileDisplayModeFlow: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[KEY_FILE_DISPLAY_MODE] ?: 1
        }

    // 更新文件显示模式设置
    suspend fun setFileDisplayMode(value: Int) {
        fileDisplayMode = value
        dataStore.edit { preferences ->
            preferences[KEY_FILE_DISPLAY_MODE] = value
        }
    }

    // 获取云存储模式设置的Flow
    val cloudStorageModeFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[KEY_CLOUD_STORAGE_MODE] ?: "Direct"
        }

    // 更新云存储模式设置
    suspend fun setCloudStorageMode(value: String) {
        Log.i("SettingsManager", "setCloudStorageMode: $value")
        cloudStorageMode = value
        dataStore.edit { preferences ->
            preferences[KEY_CLOUD_STORAGE_MODE] = value
        }
    }

    // 获取Steg等级设置的Flow
    val stegLevelFlow: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[KEY_STEG_LEVEL] ?: 1
        }

    // 更新Steg等级设置
    suspend fun setStegLevel(value: Int) {
        Log.i("SettingsManager", "setStegLevel: $value")
        stegLevel = value
        dataStore.edit { preferences ->
            preferences[KEY_STEG_LEVEL] = value
        }
    }


}