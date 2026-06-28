package com.klausner.database.tables

import com.klausner.domains.Customer
import com.klausner.domains.valueobjects.Phone
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object CustomerTable : Table("customers") {
    val id = uuid("id")
    val name = varchar("name", 255)
    val countryCode = varchar("country_code", 5)
    val areaCode = varchar("area_code", 5)
    val phoneNumber = varchar("phone_number", 20)
    val isWhatsApp = bool("is_whatsapp")
    val email = varchar("email", 255).nullable()
    val password = varchar("password", 255).nullable()

    override val primaryKey = PrimaryKey(id)

    fun toDomain(row: ResultRow) =
        Customer(
            id = row[id],
            name = row[name],
            phone =
                Phone(
                    countryCode = row[countryCode],
                    areaCode = row[areaCode],
                    number = row[phoneNumber],
                    isWhatsApp = row[isWhatsApp],
                ),
            email = row[email],
            password = row[password],
        )
}
