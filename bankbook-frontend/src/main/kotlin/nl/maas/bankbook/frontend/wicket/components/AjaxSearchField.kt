package nl.maas.bankbook.frontend.wicket.components

import org.apache.commons.lang3.StringUtils
import org.apache.wicket.ajax.AjaxRequestTarget
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior
import org.apache.wicket.markup.html.form.TextField
import org.apache.wicket.model.IModel
import java.util.*
import java.util.concurrent.TimeUnit

abstract class AjaxSearchField(id: String, model: IModel<String>) : TextField<String>(id, model) {

    private var started: String = StringUtils.EMPTY
    override fun onBeforeRender() {
        super.onBeforeRender()
    }

    init {
        add(object : OnChangeAjaxBehavior() {

            @Synchronized
            override fun onUpdate(target: AjaxRequestTarget) {
//                var oldValue = StringUtils.EMPTY
//                while (!oldValue.equals(this@AjaxSearchField.convertedInput.orEmpty())) {
//                    TimeUnit.MILLISECONDS.sleep(10)
//                    oldValue = this@AjaxSearchField.convertedInput.orEmpty()
//                }
//                if (!started.equals(oldValue)) {
//                    started = oldValue
//                findParent(BasePage::class.java).ajaxStartLoader(target)
                this@AjaxSearchField.onChange(target)
//                findParent(BasePage::class.java).ajaxStopLoader(target)
//                }
            }
        })
    }

    protected abstract fun onChange(target: AjaxRequestTarget)

    private inner class FilterTask : TimerTask() {
        override fun run() {
            var oldValue = StringUtils.EMPTY
            while (!oldValue.equals(this@AjaxSearchField.convertedInput.orEmpty())) {
                TimeUnit.MILLISECONDS.sleep(10)
                oldValue = this@AjaxSearchField.convertedInput.orEmpty()
            }
            if (!started.equals(oldValue)) {
                started = oldValue
//                findParent(BasePage::class.java).ajaxStartLoader(target)
//                this@AjaxSearchField.onChange(target)
//                findParent(BasePage::class.java).ajaxStopLoader(target)
            }
        }

    }

}