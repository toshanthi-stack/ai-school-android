package com.lillytech.aischool.automotive

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Task D — Vehicle Hardware Abstraction Layer (VHAL) integration.
 *
 * Real-time tracker over `VehiclePropertyIds.WINDOW_POS` across every window
 * zone in the cabin (`VEHICLE_AREA_TYPE_WINDOW`). Window position semantics:
 * `0` = fully closed; any value away from `0` = cracked or open. The moment
 * any window leaves the closed position, [onCabinWindowOpened] fires so the
 * media session can systemically pause the active lesson.
 *
 * Graceful degradation: reading WINDOW_POS requires the signature|privileged
 * permission `android.car.permission.CONTROL_CAR_WINDOWS`. On builds where it
 * is not granted (e.g. Play-delivered installs), [start] logs once and the
 * feature disables itself cleanly — playback and browsing are unaffected.
 *
 * Lifecycle: [start] connects to the car service asynchronously
 * (`Car.createCar` with `CAR_WAIT_TIMEOUT_DO_NOT_WAIT`); [stop] unregisters
 * the VHAL callback and disconnects, releasing all infotainment compute —
 * call it from the owning service's `onDestroy()` to avoid leaks.
 */
class CabinWindowMonitor(
    private val context: Context,
    private val onCabinWindowOpened: (areaId: Int, position: Int) -> Unit,
    private val onAllWindowsClosed: () -> Unit,
) {

    private var car: Car? = null
    private var propertyManager: CarPropertyManager? = null

    /** AreaIds of cabin windows currently away from fully-closed. */
    private val openWindows = mutableSetOf<Int>()

    @Volatile
    private var callbackRegistered = false

    /** True when the modern subscribe API registered the callback. */
    @Volatile
    private var usedSubscribeApi = false

    private val propertyCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            if (value.propertyId != VehiclePropertyIds.WINDOW_POS) return
            val position = value.value as? Int ?: return
            val areaId = value.areaId
            if (position != WINDOW_FULLY_CLOSED) {
                synchronized(openWindows) { openWindows.add(areaId) }
                Log.i(
                    TAG,
                    "Cabin change: window areaId=0x${Integer.toHexString(areaId)} " +
                        "moved to position=$position — requesting systemic pause",
                )
                onCabinWindowOpened(areaId, position)
            } else {
                val allClosed = synchronized(openWindows) {
                    openWindows.remove(areaId)
                    openWindows.isEmpty()
                }
                Log.i(
                    TAG,
                    "Cabin change: window areaId=0x${Integer.toHexString(areaId)} " +
                        "fully closed${if (allClosed) " — all windows closed, eligible to resume" else ""}",
                )
                if (allClosed) onAllWindowsClosed()
            }
        }

        override fun onErrorEvent(propertyId: Int, areaId: Int) {
            Log.w(
                TAG,
                "VHAL error for property=0x${Integer.toHexString(propertyId)} " +
                    "areaId=0x${Integer.toHexString(areaId)}",
            )
        }
    }

    /** Connects to the car service and registers the WINDOW_POS callback. */
    fun start() {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
            Log.i(TAG, "Not an automotive device; cabin monitoring disabled.")
            return
        }
        if (ContextCompat.checkSelfPermission(context, PERMISSION_CONTROL_CAR_WINDOWS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(
                TAG,
                "CONTROL_CAR_WINDOWS not granted (non-privileged build); " +
                    "window-aware pause disabled, continuing without it.",
            )
            return
        }
        try {
            car = Car.createCar(
                context,
                /* handler = */ null,
                Car.CAR_WAIT_TIMEOUT_DO_NOT_WAIT,
            ) { connectedCar, ready ->
                if (ready) {
                    onCarServiceReady(connectedCar)
                } else {
                    // Car service crashed or disconnected; drop stale handles.
                    Log.w(TAG, "Car service connection lost; clearing VHAL handles.")
                    callbackRegistered = false
                    propertyManager = null
                }
            }
        } catch (t: Throwable) {
            // NoClassDefFoundError (android.car missing) or SecurityException.
            Log.w(TAG, "Unable to connect to car service; cabin monitoring disabled.", t)
        }
    }

    private fun onCarServiceReady(connectedCar: Car) {
        val manager = connectedCar.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
        if (manager == null) {
            Log.w(TAG, "CarPropertyManager unavailable; cabin monitoring disabled.")
            return
        }
        propertyManager = manager
        try {
            callbackRegistered = subscribeToWindowPosition(manager)
            Log.i(
                TAG,
                if (callbackRegistered) {
                    "WINDOW_POS callback registered for all cabin window zones " +
                        "(modern subscribe API: $usedSubscribeApi)."
                } else {
                    "WINDOW_POS registration rejected by VHAL (property unsupported?)."
                },
            )
        } catch (e: SecurityException) {
            Log.w(TAG, "WINDOW_POS registration denied; cabin monitoring disabled.", e)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "WINDOW_POS not available on this vehicle.", e)
        }
    }

    /**
     * Prefers the current `subscribePropertyEvents` API (on-change semantics
     * by default); falls back to the legacy `registerCallback` on vehicles
     * whose Car API level predates it (minSdk 29 head units).
     */
    private fun subscribeToWindowPosition(manager: CarPropertyManager): Boolean {
        return try {
            usedSubscribeApi = true
            manager.subscribePropertyEvents(VehiclePropertyIds.WINDOW_POS, propertyCallback)
        } catch (_: NoSuchMethodError) {
            usedSubscribeApi = false
            @Suppress("DEPRECATION")
            manager.registerCallback(
                propertyCallback,
                VehiclePropertyIds.WINDOW_POS,
                CarPropertyManager.SENSOR_RATE_ONCHANGE,
            )
        }
    }

    /**
     * Unregisters the VHAL callback and disconnects from the car service.
     * Safe to call repeatedly and from `onDestroy()`.
     */
    fun stop() {
        try {
            if (callbackRegistered) {
                val manager = propertyManager
                if (manager != null) {
                    if (usedSubscribeApi) {
                        manager.unsubscribePropertyEvents(
                            VehiclePropertyIds.WINDOW_POS,
                            propertyCallback,
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        manager.unregisterCallback(propertyCallback, VehiclePropertyIds.WINDOW_POS)
                    }
                }
                Log.i(TAG, "WINDOW_POS callback unregistered.")
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Error while unregistering VHAL callback.", t)
        } finally {
            callbackRegistered = false
            usedSubscribeApi = false
            propertyManager = null
        }
        try {
            car?.disconnect()
        } catch (t: Throwable) {
            Log.w(TAG, "Error while disconnecting from car service.", t)
        } finally {
            car = null
        }
    }

    private companion object {
        const val TAG = "CabinWindowMonitor"
        const val WINDOW_FULLY_CLOSED = 0

        /**
         * Mirror of `Car.PERMISSION_CONTROL_CAR_WINDOWS`; declared locally so
         * the permission check never depends on car-lib constant visibility.
         */
        const val PERMISSION_CONTROL_CAR_WINDOWS = "android.car.permission.CONTROL_CAR_WINDOWS"
    }
}
