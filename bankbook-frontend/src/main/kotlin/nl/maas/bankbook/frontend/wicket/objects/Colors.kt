package nl.maas.bankbook.frontend.wicket.objects

import de.martinspielmann.wicket.chartjs.data.dataset.property.color.SimpleColor
import kotlin.reflect.full.memberProperties

class Colors(val index: Int, color: String) : SimpleColor(color), Comparable<Colors>, java.io.Serializable {
    companion object {
        val GREEN = Colors(0, "GREEN")
        val RED = Colors(1, "RED")
        val YELLOW = Colors(2, "YELLOW")
        val BLUE = Colors(3, "BLUE")
        val PURPLE = Colors(4, "PURPLE")
        val ORANGE = Colors(5, "ORANGE")
        val BROWN = Colors(6, "BROWN")
        val CYAN = Colors(7, "CYAN")
        val MAGENTA = Colors(8, "MAGENTA")
        val PINK = Colors(9, "PINK")
        val BURLYWOOD = Colors(10, "BURLYWOOD")
        val DARKSLATEGREY = Colors(11, "DARKSLATEGREY")

        fun colors() = this::class.memberProperties.map { it.getter.call(this) as Colors }.sorted()
        fun colors(vararg colors: String) = colors.mapIndexed { i, c -> Colors(i, c) }.sorted()

        fun colorsLot(lotNumber: Int): Map<Int, List<Colors>> {
            val map = mutableMapOf<Int, List<Colors>>()
            val lotSize = colors().size.div(lotNumber)
            for (lot in 0..(lotNumber - 1)) {
                val fromIndex = lot * lotSize
                val toIndex = fromIndex + lotSize
                map.put(lot, colors().subList(fromIndex, toIndex).sorted())
            }
            return map
        }

        fun colorsForCatagories(numberOfCategories: Int, categorySize: Int): ArrayList<List<Colors>> {
            val list = ArrayList<List<Colors>>()
            for (index in 0..numberOfCategories - 1) {
                val fromIndex = index * categorySize
                val toIndex = fromIndex + categorySize
                list.add(colors().subList(fromIndex, toIndex).sorted())
            }
            return list
        }
    }

    override fun compareTo(other: Colors): Int {
        return this.index.compareTo(other.index)
    }
}