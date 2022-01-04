package nl.maas.bankbook.frontend.wicket.objects

data class Language(
    val code: String,
    val name: String,
    val pages: List<Page>
)