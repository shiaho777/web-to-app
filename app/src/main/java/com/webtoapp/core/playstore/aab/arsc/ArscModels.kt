package com.webtoapp.core.playstore.aab.arsc

internal data class ArscValue(

    val dataType: Int,

    val data: Int
)

internal data class ArscBagEntry(

    val nameRef: Int,
    val value: ArscValue
)

internal sealed interface ArscEntryBody {
    data class Simple(val value: ArscValue) : ArscEntryBody
    data class Bag(val parentRef: Int, val entries: List<ArscBagEntry>) : ArscEntryBody
}

internal data class ArscEntry(

    val index: Int,

    val name: String,

    val flags: Int,
    val body: ArscEntryBody
)

internal data class ArscConfig(

    val rawBytes: ByteArray,

    val mcc: Int,
    val mnc: Int,
    val locale: String,
    val sdkVersion: Int,
    val orientation: Int,
    val touchscreen: Int,
    val density: Int,
    val keyboard: Int,
    val navigation: Int,
    val inputFlags: Int,
    val screenWidth: Int,
    val screenHeight: Int,
    val screenLayout: Int,
    val uiMode: Int,
    val smallestScreenWidthDp: Int,
    val screenWidthDp: Int,
    val screenHeightDp: Int,
    val layoutDirection: Int,
    val colorMode: Int
) {
    override fun equals(other: Any?): Boolean {

        if (this === other) return true
        if (other !is ArscConfig) return false
        return rawBytes.contentEquals(other.rawBytes)
    }
    override fun hashCode(): Int = rawBytes.contentHashCode()

    companion object {

        val DEFAULT = ArscConfig(
            rawBytes = ByteArray(0),
            mcc = 0, mnc = 0, locale = "", sdkVersion = 0,
            orientation = 0, touchscreen = 0, density = 0,
            keyboard = 0, navigation = 0, inputFlags = 0,
            screenWidth = 0, screenHeight = 0, screenLayout = 0,
            uiMode = 0, smallestScreenWidthDp = 0,
            screenWidthDp = 0, screenHeightDp = 0,
            layoutDirection = 0, colorMode = 0
        )
    }
}

internal data class ArscType(
    val typeId: Int,
    val config: ArscConfig,

    val entries: List<ArscEntry?>
)

internal data class ArscTypeSpec(
    val typeId: Int,
    val configFlags: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArscTypeSpec) return false
        return typeId == other.typeId && configFlags.contentEquals(other.configFlags)
    }
    override fun hashCode(): Int = 31 * typeId + configFlags.contentHashCode()
}

internal data class ArscPackage(
    val id: Int,
    val name: String,

    val typeNames: List<String>,

    val keyNames: List<String>,
    val typeSpecs: List<ArscTypeSpec>,
    val types: List<ArscType>
)

internal data class ArscResourceTable(

    val valueStringPool: List<String>,
    val packages: List<ArscPackage>
) {

    fun collectReferencedResourceFiles(): Set<String> {
        val out = mutableSetOf<String>()
        for (pkg in packages) {
            for (type in pkg.types) {
                for (entry in type.entries) {
                    if (entry == null) continue
                    when (val body = entry.body) {
                        is ArscEntryBody.Simple -> recordIfFile(out, body.value)
                        is ArscEntryBody.Bag -> body.entries.forEach { recordIfFile(out, it.value) }
                    }
                }
            }
        }
        return out
    }

    private fun recordIfFile(out: MutableSet<String>, value: ArscValue) {
        if (value.dataType != 0x03) return
        val s = valueStringPool.getOrNull(value.data) ?: return
        if (s.startsWith("res/")) out.add(s)
    }
}
