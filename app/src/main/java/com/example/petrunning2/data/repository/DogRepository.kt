package com.example.petrunning2.data.repository

import com.example.petrunning2.data.Dog
import com.example.petrunning2.data.local.dao.DogDao
import com.example.petrunning2.data.local.dao.RunRecordDao
import com.example.petrunning2.data.local.entity.DogEntity
import com.example.petrunning2.data.local.entity.RunRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DogRepository @Inject constructor(
    private val dogDao: DogDao,
    private val runRecordDao: RunRecordDao
) {
    val dog: Flow<Dog> = dogDao.getDog().map { it?.toDog() ?: DogEntity().toDog() }

    fun getTodayRecords(): Flow<List<RunRecordEntity>> {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return runRecordDao.getTodayRecords(startOfDay)
    }

    suspend fun addXp(gainedXp: Int) {
        val current = dogDao.getDogOnce() ?: DogEntity()
        var xp = current.currentXp + gainedXp
        var level = current.level
        var maxXp = current.maxXp

        // 레벨업 (연속으로 여러 번 가능)
        while (xp >= maxXp) {
            xp -= maxXp
            level++
            maxXp = level * 100
        }

        dogDao.saveDog(current.copy(currentXp = xp, level = level, maxXp = maxXp))
    }

    suspend fun addCredit(amount: Int) {
        val current = dogDao.getDogOnce() ?: DogEntity()
        dogDao.saveDog(current.copy(credit = current.credit + amount))
    }

    suspend fun updateName(name: String) {
        val current = dogDao.getDogOnce() ?: DogEntity()
        dogDao.saveDog(current.copy(name = name.trim()))
    }

    suspend fun spendCredit(price: Int): Boolean {
        val current = dogDao.getDogOnce() ?: DogEntity()
        if (current.credit < price) return false
        dogDao.saveDog(current.copy(credit = current.credit - price))
        return true
    }

    suspend fun saveRunRecord(
        distanceKm: Double,
        elapsedSeconds: Long,
        paceSecPerKm: Long,
        xpGained: Int,
        routePoints: String = ""
    ) {
        runRecordDao.insertRecord(
            RunRecordEntity(
                distanceKm = distanceKm,
                elapsedSeconds = elapsedSeconds,
                paceSecPerKm = paceSecPerKm,
                xpGained = xpGained,
                routePoints = routePoints
            )
        )
    }

    fun getAllRecords() = runRecordDao.getAllRecords()
}

private fun DogEntity.toDog() = Dog(
    name = name,
    level = level,
    currentXp = currentXp,
    maxXp = maxXp,
    credit = credit
)
