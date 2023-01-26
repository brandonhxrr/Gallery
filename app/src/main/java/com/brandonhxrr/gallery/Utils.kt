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

fun getImagesFromAlbum(folder: String): List<File> {
    val fileExtensions = listOf("jpg", "jpeg", "png", "gif", "mp4", "mkv")
    return File(folder).listFiles { file ->
        file.isFile && fileExtensions.contains(file.extension)
    }?.toList() ?: ArrayList()
}

fun getAllImages(context: Context): List<File> {
    val sortOrder = MediaStore.Images.Media.DATE_TAKEN + " ASC"
    return queryUri(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, sortOrder)
        .use { it?.getResultsFromCursor() ?: listOf() }
}



private fun queryUri(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?, sortOrder: String = ""): Cursor? {
    return context.contentResolver.query(
        uri,
        projection,
        selection,
        selectionArgs,
        sortOrder)
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