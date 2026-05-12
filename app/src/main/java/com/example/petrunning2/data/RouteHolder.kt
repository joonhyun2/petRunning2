package com.example.petrunning2.data

import com.example.petrunning2.ui.running.LatLngPoint

/**
 * 러닝 종료 시 경로 좌표를 임시 보관하는 싱글톤.
 * Navigation으로 큰 리스트를 전달할 수 없어서 이 방식을 사용.
 * ResultScreen이 읽은 후 clear() 호출.
 */
object RouteHolder {
    private var _points: List<LatLngPoint> = emptyList()

    fun set(points: List<LatLngPoint>) {
        _points = points
    }

    fun get(): List<LatLngPoint> = _points

    fun clear() {
        _points = emptyList()
    }
}
