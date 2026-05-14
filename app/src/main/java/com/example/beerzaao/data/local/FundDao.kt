package com.example.beerzaao.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FundDao {
    @Query("SELECT * FROM fund_entity ORDER BY sortOrder ASC")
    fun getAllFunds(): Flow<List<FundEntity>>

    @Query("SELECT * FROM fund_entity WHERE fundCode = :code LIMIT 1")
    suspend fun getFundByCode(code: String): FundEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFund(fund: FundEntity)

    @Delete
    suspend fun deleteFund(fund: FundEntity)

    @Query("DELETE FROM fund_entity WHERE fundCode = :code")
    suspend fun deleteFundByCode(code: String)

    @Query("SELECT MAX(sortOrder) FROM fund_entity")
    suspend fun getMaxSortOrder(): Int?

    @Query("UPDATE fund_entity SET sortOrder = :sortOrder WHERE fundCode = :code")
    suspend fun updateSortOrder(code: String, sortOrder: Int)

    @Query("SELECT COUNT(*) FROM fund_entity WHERE fundCode = :code")
    suspend fun isFundExists(code: String): Int
}
