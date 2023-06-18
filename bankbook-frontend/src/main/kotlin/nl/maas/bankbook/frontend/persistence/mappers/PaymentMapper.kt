package nl.maas.bankbook.frontend.persistence.mappers

import nl.maas.bankbook.frontend.persistence.entities.Payment
import org.mapstruct.Mapper

@Mapper
interface PaymentMapper {
    fun entityToDomain(payment: Payment): nl.maas.bankbook.domain.Payment
    fun domainToEntity(payment: nl.maas.bankbook.domain.Payment): Payment
}