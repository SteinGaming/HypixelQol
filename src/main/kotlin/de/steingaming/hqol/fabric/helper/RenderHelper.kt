package de.steingaming.hqol.fabric.helper

//? if >= 26.1 {
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack
import net.fabricmc.fabric.api.client.rendering.v1.world.LevelRenderContext
*///?}
import net.minecraft.client.Camera
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

// Most of the code here is very much inspired by SkyHanni, as getting the camera to sync to the actual location
// is somehow hard?
object RenderHelper {
    var camera: Camera? = null
    var tickCounter: DeltaTracker? = null
    @JvmStatic
    fun beginRender(camera: Camera, tickCounter: DeltaTracker) {
        this.camera = camera
        this.tickCounter = tickCounter
    }

    fun exactLocation(entity: Entity, partialTicks: Float): Vec3 {
        if (!entity.isAlive) return entity.position()
        val x = entity.xOld + (entity.x - entity.xOld) * partialTicks
        val y = entity.yOld + (entity.y - entity.yOld) * partialTicks
        val z = entity.zOld + (entity.z - entity.zOld) * partialTicks
        return Vec3(x, y, z)
    }

    fun crosshairPosition(): Vec3 {
        val player = Minecraft.getInstance().player!!
        val partialTicks = tickCounter!!.getGameTimeDeltaPartialTick(true)
        val eyeHeight = player.eyeHeight
        return exactLocation(player, partialTicks).add(0.0, eyeHeight.toDouble(), 0.0).add(player.lookAngle.multiply(2.0, 2.0, 2.0))
    }
    fun drawLineFromCrosshair(context: LevelRenderContext, pos: Vec3, color: Int, lineWidth: Float) {
        drawLine(context, crosshairPosition(), pos, color, lineWidth)
    }
    fun drawLine(
        context: LevelRenderContext, pos1: Vec3, pos2: Vec3, color: Int, lineWidth: Float
    ) {
        val normal = pos2.subtract(pos1).normalize()
        val mc = Minecraft.getInstance()
        context.poseStack().pushPose()
        val inverseView = mc.gameRenderer.mainCamera.position().multiply(-1.0, -1.0, -1.0)
        context.poseStack().translate(inverseView)

        val buf = mc.gameRenderer.renderBuffers.bufferSource().getBuffer(RenderTypes.LINES_TRANSLUCENT)
        val matrix = context.poseStack().last()
        buf.addVertex(matrix.pose(), pos1.x.toFloat(), pos1.y.toFloat(), pos1.z.toFloat())
            .setNormal(matrix, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat())
            .setColor(color)
            .setLineWidth(lineWidth)
        buf.addVertex(matrix.pose(), pos2.x.toFloat(), pos2.y.toFloat(), pos2.z.toFloat())
            .setNormal(matrix, normal.x.toFloat(), normal.y.toFloat(), normal.z.toFloat())
            .setColor(color)
            .setLineWidth(lineWidth)
        context.poseStack().popPose()
    }
    //? if < 26.1 {
    /*fun LevelRenderContext.poseStack(): PoseStack = matrices()
    *///?}

}