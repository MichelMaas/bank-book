package nl.maas.bankbook.frontend.persistence.config

import nl.maas.jpa.framework.config.DataSourceConfigTemplate
import org.springframework.context.annotation.Configuration

@Configuration
class JPAConfig : DataSourceConfigTemplate(
    "jdbc:mysql://localhost:3306/bankbook",
    "bankbook",
    "bbP@ss23",
    Companion.DB_TYPES.MYSQL8,
    true,
    packagesToScan = arrayOf("nl.maas.bankbook.frontend.persistence")
) {
}