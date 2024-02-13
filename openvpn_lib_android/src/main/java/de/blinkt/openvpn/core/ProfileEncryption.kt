package de.blinkt.openvpn.core

import android.content.Context
import android.os.Build
import androidx.security.crypto.MasterKey
import androidx.security.crypto.EncryptedFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.GeneralSecurityException


internal class ProfileEncryption {
    companion object {
        @JvmStatic
        fun encryptionEnabled(): Boolean {
            return mMasterKey != null
        }

        private var mMasterKey: MasterKey? = null
        @JvmStatic
        fun initMasterCryptAlias(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                return
            try {
                mMasterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
            } catch (e: GeneralSecurityException) {
                VpnStatus.logException("Could not initialise file encryption key.", e)
            } catch (e: IOException) {
                VpnStatus.logException("Could not initialise file encryption key.", e)
            }
        }

        @JvmStatic
        @Throws(GeneralSecurityException::class, IOException::class)
        fun getEncryptedVpInput(context: Context, file: File): FileInputStream {
            val encryptedFile = EncryptedFile.Builder(
                context,
                file,
                mMasterKey!!,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
            return encryptedFile.openFileInput()
        }

        @JvmStatic
        @Throws(GeneralSecurityException::class, IOException::class)
        fun getEncryptedVpOutput(context: Context, file: File): FileOutputStream {
            val encryptedFile = EncryptedFile.Builder(
                context,
                file,
                mMasterKey!!,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
            return encryptedFile.openFileOutput()
        }
    }
}