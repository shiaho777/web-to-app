import java.io.File

private fun readLeShortAt(src: ByteArray, off: Int): Int =
    ((src[off].toInt() and 0xff)) or ((src[off + 1].toInt() and 0xff) shl 8)

private fun readLeIntAt(src: ByteArray, off: Int): Int =
    ((src[off].toInt() and 0xff)) or
        ((src[off + 1].toInt() and 0xff) shl 8) or
        ((src[off + 2].toInt() and 0xff) shl 16) or
        ((src[off + 3].toInt() and 0xff) shl 24)

private fun readLeLongAt(src: ByteArray, off: Int): Long {
    var v = 0L
    for (i in 0 until 8) v = v or ((src[off + i].toLong() and 0xffL) shl (8 * i))
    return v
}

private fun writeLeIntAt(dst: ByteArray, off: Int, value: Int) {
    for (i in 0 until 4) dst[off + i] = ((value ushr (8 * i)) and 0xff).toByte()
}

private fun writeLeLongAt(dst: ByteArray, off: Int, value: Long) {
    for (i in 0 until 8) dst[off + i] = ((value ushr (8 * i)) and 0xffL).toByte()
}

private data class PtLoad(
    val phdrOffset: Long,
    val pOffset: Long,
    val pVaddr: Long,
    val pFilesz: Long,
    val pMemsz: Long,
)

private data class Shdr(
    val index: Int,
    val phdrOffset: Long,
    val shName: Int,
    val shType: Int,
    val shFlags: Long,
    val shAddr: Long,
    val shOffset: Long,
    val shSize: Long,
    val shLink: Int,
    val shInfo: Int,
    val shAddralign: Long,
    val shEntsize: Long,
)

fun patchElfPageSize(input: File, output: File, pageSize: Long): Boolean {
    val src = input.readBytes()
    if (src.size < 64) return false
    if (src[0] != 0x7f.toByte() || src[1] != 'E'.code.toByte() ||
        src[2] != 'L'.code.toByte() || src[3] != 'F'.code.toByte()) return false
    if (src[4].toInt() != 2 || src[5].toInt() != 1) return false

    val ePhoff = readLeLongAt(src, 32)
    val eShoff = readLeLongAt(src, 40)
    val ePhentsize = readLeShortAt(src, 54)
    val ePhnum = readLeShortAt(src, 56)
    val eShentsize = readLeShortAt(src, 58)
    val eShnum = readLeShortAt(src, 60)

    val phdrs = ArrayList<LongArray>(ePhnum)
    for (i in 0 until ePhnum) {
        val base = ePhoff + i.toLong() * ePhentsize
        phdrs += longArrayOf(
            base,
            readLeIntAt(src, base.toInt() + 0).toLong() and 0xffffffffL,
            readLeIntAt(src, base.toInt() + 4).toLong() and 0xffffffffL,
            readLeLongAt(src, base.toInt() + 8),
            readLeLongAt(src, base.toInt() + 16),
            readLeLongAt(src, base.toInt() + 24),
            readLeLongAt(src, base.toInt() + 32),
            readLeLongAt(src, base.toInt() + 40),
            readLeLongAt(src, base.toInt() + 48),
        )
    }

    val ptLoads = phdrs.filter { it[1] == 1L }.map {
        PtLoad(
            phdrOffset = it[0],
            pOffset = it[3],
            pVaddr = it[4],
            pFilesz = it[6],
            pMemsz = it[7],
        )
    }.sortedBy { it.pVaddr }

    val shdrs = ArrayList<Shdr>(eShnum)
    for (i in 0 until eShnum) {
        val base = eShoff + i.toLong() * eShentsize
        shdrs += Shdr(
            index = i,
            phdrOffset = base,
            shName = readLeIntAt(src, base.toInt() + 0),
            shType = readLeIntAt(src, base.toInt() + 4),
            shFlags = readLeLongAt(src, base.toInt() + 8),
            shAddr = readLeLongAt(src, base.toInt() + 16),
            shOffset = readLeLongAt(src, base.toInt() + 24),
            shSize = readLeLongAt(src, base.toInt() + 32),
            shLink = readLeIntAt(src, base.toInt() + 40),
            shInfo = readLeIntAt(src, base.toInt() + 44),
            shAddralign = readLeLongAt(src, base.toInt() + 48),
            shEntsize = readLeLongAt(src, base.toInt() + 56),
        )
    }

    val mask = pageSize - 1
    val newOffsets = HashMap<Long, Long>()
    var prevEnd = 0L
    for (s in ptLoads) {
        if (s.pOffset == 0L && s.pVaddr == 0L) {
            newOffsets[s.phdrOffset] = 0L
            if (s.pFilesz > prevEnd) prevEnd = s.pFilesz
            continue
        }
        val targetMod = s.pVaddr and mask
        val rounded = (prevEnd + mask) and mask.inv()
        var newOff = rounded + targetMod
        if (newOff < prevEnd) newOff += pageSize
        newOffsets[s.phdrOffset] = newOff
        prevEnd = newOff + s.pFilesz
    }

    val newShoff = (prevEnd + mask) and mask.inv()
    val sectHdrTableSize = eShentsize.toLong() * eShnum

    val nonLoadSections = shdrs.filter {
        it.shType != 0 && it.shSize > 0L && (it.shFlags and 0x2L) == 0L
    }
    var cursor = newShoff + sectHdrTableSize
    val newShOffsets = HashMap<Int, Long>()
    for (sh in nonLoadSections) {
        val align = if (sh.shAddralign < 1L) 1L else sh.shAddralign
        cursor = ((cursor + align - 1L) / align) * align
        newShOffsets[sh.index] = cursor
        cursor += sh.shSize
    }

    val out = ByteArray(cursor.toInt())
    System.arraycopy(src, 0, out, 0, 64)
    System.arraycopy(src, ePhoff.toInt(), out, ePhoff.toInt(), (ePhnum.toLong() * ePhentsize).toInt())
    for (s in ptLoads) {
        val newOff = newOffsets[s.phdrOffset]!!
        if (s.pFilesz > 0L) {
            System.arraycopy(src, s.pOffset.toInt(), out, newOff.toInt(), s.pFilesz.toInt())
        }
        writeLeLongAt(out, (s.phdrOffset + 8).toInt(), newOff)
        writeLeLongAt(out, (s.phdrOffset + 48).toInt(), pageSize)
    }
    for (ph in phdrs) {
        val pType = ph[1]
        if (pType == 1L) continue
        val pOff = ph[3]
        for (s in ptLoads) {
            if (pOff in s.pOffset until (s.pOffset + s.pFilesz)) {
                val delta = newOffsets[s.phdrOffset]!! - s.pOffset
                writeLeLongAt(out, (ph[0] + 8).toInt(), pOff + delta)
                break
            }
        }
    }
    for (sh in shdrs) {
        var newOff = sh.shOffset
        if (sh.shType == 0) newOff = 0L
        else if ((sh.shFlags and 0x2L) != 0L) {
            for (s in ptLoads) {
                if (sh.shOffset in s.pOffset until (s.pOffset + s.pFilesz)) {
                    newOff = sh.shOffset + (newOffsets[s.phdrOffset]!! - s.pOffset)
                    break
                }
            }
        } else if (newShOffsets.containsKey(sh.index)) {
            newOff = newShOffsets[sh.index]!!
        }
        val dst = (newShoff + sh.index.toLong() * eShentsize).toInt()
        writeLeIntAt(out, dst + 0, sh.shName)
        writeLeIntAt(out, dst + 4, sh.shType)
        writeLeLongAt(out, dst + 8, sh.shFlags)
        writeLeLongAt(out, dst + 16, sh.shAddr)
        writeLeLongAt(out, dst + 24, newOff)
        writeLeLongAt(out, dst + 32, sh.shSize)
        writeLeIntAt(out, dst + 40, sh.shLink)
        writeLeIntAt(out, dst + 44, sh.shInfo)
        writeLeLongAt(out, dst + 48, sh.shAddralign)
        writeLeLongAt(out, dst + 56, sh.shEntsize)
    }
    for (sh in nonLoadSections) {
        val newOff = newShOffsets[sh.index]!!
        if (sh.shSize > 0L) {
            System.arraycopy(src, sh.shOffset.toInt(), out, newOff.toInt(), sh.shSize.toInt())
        }
    }
    writeLeLongAt(out, 40, newShoff)
    output.writeBytes(out)
    println("Patched ${input.name} (${src.size} bytes) -> ${output.name} (${out.size} bytes)")
    return true
}

fun main(args: Array<String>) {
    if (args.size < 2) {
        println("usage: PatchElfStandalone <input> <output> [pageSize=16384]")
        return
    }
    val pageSize = if (args.size >= 3) args[2].toLong() else 16384L
    val ok = patchElfPageSize(File(args[0]), File(args[1]), pageSize)
    if (!ok) {
        System.err.println("FAILED")
        kotlin.system.exitProcess(1)
    }
}
