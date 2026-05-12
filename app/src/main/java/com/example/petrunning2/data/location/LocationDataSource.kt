package com.example.petrunning2.data.location

import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationDataSource @Inject constructor(
    private val fusedClient: FusedLocationProviderClient
) {
    // 3초마다 GPS 위치를 Flow로 흘려보냄
    // SecurityException은 위에서 권한 허용 확인 후 호출하므로 여기선 suppress
    @SuppressWarnings("MissingPermission")
    fun locationFlow(): Flow<Location> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            INTERVAL_MS
        )
            .setMinUpdateIntervalMillis(MIN_INTERVAL_MS)
            .build()

        fusedClient.requestLocationUpdates(
            request,
            callback,
            Looper.getMainLooper()
        ).addOnFailureListener { e -> close(e) }

        // Flow 수집이 끝나면(화면 이탈/앱 종료) GPS 구독 해제
        awaitClose { fusedClient.removeLocationUpdates(callback) }
    }

    companion object {
        private const val INTERVAL_MS = 3_000L      // 3초마다 위치 업데이트
        private const val MIN_INTERVAL_MS = 1_500L  // 최소 1.5초 간격
    }
}
