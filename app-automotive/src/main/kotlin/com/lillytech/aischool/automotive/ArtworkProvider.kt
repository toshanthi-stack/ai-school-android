package com.lillytech.aischool.automotive

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import com.lillytech.aischool.demoaudio.DemoAudio
import java.io.File
import java.io.FileOutputStream

/**
 * Serves the branded pillar artwork as `content://` URIs.
 *
 * Why a provider: the AAOS Media Center (a separate, system process) resolves
 * browse-item and Now-Playing art through its own image loader, which can read
 * `content://` and `http(s)://` but NOT a cross-package `android.resource://`
 * URI. A provider is the canonical way for a media app to hand the system UI
 * its artwork. The bytes are static brand assets, so the provider is read-only
 * and exposes nothing sensitive.
 *
 * URI shape: `content://<applicationId>.artwork/category/<art_name>`
 */
class ArtworkProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String = "image/jpeg"

    /**
     * Materializes the requested drawable into a cache file once and returns a
     * read-only descriptor. Decoding to PNG keeps the output a real image file
     * regardless of how the resource was packaged.
     */
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val file = materialize(uri) ?: return null
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    /**
     * Returns an [AssetFileDescriptor] with a **declared length**. This matters:
     * `car-media-common` decodes art via `ImageDecoder.createSource(resolver,
     * uri)`, which rejects a descriptor of `UNKNOWN_LENGTH`. Reporting the real
     * file length is what makes the art actually render on the IVI.
     */
    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        val file = materialize(uri) ?: return null
        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        return AssetFileDescriptor(pfd, 0, file.length())
    }

    override fun openAssetFile(
        uri: Uri,
        mode: String,
        signal: CancellationSignal?,
    ): AssetFileDescriptor? = openAssetFile(uri, mode)

    /** Decodes the requested drawable into a cache file once; returns it. */
    private fun materialize(uri: Uri): File? {
        val ctx = context ?: return null
        val artName = uri.lastPathSegment ?: return null
        @Suppress("DiscouragedApi") // names are data-driven by design
        val resId = ctx.resources.getIdentifier(artName, "drawable", ctx.packageName)
        if (resId == 0) return null

        val cacheFile = File(ctx.cacheDir, "artwork_$artName.jpg")
        if (!cacheFile.exists() || cacheFile.length() == 0L) {
            ctx.resources.openRawResource(resId).use { input ->
                FileOutputStream(cacheFile).use { output -> input.copyTo(output) }
            }
        }
        return cacheFile
    }

    // Read-only image provider — the rest of the CRUD surface is unused.
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    companion object {
        private const val PATH_CATEGORY = "category"

        private fun authority(context: Context): String = "${context.packageName}.artwork"

        /** `content://` artwork URI for a syllabus [category]. */
        fun forCategory(context: Context, category: String): Uri =
            Uri.Builder()
                .scheme("content")
                .authority(authority(context))
                .appendPath(PATH_CATEGORY)
                .appendPath(DemoAudio.artNameFor(category))
                .build()
    }
}
