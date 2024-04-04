package nl.maas.bankbook.frontend.wicket.objects

import nl.maas.bankbook.domain.Storable
import nl.maas.bankbook.frontend.wicket.objects.enums.StartOfMonth

data class Properties(var startOfMonth: StartOfMonth = StartOfMonth.CALENDAR) : Storable<Properties>
