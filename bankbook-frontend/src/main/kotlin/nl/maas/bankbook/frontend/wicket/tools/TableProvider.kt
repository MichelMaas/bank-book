package nl.maas.bankbook.frontend.wicket.tools

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider
import org.apache.wicket.model.IModel

class TableProvider<T, S : Comparable<S>> : SortableDataProvider<T, S>() {

    class TableProviderComparator<S : Comparable<S>> : Comparator<S> {
        override fun compare(p0: S, p1: S): Int {
            return p0.compareTo(p1)
        }
    }

    override fun iterator(p0: Long, p1: Long): MutableIterator<T> {
        TODO("Not yet implemented")
    }

    override fun size(): Long {
        TODO("Not yet implemented")
    }

    override fun model(p0: T): IModel<T> {
        TODO("Not yet implemented")
    }
}