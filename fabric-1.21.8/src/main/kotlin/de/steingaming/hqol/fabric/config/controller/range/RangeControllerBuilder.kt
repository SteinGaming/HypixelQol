package de.steingaming.hqol.fabric.config.controller.range

import dev.isxander.yacl3.api.Controller
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.impl.controller.AbstractControllerBuilderImpl
import net.minecraft.text.Text
import kotlin.reflect.KMutableProperty0

class RangeControllerBuilder(val option: Option<RangeValue>, val min: Int, val max: Int): AbstractControllerBuilderImpl<RangeValue>(option) {
    companion object {
        @JvmStatic
        fun create(option: Option<RangeValue>, min: Int, max: Int): RangeControllerBuilder {
            return RangeControllerBuilder(option, min, max)
        }

        @JvmStatic
        fun OptionGroup.Builder.rangeOption(
            min: Int,
            max: Int,
            defaultValue: RangeValue,
            field: KMutableProperty0<RangeValue>,
            name: Text,
            description: OptionDescription? = null
        ): OptionGroup.Builder {
            return this.option {
                Option.createBuilder<RangeValue>()
                    .name(name)
                    .binding(defaultValue, {
                        field.get()
                    }, {
                        field.set(it)
                    }).controller {
                        create(it, min, max)
                    }.apply {
                        if (description != null) {
                            this.description(description)
                        }
                    }
                    .build()
            }
        }
    }
    override fun build(): Controller<RangeValue> {
        return RangeController(option, min, max)
    }
}