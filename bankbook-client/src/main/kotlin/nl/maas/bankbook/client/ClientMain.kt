package nl.maas.bankbook.client

object ClientMain {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val client = BankbookClient()
        if (args.size == 1 && args[0] == "--configure") {
            client.configure()
            return
        }
        require(args.isEmpty()) { "Gebruik: bankbook [--configure]" }
        client.start()
    }
}
