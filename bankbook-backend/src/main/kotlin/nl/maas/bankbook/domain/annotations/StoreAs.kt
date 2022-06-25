package nl.maas.bankbook.domain.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class StoreAs(val storeAs: String)
