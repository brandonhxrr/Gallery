package com.brandonhxrr.gallery

import android.database.Cursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsProvider

class FileProvider: DocumentsProvider() {
    override fun onCreate(): Boolean {
        TODO("Not yet implemented")
    }

    override fun queryRoots(projection: Array<out String>?): Cursor {
        TODO("Not yet implemented")
    }

    override fun queryDocument(documentId: String?, projection: Array<out String>?): Cursor {
        TODO("Not yet implemented")
    }

    override fun queryChildDocuments(
        parentDocumentId: String?,
        projection: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        TODO("Not yet implemented")
    }

    override fun openDocument(
        documentId: String?,
        mode: String?,
        signal: CancellationSignal?
    ): ParcelFileDescriptor {
        TODO("Not yet implemented")
    }
}