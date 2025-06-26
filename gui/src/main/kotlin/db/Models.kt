package db

import org.jetbrains.exposed.dao.id.IntIdTable

object Messages : IntIdTable() {
    val sander = varchar("sender", 255)
    val content = text("content")
}
