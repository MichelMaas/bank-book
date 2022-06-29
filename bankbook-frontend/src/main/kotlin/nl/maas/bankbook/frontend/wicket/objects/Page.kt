package nl.maas.bankbook.frontend.wicket.objects

data class Page(
    val labels: Map<String, String>,
    val name: String
) : java.io.Serializable