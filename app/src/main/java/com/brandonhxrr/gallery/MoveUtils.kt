package com.brandonhxrr.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

const val PERMISSION_PREFS_NAME = "permissions"
const val SD_CARD_PERMISSION_GRANTED_KEY = "sd_card_permission_granted"

fun copyFileToUri(context: Context, fileToCopy: File, destinationPath: String, moveFile: Boolean, requestPermissionLauncher: ActivityResultLauncher<Intent>, intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>) {
    try{
        val filePath = "$destinationPath/${fileToCopy.name}"
        val newFile = File(filePath)

        val inputStream = FileInputStream(fileToCopy)
        val outputStream = FileOutputStream(newFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()

        if(moveFile) {
            deletePhotoFromExternal(context, getContentUri(context, fileToCopy)!!, intentSenderLauncher)
        }

    }catch (e: Exception){
        if(!checkExternalPermission(context)){
            requestPermission(requestPermissionLauncher)
        }else{
            copyToExternal(context, fileToCopy, destinationPath, moveFile, intentSenderLauncher)
        }
    }
}

fun copyToExternal(context: Context, fileToCopy: File, destinationPath: String, moveFile: Boolean, intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>){
    val mimeType: String = if(fileToCopy.extension in imageExtensions) "image/${fileToCopy.extension}" else "video/${fileToCopy.extension}"

    val pathSegments = destinationPath.split("/")
    val relativePath = pathSegments.subList(3, pathSegments.size).joinToString("/")

    val externalStorageUri = Uri.parse(getSDCardUri(context).toString().replace("/document/", "/tree/"))

    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    context.contentResolver.takePersistableUriPermission(externalStorageUri!!, takeFlags)

    var destinationFolder = DocumentFile.fromTreeUri(context, externalStorageUri)
    val sourceFile = DocumentFile.fromFile(fileToCopy)

    if (relativePath.isNotEmpty()) {
        val parts = relativePath.split("/")
        for (part in parts) {
            if (destinationFolder != null) {
                val existingFolder = destinationFolder.findFile(part)
                destinationFolder = if (existingFolder != null && existingFolder.isDirectory) {
                    existingFolder
                } else {
                    destinationFolder.createDirectory(part)
                }
            }
        }
    }

    val destinationFile = destinationFolder?.createFile(mimeType, fileToCopy.name)
    context.contentResolver.openOutputStream(destinationFile?.uri!!).use { outputStream ->
        context.contentResolver.openInputStream(sourceFile.uri)?.use { inputStream ->
            inputStream.copyTo(outputStream!!)
        }
    }

    if(moveFile) {
        deletePhotoFromExternal(context, getContentUri(context, fileToCopy)!!, intentSenderLauncher)
        Toast.makeText(context, "File moved successfully", Toast.LENGTH_SHORT).show()
    }
}

 fun requestPermission(requestPermissionLauncher: ActivityResultLauncher<Intent>) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
        addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    }
    requestPermissionLauncher.launch(intent)
}

fun getSDCardUri(context: Context): Uri? {
    val externalStorage = Environment.getExternalStorageDirectory().path
    val externalStorageSplitted = externalStorage.split("/")
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        return Uri.parse("file://" + "/storage/" + externalStorageSplitted[2])
    } else {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val storageVolumes = storageManager.storageVolumes
        for (storageVolume in storageVolumes) {
            if (storageVolume.isRemovable && !storageVolume.isEmulated) {
                try {
                    val storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
                    val getPathMethod = storageVolumeClazz.getMethod("getPath")
                    val getPathResult = getPathMethod.invoke(storageVolume) as String
                    val storageVolumeState = Environment.getExternalStorageState(File(getPathResult))
                    if (storageVolumeState == Environment.MEDIA_MOUNTED) {
                        val getUuidMethod = storageVolumeClazz.getMethod("getUuid")
                        val getUuidResult = getUuidMethod.invoke(storageVolume) as String
                        return DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "$getUuidResult:")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    return null
}

private fun checkExternalPermission(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences(PERMISSION_PREFS_NAME, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(SD_CARD_PERMISSION_GRANTED_KEY, false)
}