package nl.maas.bankbook.frontend.services

import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import nl.maas.bankbook.domain.Payment
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.domain.Transfer
import nl.maas.bankbook.frontend.persistence.mappers.PaymentMapper
import nl.maas.bankbook.frontend.persistence.mappers.TransferMapper
import nl.maas.bankbook.frontend.persistence.repositories.PaymentRepository
import nl.maas.bankbook.frontend.persistence.repositories.TransferRepository
import org.mapstruct.factory.Mappers
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.inject.Inject

@Component
class TransactionsService {

    @Inject
    private lateinit var paymentRepository: PaymentRepository

    @Inject
    private lateinit var transferRepository: TransferRepository

    private val paymentMapper = Mappers.getMapper(PaymentMapper::class.java)
    private val transferMapper = Mappers.getMapper(TransferMapper::class.java)

    fun getTransactionsForMonth(localDate: LocalDate): List<Transaction> {
        val startDate = localDate.withDayOfMonth(1)
        val endDate = localDate.withDayOfMonth(localDate.month.length(localDate.isLeapYear))
        val payments = paymentRepository.findFromToDates(
            startDate,
            endDate
        )
        val transfers = transferRepository.findFromToDates(startDate, endDate)
        return runBlocking {
            async {
                payments.map { paymentMapper.entityToDomain(it) }
                    .plus(transfers.map { transferMapper.entityToDomain(it) })
            }.await()
        }
    }

    fun getTransactionsForYear(localDate: LocalDate): List<Transaction> {
        val startDate = localDate.withDayOfYear(1)
        val endDate = localDate.withDayOfYear(localDate.lengthOfYear())
        val payments = paymentRepository.findFromToDates(
            startDate,
            endDate
        )
        val transfers = transferRepository.findFromToDates(startDate, endDate)
        return runBlocking {
            async {
                payments.map { paymentMapper.entityToDomain(it) }
                    .plus(transfers.map { transferMapper.entityToDomain(it) })
            }.await()
        }
    }

    fun getAll(): List<Transaction> {
        return runBlocking {
            async {
                val paymentEntities = paymentRepository.getAll()
                val transferEntities = transferRepository.getAll()
                val payments = paymentEntities.map { paymentMapper.entityToDomain(it) }
                val transfers = transferEntities.map { transferMapper.entityToDomain(it) }
                return@async payments.plus(transfers)
            }.await()
        }
    }

    fun getTransactionsForFilter(filter: String): List<Transaction> {
        return runBlocking {
            async {
                val paymentEntities = paymentRepository.byFilter(filter)
                val transferEntities = transferRepository.byFilter(filter)
                val payments = paymentEntities.map { paymentMapper.entityToDomain(it) }
                val transfers = transferEntities.map { transferMapper.entityToDomain(it) }
                return@async payments.plus(transfers)
            }.await()
        }
    }

    fun store(vararg transactions: Transaction) {
        val payments =
            transactions.filter { Payment::class.isInstance(it) }.map { paymentMapper.domainToEntity(it as Payment) }
        val transfers =
            transactions.filter { Transfer::class.isInstance(it) }.map { transferMapper.domainToEntity(it as Transfer) }
        paymentRepository.store(*payments.toTypedArray())
        transferRepository.store(*transfers.toTypedArray())
    }
}