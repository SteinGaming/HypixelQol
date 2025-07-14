package de.steingaming.hqol.fabric.config

import dev.isxander.yacl3.api.ConfigCategory

abstract class CategoryBase {
    abstract fun generateSubcategoryUI(): ConfigCategory
}