package de.steingaming.hqol.fabric.config.categories

import de.steingaming.hqol.fabric.config.CategoryBase
import de.steingaming.hqol.fabric.config.controller.range.RangeControllerBuilder.Companion.rangeOption
import de.steingaming.hqol.fabric.config.controller.range.RangeValue
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder
import dev.isxander.yacl3.api.controller.StringControllerBuilder
import net.minecraft.text.Text

data class Fishing(
    var enabled: Boolean = false,
    var useLegacyDetection: Boolean = false,
    var waterHookDelayRange: RangeValue = RangeValue(150, 250),
    var lavaHookDelayRange: RangeValue = RangeValue(150, 250),
    var rethrowHookDelay: RangeValue = RangeValue(100, 150),
    var maximumSoundDistance: Double = 0.1,
    var legacyWaterSoundPath: String = "random/splash",
    var legacyLavaSoundPath: String = "game/player/swim/splash",
) : CategoryBase() {

    override fun generateSubcategoryUI(): ConfigCategory {
        val defaultConfig = Fishing()
        return ConfigCategory.createBuilder()
            .name(Text.literal("Fishing")).apply {
                rootGroupBuilder()
                    .option(
                        Option.createBuilder<Boolean>()
                            .name(Text.literal("Enabled"))
                            .binding(
                                defaultConfig.enabled,
                                { enabled },
                                { enabled = it }
                            )
                            .controller {
                                BooleanControllerBuilder.create(it)
                                    .coloured(true)
                                    .yesNoFormatter()
                            }.build()
                    ).option(
                        Option.createBuilder<Boolean>()
                            .name(Text.literal("Use Legacy detection method"))
                            .binding(
                                defaultConfig.useLegacyDetection,
                                { useLegacyDetection },
                                { useLegacyDetection = it }
                            )
                            .controller {
                                BooleanControllerBuilder.create(it)
                                    .coloured(true)
                                    .yesNoFormatter()
                            }.build()
                    )
            }.group {
                OptionGroup.createBuilder().name(Text.of("Timing Sliders"))
                    .rangeOption(
                        10, 400, defaultConfig.waterHookDelayRange, ::waterHookDelayRange,
                        Text.of("§9Water §rhook delay range (milliseconds)")
                    ).rangeOption(
                        10, 400, defaultConfig.lavaHookDelayRange, ::lavaHookDelayRange,
                        Text.of("§cLava §rhook delay range (milliseconds)")
                    ).rangeOption(
                        10, 300, defaultConfig.rethrowHookDelay, ::rethrowHookDelay,
                        Text.of("§6Re-throw §rhook delay range (milliseconds)")
                    ).build()
            }.group {
                OptionGroup.createBuilder().name(Text.of("Legacy Detection (Sound Based)"))
                    .option(
                        Option.createBuilder<Double>()
                            .name(Text.literal("Maximum sound distance to hook"))
                            .binding(
                                defaultConfig.maximumSoundDistance,
                                { maximumSoundDistance },
                                { maximumSoundDistance = it })
                            .controller {
                                DoubleSliderControllerBuilder.create(it)
                                    .range(0.0, 1.0)
                                    .step(0.05)
                            }
                            .build()
                    )
                    .option(
                        Option.createBuilder<String>()
                            .name(Text.of("Sound Trigger: §9Water §r(EXPERT OPTION)"))
                            .binding(
                                defaultConfig.legacyWaterSoundPath,
                                { legacyWaterSoundPath },
                                { legacyWaterSoundPath = it }
                            )
                            .controller(StringControllerBuilder::create)
                            .build()
                    )
                    .option(
                        Option.createBuilder<String>()
                            .name(Text.of("Sound Trigger: §cLava §r(EXPERT OPTION)"))
                            .binding(
                                defaultConfig.legacyLavaSoundPath,
                                { legacyLavaSoundPath },
                                { legacyLavaSoundPath = it }
                            )
                            .controller(StringControllerBuilder::create)
                            .build()
                    )
                    .build()
            }
            //.rangeOption(0, 100, defaultConfig.testValue, ::testValue, Text.literal("TestValue"))

            .build()
    }


}