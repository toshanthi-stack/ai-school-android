package com.lillytech.aischool.automotive

import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.util.Log

/**
 * Validates `MediaBrowser` connections in [AISchoolMediaService.onGetRoot].
 *
 * Safety-compliant communication means only the vehicle's system media UI
 * (and a small set of trusted system surfaces) may browse the catalog.
 * Unknown callers are refused, which also prevents third-party apps from
 * re-rendering syllabus content outside the distraction-optimized templates.
 *
 * Production hardening note: pin caller signatures (as in the AOSP
 * UAMP/universal PackageValidator) in addition to this package/UID check.
 */
class PackageValidator(private val context: Context) {

    private val trustedPackages = setOf(
        context.packageName,                      // ourselves (session controller)
        "com.android.car.media",                  // AAOS Media Center
        "com.android.car.carlauncher",            // AAOS launcher media widget
        "com.android.systemui",                   // system UI / quick controls
        "com.android.bluetooth",                  // AVRCP
        "com.google.android.carassistant",        // Google Assistant (driving)
        "com.google.android.projection.gearhead", // Android Auto projection
    )

    /**
     * @return `true` when [clientPackageName] is a trusted media surface and
     * actually owns [clientUid] (prevents package-name spoofing).
     */
    fun isKnownCaller(clientPackageName: String, clientUid: Int): Boolean {
        if (clientUid == Process.myUid() || clientUid == Process.SYSTEM_UID) {
            return true
        }
        if (clientPackageName !in trustedPackages) {
            Log.w(TAG, "Rejecting unknown media browser caller: $clientPackageName (uid=$clientUid)")
            return false
        }
        val ownerUid = try {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageUid(clientPackageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Caller package not found: $clientPackageName", e)
            return false
        }
        val matches = ownerUid == clientUid
        if (!matches) {
            Log.w(
                TAG,
                "UID mismatch for $clientPackageName: claimed=$clientUid actual=$ownerUid",
            )
        }
        return matches
    }

    private companion object {
        const val TAG = "AISchoolPkgValidator"
    }
}
