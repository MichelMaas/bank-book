package nl.maas.bankbook.frontend.persistence.mappers

import nl.maas.bankbook.frontend.persistence.entities.Transfer
import org.mapstruct.Mapper

@Mapper
interface TransferMapper {
    fun entityToDomain(payment: Transfer): nl.maas.bankbook.domain.Transfer
    fun domainToEntity(payment: nl.maas.bankbook.domain.Transfer): Transfer
}