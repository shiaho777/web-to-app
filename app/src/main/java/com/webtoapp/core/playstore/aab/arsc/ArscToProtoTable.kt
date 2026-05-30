package com.webtoapp.core.playstore.aab.arsc

import com.android.aapt.ConfigurationOuterClass.Configuration
import com.android.aapt.Resources
import com.webtoapp.core.playstore.aab.axml.AxmlConstants

object ArscToProtoTable {

    internal fun convert(table: ArscResourceTable): Resources.ResourceTable {
        val builder = Resources.ResourceTable.newBuilder()
        for (pkg in table.packages) {
            builder.addPackage(convertPackage(pkg, table.valueStringPool))
        }
        return builder.build()
    }

    private fun convertPackage(
        pkg: ArscPackage,
        valuePool: List<String>
    ): Resources.Package {
        val packageBuilder = Resources.Package.newBuilder()
            .setPackageId(
                Resources.PackageId.newBuilder()
                    .setId(pkg.id)
                    .build()
            )
            .setPackageName(pkg.name)

        val typesByTypeId = pkg.types.groupBy { it.typeId }
        val typeSpecsByTypeId = pkg.typeSpecs.associateBy { it.typeId }

        for (typeId in typesByTypeId.keys.sorted()) {
            val typeChunks = typesByTypeId.getValue(typeId)
            val typeName = pkg.typeNames.getOrNull(typeId - 1) ?: continue
            val typeSpec = typeSpecsByTypeId[typeId]

            packageBuilder.addType(
                convertType(typeId, typeName, typeChunks, typeSpec, pkg.keyNames, valuePool)
            )
        }
        return packageBuilder.build()
    }

    private fun convertType(
        typeId: Int,
        typeName: String,
        typeChunks: List<ArscType>,
        typeSpec: ArscTypeSpec?,
        keyNames: List<String>,
        valuePool: List<String>
    ): Resources.Type {
        val typeBuilder = Resources.Type.newBuilder()
            .setTypeId(Resources.TypeId.newBuilder().setId(typeId).build())
            .setName(typeName)

        val totalEntries = typeSpec?.configFlags?.size
            ?: typeChunks.maxOf { it.entries.size }
        val configValuesByEntry: Array<MutableList<Pair<ArscConfig, ArscEntry>>> =
            Array(totalEntries) { mutableListOf() }

        var canonicalName: Array<String?> = arrayOfNulls(totalEntries)

        for (chunk in typeChunks) {
            for ((idx, entry) in chunk.entries.withIndex()) {
                if (entry == null) continue
                if (idx >= totalEntries) continue
                configValuesByEntry[idx].add(chunk.config to entry)
                if (canonicalName[idx] == null && entry.name.isNotEmpty()) {
                    canonicalName[idx] = entry.name
                }
            }
        }

        for (entryIndex in 0 until totalEntries) {
            val configValues = configValuesByEntry[entryIndex]
            if (configValues.isEmpty()) continue

            val entryName = canonicalName[entryIndex] ?: continue
            val entryBuilder = Resources.Entry.newBuilder()
                .setEntryId(Resources.EntryId.newBuilder().setId(entryIndex).build())
                .setName(entryName)

            val anyPublic = configValues.any { it.second.flags and ENTRY_FLAG_PUBLIC != 0 }
            if (anyPublic) {
                entryBuilder.setVisibility(
                    Resources.Visibility.newBuilder()
                        .setLevel(Resources.Visibility.Level.PUBLIC)
                        .build()
                )
            }

            val seenConfigBytes = mutableSetOf<List<Byte>>()
            for ((cfg, entry) in configValues) {
                val protoConfig = convertConfig(cfg)
                val key = protoConfig.toByteArray().toList()
                if (!seenConfigBytes.add(key)) continue
                entryBuilder.addConfigValue(
                    Resources.ConfigValue.newBuilder()
                        .setConfig(protoConfig)
                        .setValue(convertEntryValue(entry, keyNames, valuePool))
                        .build()
                )
            }
            typeBuilder.addEntry(entryBuilder.build())
        }

        return typeBuilder.build()
    }

    private fun convertEntryValue(
        entry: ArscEntry,
        keyNames: List<String>,
        valuePool: List<String>
    ): Resources.Value {
        val valueBuilder = Resources.Value.newBuilder()

        when (val body = entry.body) {
            is ArscEntryBody.Simple -> {
                valueBuilder.setItem(convertSimpleValue(body.value, valuePool))
            }
            is ArscEntryBody.Bag -> {
                valueBuilder.setCompoundValue(
                    convertBag(entry.name, body, keyNames, valuePool)
                )
            }
        }
        return valueBuilder.build()
    }

    private fun convertSimpleValue(
        value: ArscValue,
        valuePool: List<String>
    ): Resources.Item {
        return when (value.dataType) {
            AxmlConstants.TYPE_STRING -> {
                val text = valuePool.getOrElse(value.data) { "" }

                if (text.startsWith("res/")) {
                    Resources.Item.newBuilder()
                        .setFile(buildFileReference(text))
                        .build()
                } else {
                    Resources.Item.newBuilder()
                        .setStr(Resources.String.newBuilder().setValue(text).build())
                        .build()
                }
            }
            AxmlConstants.TYPE_REFERENCE -> {

                if (value.data == 0) {
                    Resources.Item.newBuilder()
                        .setRef(Resources.Reference.getDefaultInstance())
                        .build()
                } else {
                    Resources.Item.newBuilder()
                        .setRef(
                            Resources.Reference.newBuilder()
                                .setType(Resources.Reference.Type.REFERENCE)
                                .setId(value.data)
                                .build()
                        )
                        .build()
                }
            }
            AxmlConstants.TYPE_ATTRIBUTE -> {
                if (value.data == 0) {

                    Resources.Item.newBuilder()
                        .setRef(
                            Resources.Reference.newBuilder()
                                .setType(Resources.Reference.Type.ATTRIBUTE)
                                .build()
                        )
                        .build()
                } else {
                    Resources.Item.newBuilder()
                        .setRef(
                            Resources.Reference.newBuilder()
                                .setType(Resources.Reference.Type.ATTRIBUTE)
                                .setId(value.data)
                                .build()
                        )
                        .build()
                }
            }
            AxmlConstants.TYPE_NULL -> {

                val primBuilder = Resources.Primitive.newBuilder()
                if (value.data == 1) {
                    primBuilder.setEmptyValue(
                        Resources.Primitive.EmptyType.getDefaultInstance()
                    )
                } else {
                    primBuilder.setNullValue(
                        Resources.Primitive.NullType.getDefaultInstance()
                    )
                }
                Resources.Item.newBuilder()
                    .setPrim(primBuilder.build())
                    .build()
            }
            AxmlConstants.TYPE_INT_BOOLEAN -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setBooleanValue(value.data != 0)
                            .build()
                    )
                    .build()
            }
            AxmlConstants.TYPE_INT_DEC -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setIntDecimalValue(value.data)
                            .build()
                    )
                    .build()
            }
            AxmlConstants.TYPE_INT_HEX -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setIntHexadecimalValue(value.data)
                            .build()
                    )
                    .build()
            }
            AxmlConstants.TYPE_FLOAT -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setFloatValue(Float.fromBits(value.data))
                            .build()
                    )
                    .build()
            }
            AxmlConstants.TYPE_DIMENSION -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setDimensionValue(value.data)
                            .build()
                    )
                    .build()
            }
            AxmlConstants.TYPE_FRACTION -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setFractionValue(value.data)
                            .build()
                    )
                    .build()
            }
            AxmlConstants.TYPE_INT_COLOR_ARGB8 -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setColorArgb8Value(value.data)
                            .build()
                    )
                    .build()
            }
            AxmlConstants.TYPE_INT_COLOR_RGB8 -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setColorRgb8Value(value.data)
                            .build()
                    )
                    .build()
            }
            AxmlConstants.TYPE_INT_COLOR_ARGB4 -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setColorArgb4Value(value.data)
                            .build()
                    )
                    .build()
            }
            AxmlConstants.TYPE_INT_COLOR_RGB4 -> {
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setColorRgb4Value(value.data)
                            .build()
                    )
                    .build()
            }
            else -> {

                Resources.Item.newBuilder()
                    .setStr(Resources.String.newBuilder().setValue("").build())
                    .build()
            }
        }
    }

    private fun convertBag(
        entryName: String,
        bag: ArscEntryBody.Bag,
        @Suppress("UNUSED_PARAMETER") keyNames: List<String>,
        valuePool: List<String>
    ): Resources.CompoundValue {

        val firstName = bag.entries.firstOrNull()?.nameRef ?: 0

        return when {

            firstName == ATTR_TYPE -> {
                val attrBuilder = Resources.Attribute.newBuilder()
                for (e in bag.entries) {
                    when {
                        e.nameRef == ATTR_TYPE ->
                            attrBuilder.setFormatFlags(e.value.data)
                        e.nameRef == ATTR_MIN ->
                            attrBuilder.setMinInt(e.value.data)
                        e.nameRef == ATTR_MAX ->
                            attrBuilder.setMaxInt(e.value.data)

                        (e.nameRef and 0xFFFF0000.toInt()) == 0x01000000 -> {

                        }
                        else -> {

                            attrBuilder.addSymbol(
                                Resources.Attribute.Symbol.newBuilder()
                                    .setName(
                                        Resources.Reference.newBuilder()
                                            .setType(Resources.Reference.Type.REFERENCE)
                                            .setId(e.nameRef)
                                            .build()
                                    )
                                    .setValue(e.value.data)
                                    .setType(e.value.dataType)
                                    .build()
                            )
                        }
                    }
                }
                Resources.CompoundValue.newBuilder().setAttr(attrBuilder.build()).build()
            }

            (firstName ushr 24) == 0x02 -> {
                val arrayBuilder = Resources.Array.newBuilder()
                for (e in bag.entries) {
                    arrayBuilder.addElement(
                        Resources.Array.Element.newBuilder()
                            .setItem(convertSimpleValue(e.value, valuePool))
                            .build()
                    )
                }
                Resources.CompoundValue.newBuilder().setArray(arrayBuilder.build()).build()
            }

            firstName in ATTR_OTHER..ATTR_MANY -> {
                val pluralBuilder = Resources.Plural.newBuilder()
                for (e in bag.entries) {
                    val arity = pluralArityFor(e.nameRef) ?: continue
                    pluralBuilder.addEntry(
                        Resources.Plural.Entry.newBuilder()
                            .setArity(arity)
                            .setItem(convertSimpleValue(e.value, valuePool))
                            .build()
                    )
                }
                Resources.CompoundValue.newBuilder().setPlural(pluralBuilder.build()).build()
            }

            else -> {
                val styleBuilder = Resources.Style.newBuilder()
                if (bag.parentRef != 0) {
                    styleBuilder.setParent(
                        Resources.Reference.newBuilder()
                            .setType(Resources.Reference.Type.REFERENCE)
                            .setId(bag.parentRef)
                            .build()
                    )
                }
                for (e in bag.entries) {
                    styleBuilder.addEntry(
                        Resources.Style.Entry.newBuilder()
                            .setKey(
                                Resources.Reference.newBuilder()
                                    .setType(Resources.Reference.Type.REFERENCE)
                                    .setId(e.nameRef)
                                    .build()
                            )
                            .setItem(convertSimpleValue(e.value, valuePool))
                            .build()
                    )
                }
                @Suppress("UNUSED_VARIABLE")
                val _entryName = entryName
                Resources.CompoundValue.newBuilder().setStyle(styleBuilder.build()).build()
            }
        }
    }

    private fun pluralArityFor(nameRef: Int): Resources.Plural.Arity? = when (nameRef) {
        ATTR_OTHER -> Resources.Plural.Arity.OTHER
        ATTR_ZERO -> Resources.Plural.Arity.ZERO
        ATTR_ONE -> Resources.Plural.Arity.ONE
        ATTR_TWO -> Resources.Plural.Arity.TWO
        ATTR_FEW -> Resources.Plural.Arity.FEW
        ATTR_MANY -> Resources.Plural.Arity.MANY
        else -> null
    }

    private fun buildFileReference(path: String): Resources.FileReference {
        val type = when {
            path.endsWith(".xml") -> Resources.FileReference.Type.PROTO_XML
            path.endsWith(".png") || path.endsWith(".webp") || path.endsWith(".jpg") ||
                path.endsWith(".jpeg") || path.endsWith(".gif") ->
                Resources.FileReference.Type.PNG
            else -> Resources.FileReference.Type.UNKNOWN
        }
        return Resources.FileReference.newBuilder()
            .setPath(path)
            .setType(type)
            .build()
    }

    private fun convertConfig(cfg: ArscConfig): Configuration {
        val builder = Configuration.newBuilder()
        if (cfg.mcc != 0) builder.mcc = cfg.mcc
        if (cfg.mnc != 0) builder.mnc = cfg.mnc
        if (cfg.locale.isNotEmpty()) builder.locale = cfg.locale
        if (cfg.sdkVersion != 0) builder.sdkVersion = cfg.sdkVersion
        if (cfg.density != 0) builder.density = cfg.density
        if (cfg.screenWidth != 0) builder.screenWidth = cfg.screenWidth
        if (cfg.screenHeight != 0) builder.screenHeight = cfg.screenHeight
        if (cfg.smallestScreenWidthDp != 0) builder.smallestScreenWidthDp = cfg.smallestScreenWidthDp
        if (cfg.screenWidthDp != 0) builder.screenWidthDp = cfg.screenWidthDp
        if (cfg.screenHeightDp != 0) builder.screenHeightDp = cfg.screenHeightDp

        builder.orientation = orientationFromArsc(cfg.orientation)
        builder.touchscreen = touchscreenFromArsc(cfg.touchscreen)
        builder.keyboard = keyboardFromArsc(cfg.keyboard)
        builder.navigation = navigationFromArsc(cfg.navigation)
        builder.layoutDirection = layoutDirectionFromArsc(cfg.layoutDirection)
        builder.uiModeType = uiModeTypeFromArsc(cfg.uiMode and 0x0F)
        builder.uiModeNight = uiModeNightFromArsc((cfg.uiMode ushr 4) and 0x03)
        builder.screenLayoutSize = screenLayoutSizeFromArsc(cfg.screenLayout and 0x0F)
        builder.screenLayoutLong = screenLayoutLongFromArsc((cfg.screenLayout ushr 4) and 0x03)

        return builder.build()
    }

    private fun orientationFromArsc(b: Int): Configuration.Orientation = when (b) {
        0 -> Configuration.Orientation.ORIENTATION_UNSET
        1 -> Configuration.Orientation.ORIENTATION_PORT
        2 -> Configuration.Orientation.ORIENTATION_LAND
        3 -> Configuration.Orientation.ORIENTATION_SQUARE
        else -> Configuration.Orientation.ORIENTATION_UNSET
    }

    private fun touchscreenFromArsc(b: Int): Configuration.Touchscreen = when (b) {
        0 -> Configuration.Touchscreen.TOUCHSCREEN_UNSET
        1 -> Configuration.Touchscreen.TOUCHSCREEN_NOTOUCH
        2 -> Configuration.Touchscreen.TOUCHSCREEN_STYLUS
        3 -> Configuration.Touchscreen.TOUCHSCREEN_FINGER
        else -> Configuration.Touchscreen.TOUCHSCREEN_UNSET
    }

    private fun keyboardFromArsc(b: Int): Configuration.Keyboard = when (b) {
        0 -> Configuration.Keyboard.KEYBOARD_UNSET
        1 -> Configuration.Keyboard.KEYBOARD_NOKEYS
        2 -> Configuration.Keyboard.KEYBOARD_QWERTY
        3 -> Configuration.Keyboard.KEYBOARD_TWELVEKEY
        else -> Configuration.Keyboard.KEYBOARD_UNSET
    }

    private fun navigationFromArsc(b: Int): Configuration.Navigation = when (b) {
        0 -> Configuration.Navigation.NAVIGATION_UNSET
        1 -> Configuration.Navigation.NAVIGATION_NONAV
        2 -> Configuration.Navigation.NAVIGATION_DPAD
        3 -> Configuration.Navigation.NAVIGATION_TRACKBALL
        4 -> Configuration.Navigation.NAVIGATION_WHEEL
        else -> Configuration.Navigation.NAVIGATION_UNSET
    }

    private fun layoutDirectionFromArsc(b: Int): Configuration.LayoutDirection = when (b) {
        0 -> Configuration.LayoutDirection.LAYOUT_DIRECTION_UNSET
        1 -> Configuration.LayoutDirection.LAYOUT_DIRECTION_LTR
        2 -> Configuration.LayoutDirection.LAYOUT_DIRECTION_RTL
        else -> Configuration.LayoutDirection.LAYOUT_DIRECTION_UNSET
    }

    private fun uiModeTypeFromArsc(b: Int): Configuration.UiModeType = when (b) {
        0 -> Configuration.UiModeType.UI_MODE_TYPE_UNSET
        1 -> Configuration.UiModeType.UI_MODE_TYPE_NORMAL
        2 -> Configuration.UiModeType.UI_MODE_TYPE_DESK
        3 -> Configuration.UiModeType.UI_MODE_TYPE_CAR
        4 -> Configuration.UiModeType.UI_MODE_TYPE_TELEVISION
        5 -> Configuration.UiModeType.UI_MODE_TYPE_APPLIANCE
        6 -> Configuration.UiModeType.UI_MODE_TYPE_WATCH
        7 -> Configuration.UiModeType.UI_MODE_TYPE_VRHEADSET
        else -> Configuration.UiModeType.UI_MODE_TYPE_UNSET
    }

    private fun uiModeNightFromArsc(b: Int): Configuration.UiModeNight = when (b) {
        0 -> Configuration.UiModeNight.UI_MODE_NIGHT_UNSET

        1 -> Configuration.UiModeNight.UI_MODE_NIGHT_NOTNIGHT
        2 -> Configuration.UiModeNight.UI_MODE_NIGHT_NIGHT
        else -> Configuration.UiModeNight.UI_MODE_NIGHT_UNSET
    }

    private fun screenLayoutSizeFromArsc(b: Int): Configuration.ScreenLayoutSize = when (b) {
        0 -> Configuration.ScreenLayoutSize.SCREEN_LAYOUT_SIZE_UNSET
        1 -> Configuration.ScreenLayoutSize.SCREEN_LAYOUT_SIZE_SMALL
        2 -> Configuration.ScreenLayoutSize.SCREEN_LAYOUT_SIZE_NORMAL
        3 -> Configuration.ScreenLayoutSize.SCREEN_LAYOUT_SIZE_LARGE
        4 -> Configuration.ScreenLayoutSize.SCREEN_LAYOUT_SIZE_XLARGE
        else -> Configuration.ScreenLayoutSize.SCREEN_LAYOUT_SIZE_UNSET
    }

    private fun screenLayoutLongFromArsc(b: Int): Configuration.ScreenLayoutLong = when (b) {
        0 -> Configuration.ScreenLayoutLong.SCREEN_LAYOUT_LONG_UNSET
        1 -> Configuration.ScreenLayoutLong.SCREEN_LAYOUT_LONG_LONG
        2 -> Configuration.ScreenLayoutLong.SCREEN_LAYOUT_LONG_NOTLONG
        else -> Configuration.ScreenLayoutLong.SCREEN_LAYOUT_LONG_UNSET
    }

    private const val ATTR_TYPE = 0x01000000
    private const val ATTR_MIN = 0x01000001
    private const val ATTR_MAX = 0x01000002
    private const val ATTR_L10N = 0x01000003
    private const val ATTR_OTHER = 0x01000004
    private const val ATTR_ZERO = 0x01000005
    private const val ATTR_ONE = 0x01000006
    private const val ATTR_TWO = 0x01000007
    private const val ATTR_FEW = 0x01000008
    private const val ATTR_MANY = 0x01000009

    private const val ENTRY_FLAG_PUBLIC = 0x0002
}
