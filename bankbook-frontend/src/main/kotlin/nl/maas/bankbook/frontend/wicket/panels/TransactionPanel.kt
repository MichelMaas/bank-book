package nl.maas.bankbook.frontend.wicket.panels

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons
import kotlinx.coroutines.runBlocking
import nl.maas.bankbook.domain.Amount
import nl.maas.bankbook.domain.ManualTransaction
import nl.maas.bankbook.domain.Payment
import nl.maas.bankbook.domain.Transaction
import nl.maas.bankbook.domain.enums.MutationTypes
import nl.maas.bankbook.domain.properties.Categories
import nl.maas.bankbook.frontend.wicket.caches.ModelCache
import nl.maas.bankbook.frontend.wicket.objects.AmountTransformer
import nl.maas.bankbook.frontend.wicket.tools.TupleUtils
import nl.maas.bankbook.utils.UUIDUtil
import nl.maas.wicket.framework.components.base.*
import nl.maas.wicket.framework.components.base.DynamicPanel.Companion.ROW_CONTENT_ID
import nl.maas.wicket.framework.components.elemental.SimpleAjaxButton
import nl.maas.wicket.framework.panels.RIAPanel
import nl.maas.wicket.framework.services.Translator
import org.apache.commons.lang3.StringUtils
import org.apache.wicket.Component
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.model.CompoundPropertyModel
import org.apache.wicket.spring.injection.annot.SpringBean
import java.math.BigDecimal
import java.time.LocalDate

class TransactionPanel : RIAPanel() {

    @SpringBean
    private lateinit var modelCache: ModelCache

    @SpringBean
    private lateinit var tupleUtils: TupleUtils

    @SpringBean
    private lateinit var translator: Translator

    private lateinit var transaction: Transaction

    private var newChild: ManualTransaction = ManualTransaction.EMPTY

    override fun onBeforeRender() {
        super.onBeforeRender()
        transaction = modelCache.selectedTransaction!!
        createContent()
    }

    private fun createContent() {
        val dynamicPanel = DynamicPanel("panel").addRows(
            "summary" to intArrayOf(6, 6),
            "form" to intArrayOf(6, 6),
            "buttons" to intArrayOf(12)
        )
        dynamicPanel.addOrReplaceComponentToColumn("summary", 0, createSummaryLeft())
        dynamicPanel.addOrReplaceComponentToColumn("summary", 1, createSummaryRight())
        dynamicPanel.addOrReplaceComponentToColumn("form", 0, createForm())
        if (Payment::class.isInstance(transaction)) {
            dynamicPanel.addOrReplaceComponentToColumn("form", 1, createDivision(transaction as Payment))
        }
        dynamicPanel.addOrReplaceComponentToColumn("buttons", 0, createButtonsBar())
        addOrReplace(dynamicPanel)
    }

    private fun createButtonsBar(): Component {
        return ButtonGroup(
            ROW_CONTENT_ID,
            object : SimpleAjaxButton(
                ComponentListView.CONTENT_ID,
                "Back",
                Buttons.Type.Primary,
                SimpleAjaxButton.Size.SMALL,
                translator,
                true
            ) {
                override fun onClick(target: AjaxRequestTarget) {
                    modelCache.selectedTransaction = null
                    switchToPanel(FiltersPanel(), target)
                }
            }
        )
    }

    private fun createDivision(payment: Payment): Component {
        val panel = DynamicPanel(ROW_CONTENT_ID).addRows(
            "List" to intArrayOf(12),
            "Buttons" to intArrayOf(2, 2, 8),
            "New" to intArrayOf(12)
        ).addOrReplaceComponentToColumn("List", 0, createChildList())
            .addOrReplaceComponentToColumn("Buttons", 0, createAddButton(payment))
            .addOrReplaceComponentToColumn("New", 0, createNewChildForm(!newChild.empty))
        return panel
    }

    private fun createNewChildForm(visible: Boolean): Component {
        val formComponent = object : DynamicFormComponent<ManualTransaction>(
            ROW_CONTENT_ID,
            "New transaction",
            CompoundPropertyModel.of(newChild),
            translator
        ) {
            override fun onAfterSubmit(target: AjaxRequestTarget, typedModelObject: ManualTransaction) {
                if (!typedModelObject.empty) {
                    typedModelObject.store()
                }
                newChild = ManualTransaction.EMPTY
            }

            override fun onSubmitCompleted(target: AjaxRequestTarget, typedModelObject: ManualTransaction) {
                reload(target)
            }
        }.addTextBox("description", "Description")
            .addTextBox("mutation", "Amount ${newChild.currency.symbol}", AmountTransformer())
            .addSelect("category", "Category", Categories.values().toList(), Categories.MANUAL)
            .addTextBox("counterName", "Receiver")
        formComponent.isVisible = visible
        return formComponent
    }

    private fun createAddButton(payment: Payment): Component {
        return object : SimpleAjaxButton(
            ROW_CONTENT_ID,
            "+",
            Buttons.Type.Outline_Primary,
            SimpleAjaxButton.Size.SMALL,
            translator,
            true
        ) {
            override fun onClick(target: AjaxRequestTarget) {
                newChild = ManualTransaction(
                    UUIDUtil.createUUID(modelCache.occupiedIDs.toTypedArray()),
                    payment.id,
                    LocalDate.now(),
                    payment.baseAccount,
                    payment.currency,
                    Amount(BigDecimal.ZERO, payment.mutation.symbol),
                    MutationTypes.MAN,
                    StringUtils.EMPTY
                )
                reload(target)
            }
        }
    }

    private fun createChildList(): Component {
        val children = modelCache.getChildrenFor(transaction.id)
        val tuples = runBlocking { tupleUtils.transactionsToTuples(children, true, ModelCache.PERIOD.NONE) }
        return DynamicDataTable.get(ROW_CONTENT_ID, tuples, 3, 20, translator)
    }

    private fun createForm(): Component {
        return DynamicFormComponent<Transaction>(
            ROW_CONTENT_ID,
            "Details",
            CompoundPropertyModel.of(transaction),
            translator
        ).addSelect("category", "Category", Categories.values().toList(), transaction.category)
    }

    private fun createSummaryRight(): Component {
        return KeyValueView(
            ROW_CONTENT_ID,
            translator,
            "Account" to transaction.baseAccount,
            "Offset account" to transaction.counter(),
            "Transaction date" to transaction.date
        )
    }

    private fun createSummaryLeft(): Component {
        return KeyValueView(
            ROW_CONTENT_ID,
            translator,
            "Type" to transaction.mutationType,
            "Amount" to transaction.mutation,
            "Category" to transaction.category
        )
    }
}