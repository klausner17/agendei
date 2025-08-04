package com.klausner.helpers

import com.klausner.domains.Professional
import com.klausner.domains.valueobjects.Address
import com.klausner.domains.valueobjects.Interval
import com.klausner.domains.valueobjects.Phone
import java.util.UUID

data class ProfessionalTestInput(
    val id: UUID = UUID.randomUUID(),
    val storeId: UUID? = null,
    val name: String = "John Doe",
    val bio: String = "Professional bio",
    val address: Address? = null,
    val password: String? = null,
    val phone: Phone = Phone("+55", "11", "999999999", true),
    val instagram: String? = null,
    val facebook: String? = null,
    val photo: String = "s3://johndoe.jpg",
    val doLogin: Boolean = true,
    val workHours: List<Interval>? = null
)

fun buildProfessional(input: ProfessionalTestInput = ProfessionalTestInput()) = Professional(
    id = input.id,
    storeId = input.storeId,
    name = input.name,
    bio = input.bio,
    address = input.address,
    password = input.password,
    phone = input.phone,
    instagram = input.instagram,
    facebook = input.facebook,
    photo = input.photo,
    workHours = input.workHours
)

