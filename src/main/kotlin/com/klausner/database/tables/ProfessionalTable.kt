package com.klausner.database.tables

import com.klausner.domains.Professional
import com.klausner.domains.valueobjects.Address
import com.klausner.domains.valueobjects.Phone
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object ProfessionalTable : Table() {
    val id = uuid(name = "id")
    val storeId = uuid(name = "store_id").nullable()
    val professionalName = varchar(name = "name", length = 100)
    val bio = text(name = "bio").nullable()
    val email = varchar(name = "email", length = 100).nullable()
    val password = varchar(name = "password", length = 100).nullable()
    val houseNumber = varchar(name = "house_number", length = 20).nullable()
    val complement = varchar(name = "complement", length = 100).nullable()
    val street = varchar(name = "street", length = 100).nullable()
    val neighborhood = varchar(name = "neighborhood", length = 100).nullable()
    val city = varchar(name = "city", length = 100).nullable()
    val state = varchar(name = "state", length = 100).nullable()
    val country = varchar(name = "country", length = 100).nullable()
    val zipCode = varchar(name = "zip_code", length = 100).nullable()
    val phone = varchar(name = "phone_number", length = 20).nullable()
    val phoneIsWhatsApp = bool(name = "phone_is_whatsapp").nullable()
    val instagram = varchar(name = "instagram", length = 100).nullable()
    val facebook = varchar(name = "facebook", length = 100).nullable()
    val photo = varchar(name = "photo", length = 100).nullable()
    val workHours = varchar(name = "work_hours", length = 500).nullable()

    fun toDomain(row: ResultRow): Professional {
        return Professional(
            id = row[id],
            storeId = row[storeId],
            name = row[professionalName],
            bio = row[bio],
            email = row[email],
            password = row[password],
            address = Address.PossibleAddress(
                street = row[street],
                number = row[houseNumber],
                complement = row[complement],
                neighborhood = row[neighborhood],
                city = row[city],
                state = row[state],
                country = row[country],
                zipCode = row[zipCode]
            ).toAddressOrNull(),
            phone = row[phone]?.let { Phone.fromString(it, row[phoneIsWhatsApp] ?: false) },
            instagram = row[instagram],
            facebook = row[facebook],
            photo = row[photo],
            workHours = null
        )
    }
}
