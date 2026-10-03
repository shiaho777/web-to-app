package com.webtoapp.core.golang

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.nio.file.Files
import org.junit.Test

class GoDirectBuilderTest {

    private fun pkg(
        importPath: String,
        name: String = importPath.substringAfterLast('/'),
        goFiles: List<String> = listOf("a.go"),
        sFiles: List<String> = emptyList(),
        imports: List<String> = emptyList(),
        importMap: Map<String, String> = emptyMap(),
        standard: Boolean = true,
        depOnly: Boolean = true,
        dir: String = "/goroot/src/$importPath",
    ) = GoDirectBuilder.GoListPackage(
        importPath = importPath,
        name = name,
        dir = dir,
        goFiles = goFiles,
        sFiles = sFiles,
        imports = imports,
        importMap = importMap,
        deps = emptyList(),
        standard = standard,
        depOnly = depOnly,
    )

    @Test
    fun `parseFlatLine reads real go list shape`() {
        // Mirrors `go list -e -deps -f <template>` output (fields are
        // U+001F-separated; verified against a real 184-package dump).
        val line = listOf(
            "net/http", "http", "/goroot/src/net/http",
            "client.go,server.go", "", "bufio,golang.org/x/net/idna",
            "golang.org/x/net/idna=vendor/golang.org/x/net/idna;",
            "bufio,errors", "true", "true",
            "", "", "", "", "", "", "", "",
            "", "asynctimerchan=1"
        ).joinToString("\u001F")
        val p = GoDirectBuilder.parseFlatLine(line)!!
        assertThat(p.importPath).isEqualTo("net/http")
        assertThat(p.name).isEqualTo("http")
        assertThat(p.goFiles).containsExactly("client.go", "server.go")
        assertThat(p.sFiles).isEmpty()
        assertThat(p.importMap).containsExactly(
            "golang.org/x/net/idna", "vendor/golang.org/x/net/idna"
        )
        assertThat(p.standard).isTrue()
        assertThat(p.depOnly).isTrue()
        assertThat(p.defaultGoDebug).isEqualTo("asynctimerchan=1")
    }

    @Test
    fun `parseFlatLine tolerates short lines`() {
        assertThat(GoDirectBuilder.parseFlatLine("")).isNull()
        assertThat(GoDirectBuilder.parseFlatLine("   ")).isNull()
        val p = GoDirectBuilder.parseFlatLine("hello\u001Fmain\u001F/proj\u001Fmain.go")
        assertThat(p).isNotNull()
    }

    @Test
    fun `parseFlatLine reads module and dep flags`() {
        val fields = listOf(
            "github.com/gin-gonic/gin", "gin", "/mod/gin@v1.9.1",
            "gin.go", "", "fmt,errors", "", "fmt,errors",
            "false", "false", "", "", "", "", "", "", "", "",
            "github.com/gin-gonic/gin|v1.9.1", ""
        ).joinToString("\u001F")
        val p = GoDirectBuilder.parseFlatLine(fields)!!
        assertThat(p.standard).isFalse()
        assertThat(p.depOnly).isFalse()
        assertThat(p.modulePath).isEqualTo("github.com/gin-gonic/gin")
        assertThat(p.moduleVersion).isEqualTo("v1.9.1")
    }

    @Test
    fun `topoSort orders deps first and skips unsafe`() {
        val c = pkg("c")
        val b = pkg("b", imports = listOf("c"))
        val a = pkg("a", imports = listOf("b", "unsafe"))
        val order = GoDirectBuilder.topoSort(listOf(a, b, c)).map { it.importPath }
        assertThat(order).containsExactly("c", "b", "a").inOrder()
    }

    @Test
    fun `renderImportCfg emits importmap lines and skips unsafe`() {
        val p = pkg(
            "net",
            imports = listOf("golang.org/x/net/idna", "errors", "unsafe"),
            importMap = mapOf("golang.org/x/net/idna" to "vendor/golang.org/x/net/idna"),
        )
        val archives = mapOf(
            "vendor/golang.org/x/net/idna" to File("/w/idna/_pkg_.a"),
            "errors" to File("/w/errors/_pkg_.a"),
        )
        val cfg = GoDirectBuilder.renderImportCfg(p, archives)
        assertThat(cfg).contains("importmap golang.org/x/net/idna=vendor/golang.org/x/net/idna")
        assertThat(cfg).contains("packagefile vendor/golang.org/x/net/idna=/w/idna/_pkg_.a")
        assertThat(cfg).contains("packagefile errors=/w/errors/_pkg_.a")
        assertThat(cfg).doesNotContain("unsafe")
    }

    @Test
    fun `no-complete set matches cmd-go forward-declaration list`() {
        // Mirrors cmd/go/internal/work/gc.go (verified: full `go build -n`
        // recipe for this toolchain agrees on all 182 packages).
        assertThat(GoDirectBuilder.NO_COMPLETE_STD).containsAtLeast(
            "bytes", "internal/poll", "net", "os",
            "runtime/metrics", "runtime/pprof", "runtime/trace",
            "sync", "syscall", "time"
        )
    }

    @Test
    fun `compileArgs follow the captured recipe`() {
        val dir = Files.createTempDirectory("wta-gotest").toFile()
        try {
            val main = pkg("hello", name = "main", standard = false, depOnly = false)
            val args = GoDirectBuilder.compileArgs(
                pkg = main,
                outArchive = File(dir, "_pkg_.a"),
                importCfg = File(dir, "importcfg"),
                workDir = dir,
                goVersion = "go1.26.4",
                lang = "go1.26",
                pflag = "main",
                complete = true,
                symabis = null,
                asmHdr = null,
            )
            assertThat(args).containsAtLeast(
                "-p", "main", "-lang=go1.26", "-goversion", "go1.26.4",
                "-shared", "-nolocalimports", "-pack", "-complete"
            )
            assertThat(args).doesNotContain("-std")

            val std = pkg("fmt")
            val stdArgs = GoDirectBuilder.compileArgs(
                pkg = std,
                outArchive = File(dir, "s.a"),
                importCfg = File(dir, "importcfg"),
                workDir = dir,
                goVersion = "go1.26.4",
                lang = "go1.26",
                pflag = "fmt",
                complete = false,
                symabis = File(dir, "symabis"),
                asmHdr = File(dir, "go_asm.h"),
            )
            assertThat(stdArgs).containsAtLeast("-std", "-symabis", "-asmhdr")
            assertThat(stdArgs).doesNotContain("-complete")
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `renderModinfo synthesizes the link directive`() {
        val modinfo = GoDirectBuilder.renderModinfo(
            mainImportPath = "hello",
            modulePath = "hello",
            moduleVersion = null,
            goDebug = "a=1,b=0",
            goos = "android",
            goarch = "arm64",
        )
        // Single physical line with literal backslash escapes (linker syntax).
        assertThat(modinfo).contains("modinfo \"path\\thello\\nmod\\thello\\t(devel)\\t\\n")
        assertThat(modinfo).contains("\\nbuild\\tDefaultGODEBUG=a=1,b=0\\n")
        assertThat(modinfo).contains("\\nbuild\\tGOOS=android\\n")
        assertThat(modinfo).contains("\\nbuild\\tGOARM64=v8.0\\n")
        assertThat(modinfo.trimEnd()).endsWith("\\n\"")
        assertThat(modinfo.lines()).hasSize(1)
    }

    @Test
    fun `refuseUnsupported rejects cgo and embed`() {
        val cgo = pkg("x", goFiles = listOf("x.go")).copy(cgoFiles = listOf("x.go"))
        assertThat(GoDirectBuilder.refuseUnsupported(listOf(cgo))).contains("cgo")
        val emb = pkg("y").copy(embedFiles = listOf("data.bin"))
        assertThat(GoDirectBuilder.refuseUnsupported(listOf(emb))).contains("embed")
        val ok = pkg("z")
        assertThat(GoDirectBuilder.refuseUnsupported(listOf(ok))).isNull()
    }

    @Test
    fun `fingerprint is stable and content-sensitive`() {
        val dir = Files.createTempDirectory("wta-gofp").toFile()
        try {
            val src = File(dir, "a.go").also { it.writeText("package p\n") }
            val p = pkg("p", dir = dir.absolutePath)
            val f1 = GoDirectBuilder.fingerprint(p, "go1.26.4", "go1.26", "android", "arm64", emptyMap())
            val f2 = GoDirectBuilder.fingerprint(p, "go1.26.4", "go1.26", "android", "arm64", emptyMap())
            assertThat(f1).isEqualTo(f2)
            src.writeText("package p\n// changed\n")
            val f3 = GoDirectBuilder.fingerprint(p, "go1.26.4", "go1.26", "android", "arm64", emptyMap())
            assertThat(f3).isNotEqualTo(f1)
        } finally {
            dir.deleteRecursively()
        }
    }

    @Test
    fun `findToolDir prefers linux_arm64 layout`() {
        val root = Files.createTempDirectory("wta-gotool").toFile()
        try {
            val canonical = File(root, "pkg/tool/linux_arm64").also { it.mkdirs() }
            File(canonical, "compile").writeText("x")
            assertThat(GoDirectBuilder.findToolDir(root)).isEqualTo(canonical)
        } finally {
            root.deleteRecursively()
        }
    }
}
