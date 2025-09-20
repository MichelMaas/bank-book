package nl.maas.bankbook.client

import nl.maas.bankbook.domain.Storable

data class ClientProperties(var serverURL: String = "") : Storable<ClientProperties>
