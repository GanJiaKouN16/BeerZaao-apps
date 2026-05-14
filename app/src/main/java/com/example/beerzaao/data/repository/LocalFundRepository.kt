package com.example.beerzaao.data.repository

import com.example.beerzaao.data.local.FundDao
import com.example.beerzaao.data.local.FundEntity
import kotlinx.coroutines.flow.Flow

class LocalFundRepository(private val fundDao: FundDao) {

    fun getAllFunds(): Flow<List<FundEntity>> = fundDao.getAllFunds()

    suspend fun getFundByCode(code: String): FundEntity? = fundDao.getFundByCode(code)

    suspend fun addFund(code: String, name: String): Result<Unit> {
        return try {
            val exists = fundDao.isFundExists(code) > 0
            if (exists) {
                Result.failure(Exception("基金已在自选列表中"))
            } else {
                val maxOrder = fundDao.getMaxSortOrder() ?: 0
                fundDao.insertFund(
                    FundEntity(
                        fundCode = code,
                        fundName = name,
                        sortOrder = maxOrder + 1
                    )
                )
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFund(code: String): Result<Unit> {
        return try {
            fundDao.deleteFundByCode(code)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSortOrder(funds: List<FundEntity>): Result<Unit> {
        return try {
            funds.forEachIndexed { index, fund ->
                fundDao.updateSortOrder(fund.fundCode, index)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isFundExists(code: String): Boolean {
        return fundDao.isFundExists(code) > 0
    }
}
