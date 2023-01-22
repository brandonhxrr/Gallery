package com.brandonhxrr.gallery

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns
import java.io.File

fun sortImagesByFolder(files: List<File>): Map<File, List<File>> {
    val resultMap = mutableMapOf<File, MutableList<File>>()
    for (file in files) {
        if(file.totalSpace != 0L){
            (!resultMap.containsKey(file.parentFile)).let { resultMap.put(file.parentFile, mutableListOf()) }
            resultMap[file.parentFile]?.add(file)
        }
    }
    return resultMap
}

fun getImagesFromFolder(context: Context, folder: String): List<File> {

    val selection = MediaStore.Images.Media.DATA + " LIKE ?"

    return queryUri(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, arrayOf("%$folder/%"))
        .use { it?.getResultsFromCursor() ?: listOf() }
}

fun getAllImages(context: Context): List<File> {
    return queryUri(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null)
        .use { it?.getResultsFromCursor() ?: listOf() }
}


private fun queryUri(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): Cursor? {
    return context.contentResolver.query(
        uri,
        projection,
        selection,
        selectionArgs,
        null)
}

private fun Cursor.getResultsFromCursor(): List<File> {
    val results = mutableListOf<File>()

    while (this.moveToNext()) {
        results.add(File(this.getString(this.getColumnIndexOrThrow(MediaColumns.DATA))))
    }


    return results
}

fun getImageVideoNumber(parent : File) : Int{
    var imageCount = 0
    var videoCount = 0

    val imageExtensions = arrayOf("jpg", "jpeg", "png", "gif", "bmp")
    val videoExtensions = arrayOf("mp4", "mkv", "avi", "wmv", "mov")

    for (file in parent.listFiles()!!) {
        if (file.isFile) {
            val fileExtension = file.extension.lowercase()
            if (imageExtensions.contains(fileExtension)) {
                imageCount++
            } else if (videoExtensions.contains(fileExtension)) {
                videoCount++
            }
        }
    }
    return imageCount + videoCount
}

val projection = arrayOf(
    MediaStore.Files.FileColumns._ID,
    MediaStore.Files.FileColumns.DATA,
    MediaStore.Files.FileColumns.DATE_ADDED,
    MediaStore.Files.FileColumns.MIME_TYPE,
    MediaStore.Files.FileColumns.TITLE
)