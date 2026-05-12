package com.example.petrunning2.ui.running

// RunningScreen이 화면에 표시할 데이터를 담는 봉투
// ViewModel이 이걸 업데이트하면 Screen이 자동으로 다시 그려짐
data class RunningUiState(
    val distanceKm: Double = 0.0,       // 뛴 거리 (km)
    val elapsedSeconds: Long = 0L,      // 경과 시간 (초)
    val paceSecPerKm: Long = 0L,        // 페이스 (초/km) — GPS 붙이면 자동 계산됨
    val status: RunStatus = RunStatus.IDLE
)

enum class RunStatus {
    IDLE,       // 시작 전
    RUNNING,    // 뛰는 중
    PAUSED      // 일시정지
}

// 경과 시간(초)을 "24:15" 형태 문자열로 변환하는 도우미 함수
fun Long.toTimeString(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%d:%02d".format(minutes, seconds)
}

// 페이스(초/km)를 "7'05\"" 형태로 변환
fun Long.toPaceString(): String {
    if (this <= 0L) return "--'--\""
    val minutes = this / 60
    val seconds = this % 60
    return "%d'%02d\"".format(minutes, seconds)
}
