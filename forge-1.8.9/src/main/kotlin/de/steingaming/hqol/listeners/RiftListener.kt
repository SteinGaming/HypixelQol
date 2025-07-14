package de.steingaming.hqol.listeners

import de.steingaming.hqol.Utilities.cleanupColorCodes
import de.steingaming.hqol.mixins.transformers.AccessorMinecraft
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.random.Random

object RiftListener {

    val coroutineRift = CoroutineScope(Dispatchers.Default)
    const val ENTITY_MAX_DISTANCE = 20
    val STATUS_BAR_TRIGGER = Regex(".*TWINCLAWS 0\\.[0-9].*")
    val TWINCLAW_ITEM = Item.getItemById(264)
    val MELON_ITEM = Item.getItemById(360)

    var melonEnabled = true
    var iceEnabled = true


    var cooldownPeriod = 0

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun tickEvent(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START ||
            Minecraft.getMinecraft().theWorld == null ||
            Minecraft.getMinecraft()?.ingameGUI?.chatGUI?.chatOpen != false ||
            Minecraft.getMinecraft()?.currentScreen != null
        ) return

        if (cooldownPeriod > 0) {
            cooldownPeriod--
            return
        }

        if (melonCheck()) return

        if (cooldownPeriod > 0) {
            return
        }

        twinclawCheck()
        return

    }

    private fun melonCheck(): Boolean {
        if (!melonEnabled) return false
        val player = Minecraft.getMinecraft().thePlayer
        val healthPercentage = (player.health / player.maxHealth)

        if (healthPercentage > 0.55)
            return false
        val (foundItem: ItemStack?, itemSlot) = findItemOfType(MELON_ITEM)
        if (foundItem == null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("Oops! Didn't find your melon item in your hotbar! No health for you!"))
            return false
        }
        cooldownPeriod = 15
        pressGivenItemAndSwitchBack(itemSlot)
        return true
    }

    private fun twinclawCheck() {
        if (!iceEnabled || !twinclawsAboutToAttack()) {
            return
        }

        val (foundItem: ItemStack?, itemSlot) = findItemOfType(TWINCLAW_ITEM)
        if (foundItem == null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("Oops! Didn't find your ice in your hotbar!"))
            return
        }
        pressGivenItemAndSwitchBack(itemSlot)

        cooldownPeriod = 20
    }

    private fun findItemOfType(searchedItemType: Item): Pair<ItemStack?, Int> {
        val playerInventory = Minecraft.getMinecraft().thePlayer.inventory.mainInventory
        var foundItem: ItemStack? = null
        var itemSlot = -1
        for (i in 0 until 9) {
            val hotbarItem = playerInventory[i]
            // Maybe also check for item name?
            if (hotbarItem == null || hotbarItem.item != searchedItemType)
                continue
            foundItem = hotbarItem
            itemSlot = i
            break
        }
        return Pair(foundItem, itemSlot)
    }

    val changeItemMutex = Mutex()

    @OptIn(DelicateCoroutinesApi::class)
    private fun pressGivenItemAndSwitchBack(itemSlot: Int) = runBlocking {
        if (changeItemMutex.tryLock()) return@runBlocking
        val previousItemSlot = Minecraft.getMinecraft().thePlayer.inventory.currentItem
        coroutineRift.launch {
            Minecraft.getMinecraft().thePlayer.inventory.currentItem = itemSlot
            delay(Random.nextLong(70, 150))
            val mc = (Minecraft.getMinecraft() as AccessorMinecraft)
            mc.rightClickMouse_hqol()
            delay(Random.nextLong(70, 100))
            if (Minecraft.getMinecraft().currentScreen != null) return@launch
            Minecraft.getMinecraft().thePlayer.inventory.currentItem = previousItemSlot
            changeItemMutex.unlock()
        }.start()
    }

    private fun twinclawsAboutToAttack(): Boolean {
        val playerLocation = Minecraft.getMinecraft()?.thePlayer?.position ?: return false
        if (playerLocation?.x == null) return false
        val boundingBox = AxisAlignedBB(
            playerLocation.x - ENTITY_MAX_DISTANCE.toDouble(),
            playerLocation.y - ENTITY_MAX_DISTANCE.toDouble(),
            playerLocation.z - ENTITY_MAX_DISTANCE.toDouble(),
            playerLocation.x + ENTITY_MAX_DISTANCE.toDouble(),
            playerLocation.y + ENTITY_MAX_DISTANCE.toDouble(),
            playerLocation.z + ENTITY_MAX_DISTANCE.toDouble()
        )
        val nearbyArmorStands = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB<EntityArmorStand>(
            EntityArmorStand::class.java,
            boundingBox
        )
        val playerName = Minecraft.getMinecraft().thePlayer.gameProfile.name
        val spawnedByRegex = Regex("Spawned by: $playerName")

        val statusBar = nearbyArmorStands.find {
            val text = it.displayName.unformattedText.cleanupColorCodes()
            STATUS_BAR_TRIGGER.matches(text)
        } ?: return false

        nearbyArmorStands.find {
            val text = it.displayName.unformattedText.cleanupColorCodes()
            spawnedByRegex.matches(text) && statusBar.getDistanceToEntity(it) < 1.0
        } ?: return false
        return true
    }
}

