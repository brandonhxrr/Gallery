package com.brandonhxrr.gallery

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.File
import java.util.*
import kotlin.collections.HashMap

val imageExtensions = arrayOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
val videoExtensions = arrayOf("mp4", "mkv", "avi", "wmv", "mov")
val fileExtensions = imageExtensions.plus(videoExtensions)
var selectable = false

var albumes: HashMap<File, List<File>>? = null
var itemsList = mutableListOf<Photo>()

fun sortImagesByFolder(files: List<File>): Map<File, List<File>> {
    val resultMap = mutableMapOf<File, MutableList<File>>()
    for (file in files) {
        if(file.totalSpace != 0L){
            (!resultMap.containsKey(file.parentFile!!)).let { resultMap.put(file.parentFile!!, mutableListOf()) }
            resultMap[file.parentFile!!]?.add(file)
        }
    }
    return resultMap
}

fun getImagesFromAlbum(folder: String): List<Photo> {
    return File(folder)
        .listFiles { file -> file.isFile && fileExtensions.contains(file.extension) }
        ?.sortedWith(compareByDescending { it.lastModified() })
        ?.map { file -> Photo(path = file.absolutePath, position = 0, selected = false) }
        ?: emptyList()
}

fun getAllImages(context: Context): List<File> {
    val sortOrder = MediaStore.Images.Media.DATE_TAKEN + " ASC"
    val sortOrderVideos = MediaStore.Video.Media.DATE_TAKEN + " ASC"

    val imageList = queryUri(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, sortOrder)
        .use { it?.getResultsFromCursor() ?: listOf() }
    val videoList = queryUri(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, sortOrderVideos)
        .use { it?.getResultsFromCursor() ?: listOf() }
    return videoList + imageList
}

fun getAllImagesAndVideosSortedByRecent(context: Context): List<Photo> {
    val sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC"
    val sortOrderVideos = MediaStore.Video.Media.DATE_TAKEN + " DESC"

    val imageList = queryUri(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, sortOrder)
        .use { it?.getResultsFromCursor() ?: listOf() }
    val videoList = queryUri(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, sortOrderVideos)
        .use { it?.getResultsFromCursor() ?: listOf() }

    val resultList = (imageList + videoList).sortedWith(compareByDescending { it.lastModified() })
    return resultList.map { file -> Photo(path = file.absolutePath, position = 0, selected = false) }
}

fun getImagesFromPage(page: Int, data: List<Photo>): List<Photo> {
    val startIndex = (page - 1) * 100
    val endIndex = startIndex + 100

    if (startIndex >= data.size) {
        return emptyList()
    }

    val end = if (endIndex > data.size) data.size else endIndex

    return data.subList(startIndex, end)
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

@SuppressLint("Range")
fun getContentUri(context: Context, file: File): Uri? {
    val filePath = file.absolutePath
    var mimeType = ""
    val contentUri: Uri
    val contentValues = ContentValues()

    if(file.extension in imageExtensions) {
        mimeType = "image/*"
        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
    }else{
        mimeType = "video/*"
        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
        contentValues.put(MediaStore.Video.Media.MIME_TYPE, mimeType)
    }

    val cursor: Cursor = context.contentResolver.query(
        contentUri, arrayOf(MediaColumns._ID),
        "${MediaColumns.DATA} =? ", arrayOf(filePath), null
    )!!

    return if (cursor.moveToFirst()) {
        val id: Int = cursor.getInt(cursor.getColumnIndex(MediaColumns._ID))
        cursor.close()
        Uri.withAppendedPath(contentUri, "" + id)
    } else {
        if (file.exists()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver: ContentResolver = context.contentResolver
                val contentCollection =
                    if (mimeType.startsWith("image/")) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    else MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/" + UUID.randomUUID().toString())
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 1)
                val finalUri = resolver.insert(contentCollection, contentValues)
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(contentCollection, contentValues, null, null)
                finalUri
            } else {
                contentValues.put(MediaColumns.DATA, filePath)
                context.contentResolver.insert(contentUri, contentValues)
            }
        } else {
            null
        }
    }
}

val projection = arrayOf(
    MediaStore.Files.FileColumns._ID,
    MediaStore.Files.FileColumns.DATA,
    MediaStore.Files.FileColumns.DATE_ADDED,
    MediaStore.Files.FileColumns.MIME_TYPE,
    MediaStore.Files.FileColumns.TITLE
)