package com.campuscoders.posterminalapp.data.repository.locale

import com.campuscoders.posterminalapp.data.locale.TerminalUsersDao
import com.campuscoders.posterminalapp.domain.model.TerminalUsers
import com.campuscoders.posterminalapp.domain.repository.locale.CashierAndReportRepository
import javax.inject.Inject

class CashierAndReportRepositoryImpl @Inject constructor(
    private val terminalUsersDao: TerminalUsersDao
) : CashierAndReportRepository {

    override suspend fun deleteCashier(terminalId: Int): Int {
        return terminalUsersDao.deleteTerminalUser(terminalId)
    }

    override suspend fun fetchAllCashiers(): List<TerminalUsers>? {
        return terminalUsersDao.queryAllTerminalUsers()
    }

    override suspend fun fetchLastTerminalUserId(): Int? {
        return terminalUsersDao.queryLastInsertedTerminalUsers()
    }

    override suspend fun fetchTerminalUserById(terminalId: String): TerminalUsers? {
        return terminalUsersDao.queryTerminalUserById(terminalId)
    }
}