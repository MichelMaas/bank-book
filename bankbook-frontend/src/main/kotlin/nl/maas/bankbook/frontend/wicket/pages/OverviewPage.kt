package nl.maas.bankbook.frontend.wicket.pages

import org.apache.wicket.AttributeModifier
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.request.mapper.parameter.PageParameters

class OverviewPage(parameters: PageParameters?) : BasePage(parameters) {
    private val panelToHideL = CollapsablePanel("panelToHideL")
    private val panelToHideR = CollapsablePanel("panelToHideR")

    private inner class CollapsablePanel(id: String) : WebMarkupContainer(id) {
        private var visible = false

        override fun onBeforeRender() {
            super.onBeforeRender()
            setClasses()
        }

        fun setVisibilty(visible: Boolean): CollapsablePanel {
            this.visible = visible
            return this
        }

        private fun setClasses() {
            if (visible)
                add(AttributeModifier.replace("class", "accordion-collapse collapse show"))
            else
                add(AttributeModifier.replace("class", "accordion-collapse collapse"))
        }

        fun toggleVisible(): CollapsablePanel {
            visible = !visible
            return this
        }
    }

//    override fun onBeforeRender() {
//        super.onBeforeRender()
//        makeUpLeftDataColumn()
//        makeUpRightDataColumn()
//        makeUpHoldDataRow()
//        makeUpTransfers()
//        makeUpTransactions()
//    }
//
//    private fun makeUpHoldDataRow() {
//        val right = listOf(
//            Pair(
//                propertiesCache.translator.translate(this::class, "AverageTimeHeld"),
//                modelCache.fxDataSet.avgHoldTime().toString()
//            ),
//            Pair(
//                propertiesCache.translator.translate(this::class, "AverageTimeHeldWin"),
//                modelCache.fxDataSet.avgHoldTimeByResult(WON).toString()
//            ),
//            Pair(
//                propertiesCache.translator.translate(this::class, "AverageTimeHeldLoss"),
//                modelCache.fxDataSet.avgHoldTimeByResult(LOST).toString()
//            )
//        )
//        addOrReplace(object : ListView<Pair<String, String>>("dataHold", right) {
//            override fun populateItem(p0: ListItem<Pair<String, String>>?) {
//                p0!!.addOrReplace(
//                    SingleDataViewPanel(
//                        "data",
//                        p0.modelObject,
//                        propertiesCache.translator.translate(this@OverviewPage::class, "H"),
//                        false
//                    )
//                )
//            }
//
//        })
//    }
//
//    private fun makeUpRightDataColumn() {
//        val right = listOf(
//            Pair(propertiesCache.translator.translate(this::class, "Profit"), modelCache.fxDataSet.profit().toString()),
//            Pair(
//                propertiesCache.translator.translate(this::class, "AverageLoss"),
//                modelCache.fxDataSet.avgLoss().toString()
//            ),
//            Pair(
//                propertiesCache.translator.translate(this::class, "WinPercentage"),
//                modelCache.fxDataSet.winPercentage().toString()
//            )
//        )
//        addOrReplace(object : ListView<Pair<String, String>>("dataR", right) {
//            override fun populateItem(p0: ListItem<Pair<String, String>>?) {
//                p0!!.addOrReplace(
//                    SingleDataViewPanel(
//                        "data",
//                        p0.modelObject,
//                        if (p0.index == 2) "%" else modelCache.fxDataSet.currencySymbol(),
//                        p0.index != 2
//                    )
//                )
//            }
//
//        })
//    }
//
//    private fun makeUpLeftDataColumn() {
//        val left = listOf(
//            Pair(
//                propertiesCache.translator.translate(this::class, "AccountValue"),
//                modelCache.fxDataSet.worth().toString()
//            ),
//            Pair(
//                propertiesCache.translator.translate(this::class, "AverageProfit"),
//                modelCache.fxDataSet.avgProfit().toString()
//            ),
//            Pair(propertiesCache.translator.translate(this::class, "Risk"), modelCache.fxDataSet.risk().toString())
//        )
//
//        addOrReplace(object : ListView<Pair<String, String>>("dataL", left) {
//            override fun populateItem(p0: ListItem<Pair<String, String>>?) {
//                p0!!.addOrReplace(
//                    SingleDataViewPanel(
//                        "data",
//                        p0.modelObject,
//                        if (p0.index == 2) "%" else modelCache.fxDataSet.currencySymbol(),
//                        p0.index != 2
//                    )
//                )
//            }
//
//        })
//    }
//
//    fun makeUpTransactions() {
//        val memberpanel = this
//        val button = object : AjaxLink<String>("buttonR") {
//            init {
//                add(
//                    Label(
//                        "buttonRLabel",
//                        propertiesCache.translator.translate(this@OverviewPage::class, "Transactions")
//                    )
//                )
//            }
//
//            override fun onClick(target: AjaxRequestTarget) {
//                panelToHideR.toggleVisible()
//                panelToHideL.setVisibilty(false)
//                target.add(memberpanel)
//            }
//        }
//        addOrReplace(button)
//        panelToHideR.addOrReplace(TransactionsPanel("transactions", CompoundPropertyModel.of(modelCache.fxDataSet)))
//        addOrReplace(panelToHideR)
//    }
//
//
//    fun makeUpTransfers() {
//        val memberpanel = this
//        val button = object : AjaxLink<String>("buttonL") {
//            init {
//                add(Label("buttonLLabel", propertiesCache.translator.translate(this@OverviewPage::class, "Transfers")))
//            }
//
//            override fun onClick(target: AjaxRequestTarget) {
//                panelToHideL.toggleVisible()
//                panelToHideR.setVisibilty(false)
//                target.add(memberpanel)
//            }
//        }
//        addOrReplace(button)
//        panelToHideL.addOrReplace(TransfersPanel("transfers", CompoundPropertyModel.of(modelCache.fxDataSet)))
//        addOrReplace(panelToHideL)
//    }
}