package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object AuthorTable : IntIdTable("Author") {
    val name = text("name")
    val createdAt = datetime("created_at")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var name by AuthorTable.name
    var createdAt by AuthorTable.createdAt

    fun toResponse(): AuthorRecord {
        return AuthorRecord(id.value, name, createdAt)
    }
}
