package app.simple.positional.viewmodels.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.positional.R
import app.simple.positional.licensing.*
import app.simple.positional.util.isNetworkAvailable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("HardwareIds")
class LicenseViewModel(application: Application) : AndroidViewModel(application), LicenseCheckerCallback {

    val allow = MutableLiveData<String>()
    val doNotAllow = MutableLiveData<String>()
    val applicationError = MutableLiveData<String>()

    private var base64PublicKey = getApplication<Application>().getString(R.string.licensing_key)
    private val deviceId: String = Settings.Secure.getString(getApplication<Application>().contentResolver, Settings.Secure.ANDROID_ID)

    private val salt = byteArrayOf(-46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64, 89)

    private var licenseChecker = LicenseChecker(
            getApplication(),
            ServerManagedPolicy(
                    getApplication(),
                    AESObfuscator(
                            salt,
                            getApplication<Application>().packageName,
                            deviceId)),
            base64PublicKey)

    init {
        beginCheck(1000L)
    }

    fun retry() {
        beginCheck(500L)
    }

    private fun beginCheck(delay: Long) {
        viewModelScope.launch {
            if(isNetworkAvailable(getApplication())) {
                delay(delay)
                licenseChecker.checkAccess(this@LicenseViewModel)
            } else {
                doNotAllow.postValue(getApplication<Application>().getString(R.string.internet_connection_alert))
            }
        }
    }

    override fun allow(reason: Int) {
        when (reason) {
            Policy.NOT_LICENSED -> {
                doNotAllow.postValue(getApplication<Application>().getString(R.string.license_failed))
            }
            Policy.RETRY -> {
                doNotAllow.postValue(getApplication<Application>().getString(R.string.failed))
            }
            Policy.LICENSED -> {
                allow.postValue(getApplication<Application>().getString(R.string.license_successful))
            }
            else -> {
                doNotAllow.postValue(getApplication<Application>().getString(R.string.common_google_play_services_unknown_issue))
            }
        }
    }

    override fun doNotAllow(reason: Int) {
        when (reason) {
            Policy.NOT_LICENSED -> {
                doNotAllow.postValue(getApplication<Application>().getString(R.string.license_failed))
            }
            Policy.RETRY -> {
                doNotAllow.postValue(getApplication<Application>().getString(R.string.failed))
            }
            Policy.LICENSED -> {
                allow.postValue(getApplication<Application>().getString(R.string.license_successful))
            }
            else -> {
                doNotAllow.postValue(getApplication<Application>().getString(R.string.common_google_play_services_unknown_issue))
            }
        }
    }

    override fun applicationError(errorCode: Int) {
        when (errorCode) {
            LicenseCheckerCallback.ERROR_INVALID_PACKAGE_NAME -> {
                doNotAllow.postValue("ERROR_INVALID_PACKAGE_NAME: ${getApplication<Application>().packageName}")
            }
            LicenseCheckerCallback.ERROR_INVALID_PUBLIC_KEY -> {
                applicationError.postValue("ERROR_INVALID_PUBLIC_KEY")
            }
            LicenseCheckerCallback.ERROR_MISSING_PERMISSION -> {
                applicationError.postValue("ERROR_MISSING_PERMISSION")
            }
            LicenseCheckerCallback.ERROR_NON_MATCHING_UID -> {
                applicationError.postValue("ERROR_NON_MATCHING_UID")
            }
            LicenseCheckerCallback.ERROR_NOT_MARKET_MANAGED -> {
                doNotAllow.postValue("ERROR_NOT_MARKET_MANAGED")
            }
            else -> {
                applicationError.postValue("UNKNOWN_ERROR")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        licenseChecker.onDestroy()
    }
}
