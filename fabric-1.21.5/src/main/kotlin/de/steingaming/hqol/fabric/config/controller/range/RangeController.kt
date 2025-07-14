package de.steingaming.hqol.fabric.config.controller.range

import dev.isxander.yacl3.api.Controller
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.utils.Dimension
import dev.isxander.yacl3.gui.AbstractWidget
import dev.isxander.yacl3.gui.YACLScreen
import net.minecraft.text.Text

class RangeController(val option: Option<RangeValue>, val min: Int, val max: Int): Controller<RangeValue> {

    override fun option(): Option<RangeValue> {
        return option
    }

    override fun formatValue(): Text {
        val currentValue = option.pendingValue()
        return Text.of("${currentValue.lowerValue} - ${currentValue.upperValue}")
    }

    override fun provideWidget(
        screen: YACLScreen,
        dim: Dimension<Int>
    ): AbstractWidget? {
        val value = option.pendingValue()
        return RangeControllerWidget(this, screen, dim, min, max, 1)
    }
}