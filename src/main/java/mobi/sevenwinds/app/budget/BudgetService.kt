package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = body.author?.let { EntityID(it, AuthorTable) }
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {

            val author = param.author?.let {
                AuthorTable
                    .select { AuthorTable.name.lowerCase() like "%${it.toLowerCase()}%" }
                    .firstOrNull()
                    ?.let(AuthorEntity::wrapRow)
            }

            val totalQuery = BudgetTable
                .select {
                    if (author != null) (BudgetTable.year eq param.year) and (BudgetTable.author eq author.id)
                    else BudgetTable.year eq param.year
                }

            val limitedQuery = BudgetTable
                .select {
                    if (author != null) (BudgetTable.year eq param.year) and (BudgetTable.author eq author.id)
                    else BudgetTable.year eq param.year
                }
                .orderBy(Pair(BudgetTable.month, SortOrder.ASC), Pair(BudgetTable.amount, SortOrder.DESC))
                .limit(param.limit, param.offset)

            val total = totalQuery.count()
            val data = BudgetEntity.wrapRows(limitedQuery).map { it.toResponse() }

            val sumByType = totalQuery
                .groupBy { it[BudgetTable.type].toString() }
                .mapValues { (_, v) -> v.sumBy { it[BudgetTable.amount] } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data,
                author = author?.name,
                authorCreateDateTime = author?.createdAt
            )
        }
    }
}