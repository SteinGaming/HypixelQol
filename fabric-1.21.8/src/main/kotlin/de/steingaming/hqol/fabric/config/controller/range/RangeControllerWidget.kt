package de.steingaming.hqol.fabric.config.controller.range

import dev.isxander.yacl3.api.Controller
import dev.isxander.yacl3.api.utils.Dimension
import dev.isxander.yacl3.gui.YACLScreen
import dev.isxander.yacl3.gui.controllers.ControllerWidget
import dev.isxander.yacl3.gui.utils.GuiUtils
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import kotlin.math.abs
import kotlin.math.roundToInt

class RangeControllerWidget(
    val rangeController: RangeController,
    screen: YACLScreen,
    dim: Dimension<Int>,
    val min: Int,
    val max: Int,
    val interval: Int
) : ControllerWidget<Controller<RangeValue>>(rangeController, screen, dim) {
    companion object {
        const val thumbWidth = 2
        const val thumbHeight = 10
    }

    var mouseDown = false
    lateinit var sliderBounds: Dimension<Int>

    val interpolationLower: Float
        get() = Math.clamp(
            (this.control.option().pendingValue().lowerValue - min).toFloat() / (max - min).toFloat(), 0.0f, 1.0f
        )

    val interpolationUpper: Float
        get() = Math.clamp(
            (this.control.option().pendingValue().upperValue - min).toFloat() / (max - min).toFloat(), 0.0f, 1.0f
        )

    val thumbXLower: Int
        get() = (sliderBounds.x() + sliderBounds.width().toFloat() * interpolationLower).toInt()
    val thumbXUpper: Int
        get() = (sliderBounds.x() + sliderBounds.width().toFloat() * interpolationUpper).toInt()

    init {
        setDimension(dim)
    }

    override fun render(
        graphics: DrawContext?,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        super.render(graphics, mouseX, mouseY, delta)
    }

    override fun drawHoveredControl(
        graphics: DrawContext,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        //println("Filling (${sliderBounds.x()}, ${sliderBounds.centerY() - 1}) to (${sliderBounds.xLimit()}, ${sliderBounds.centerY()})")
        graphics.fill(sliderBounds.x(), sliderBounds.centerY() - 1, sliderBounds.xLimit(), sliderBounds.centerY(), -1)
        // Shadow: graphics.fill(sliderBounds.x() + 1, sliderBounds.centerY(), sliderBounds.x(), sliderBounds.centerY() + 1, -12566464)
        graphics.fill(
            thumbXLower - thumbWidth / 2,
            sliderBounds.y(),
            thumbXLower + thumbWidth / 2,
            sliderBounds.yLimit(),
            -1
        )
        graphics.fill(
            thumbXUpper - thumbWidth / 2,
            sliderBounds.y(),
            thumbXUpper + thumbWidth / 2,
            sliderBounds.yLimit(),
            -1
        )
    }


    override fun getHoveredControlWidth(): Int = sliderBounds.width() + unhoveredControlWidth + 6 + thumbWidth / 2

    override fun drawValueText(
        graphics: DrawContext?,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        GuiUtils.pushPose(graphics)
        if (this.isHovered) {
            GuiUtils.translate2D(
                graphics,
                -((this.sliderBounds.width() + 6).toFloat() + thumbWidth.toFloat() / 2.0f),
                0.0f
            )
        }

        super.drawValueText(graphics, mouseX, mouseY, delta)
        GuiUtils.popPose(graphics)
    }

    override fun setDimension(dim: Dimension<Int>) {
        super.setDimension(dim)
        val sliderWidth =
            if (optionNameString.isNullOrEmpty()) dim.width() / 2
            else dim.width() / 3

        sliderBounds = Dimension.ofInt(
            dim.xLimit() - xPadding - thumbWidth / 2 - sliderWidth,
            dim.centerY() - thumbHeight / 2,
            sliderWidth,
            thumbHeight
        )
    }

    enum class SliderSelection {
        UPPER, LOWER;
    }

    var sliderSelection: SliderSelection? = null

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        return super.isMouseOver(mouseX, mouseY) || this.mouseDown
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return if (this.isAvailable && button == 0 && this.sliderBounds.isPointInside(mouseX.toInt(), mouseY.toInt())) {
            mouseDown = true
            selectSlider(mouseX)
            setValueFromMouse(mouseX)
            true
        } else false
    }

    override fun mouseDragged(
        mouseX: Double,
        mouseY: Double,
        button: Int,
        deltaX: Double,
        deltaY: Double
    ): Boolean {
        return if (this.isAvailable && button == 0 && this.mouseDown) {
            setValueFromMouse(mouseX)
            true
        } else false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.isAvailable && this.mouseDown) {
            playDownSound()
            this.mouseDown = false
            this.sliderSelection = null
        }

        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)

        if (!this.isAvailable && !this.isMouseOver(mouseX, mouseY) && !Screen.hasShiftDown())
            return false

        if (!Screen.hasControlDown()) {
            incrementLowerValue(verticalAmount)
        } else
            incrementUpperValue(horizontalAmount)

        return true
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return super.keyPressed(keyCode, scanCode, modifiers)
        if (!focused)
            return false

        val amount = when (keyCode) {
            262 -> 1.0
            263 -> -1.0
            else -> null
        } ?: return false

        if (!Screen.hasControlDown())
            incrementLowerValue(amount)
        else
            incrementUpperValue(amount)

        return true
    }

    private fun selectSlider(mouseX: Double) {
        val lowerDistanceFromMouse = abs(thumbXLower - mouseX)
        val upperDistanceFromMouse = abs(thumbXUpper - mouseX)
        if (upperDistanceFromMouse > lowerDistanceFromMouse)
            sliderSelection = SliderSelection.LOWER
        else if (lowerDistanceFromMouse > upperDistanceFromMouse)
            sliderSelection = SliderSelection.UPPER
    }

    private fun setValueFromMouse(mouseX: Double) {
        val selection = sliderSelection ?: let {
            selectSlider(mouseX)
            sliderSelection ?: return
        }

        when (selection) {
            SliderSelection.UPPER -> setUpperValueFromMouse(mouseX)
            SliderSelection.LOWER -> setLowerValueFromMouse(mouseX)
        }
    }

    private fun setLowerValueFromMouse(mouseX: Double) {
        val mouseValue = mouseCoordinateToValue(mouseX)
        val interpolatedValue =
            interpolateValue(mouseValue)
        val currentRange = control.option().pendingValue()
        if (interpolatedValue + interval > currentRange.upperValue) return
        control.option().requestSet(currentRange.copy(lowerValue = interpolatedValue))
    }

    private fun setUpperValueFromMouse(mouseX: Double) {
        val mouseValue = mouseCoordinateToValue(mouseX)
        val interpolatedValue =
            interpolateValue(mouseValue)
        val currentRange = control.option().pendingValue()
        if (interpolatedValue - interval < currentRange.lowerValue) return
        control.option().requestSet(currentRange.copy(upperValue = interpolatedValue))
    }

    private fun incrementLowerValue(value: Double) {
        TODO("broken currently")
        val currentRange = control.option().pendingValue()
        val interpolatedValue = interpolateValue(value + currentRange.lowerValue.toDouble())
        if (interpolatedValue + interval > currentRange.upperValue) return
        control.option().requestSet(currentRange.copy(lowerValue = interpolatedValue))
    }

    private fun incrementUpperValue(value: Double) {
        TODO("broken currently")
        val currentRange = control.option().pendingValue()
        val interpolatedValue = interpolateValue(value + currentRange.upperValue.toDouble())
        if (interpolatedValue - interval < currentRange.lowerValue) return
        control.option().requestSet(currentRange.copy(upperValue = interpolatedValue))
    }
    private fun mouseCoordinateToValue(mouseX: Double): Double =
        (mouseX - sliderBounds.x()) / sliderBounds.width() * (max - min)

    private fun interpolateValue(value: Double): Int =
        Math.clamp(min + interval * (value / interval).roundToInt().toDouble(), min.toDouble(), max.toDouble())
            .toInt()

}