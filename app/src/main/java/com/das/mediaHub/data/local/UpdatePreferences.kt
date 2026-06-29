package com.das.mediaHub.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.das.mediaHub.data.model.PendingUpdate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/**
 * Manages persisted state for pending application updates.
 *
 * Stores and retrieves the downloaded APK path and its corresponding version
 * code using a dedicated SharedPreferences file. The application context is
 * injected by Hilt to ensure a single, application-scoped preferences instance
 * is used.
 */
class UpdatePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val pendingUpdate: Flow<PendingUpdate> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == UPDATE_APK_PATH_KEY || key == UPDATE_VERSION_CODE_KEY) {
                trySend(
                    PendingUpdate(
                        apkPath = getUpdateApkPath(),
                        versionCode = getUpdateVersionCode()
                    )
                )
            }
        }

        // Emit the current value immediately.
        trySend(
            PendingUpdate(
                apkPath = getUpdateApkPath(),
                versionCode = getUpdateVersionCode()
            )
        )

        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    /**
     * Saves information about a downloaded APK that is awaiting installation.
     *
     * @param apkPath Absolute path to the downloaded APK file.
     * @param versionCode Version code of the downloaded APK.
     */
    fun updatePendingInstall(apkPath: String, versionCode: Int) {
        prefs.edit {
            putString(UPDATE_APK_PATH_KEY, apkPath)
            putInt(UPDATE_VERSION_CODE_KEY, versionCode)
        }
    }

    /**
     * Returns the stored APK path for the pending update, or `null` if no
     * pending update exists.
     */
    fun getUpdateApkPath(): String? {
        return prefs.getString(UPDATE_APK_PATH_KEY, null)
    }

    /**
     * Returns the version code of the pending update, or `-1` if none has been
     * stored.
     */
    fun getUpdateVersionCode(): Int {
        return prefs.getInt(UPDATE_VERSION_CODE_KEY, -1)
    }

    /**
     * Removes all persisted information about the pending update.
     */
    fun clearPendingInstall() {
        prefs.edit {
            remove(UPDATE_APK_PATH_KEY)
            remove(UPDATE_VERSION_CODE_KEY)
        }
    }

    private companion object {
        const val PREFS_NAME = "UpdatePrefs"
        const val UPDATE_APK_PATH_KEY = "update_apk_path"
        const val UPDATE_VERSION_CODE_KEY = "update_version_code"
    }
}
