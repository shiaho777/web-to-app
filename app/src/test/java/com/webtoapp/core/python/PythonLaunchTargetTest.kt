package com.webtoapp.core.python

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PythonLaunchTargetTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun `module name converts nested entry paths to dotted modules`() {
        assertThat(PythonLaunchTarget.moduleNameFor("main.py")).isEqualTo("main")
        assertThat(PythonLaunchTarget.moduleNameFor("src/main.py")).isEqualTo("src.main")
        assertThat(PythonLaunchTarget.moduleNameFor("src\\api\\server.py")).isEqualTo("src.api.server")
        assertThat(PythonLaunchTarget.moduleNameFor("./app.py")).isEqualTo("app")
    }

    @Test
    fun `detects the assigned FastAPI object name`() {
        val dir = tmp.newFolder("proj")
        File(dir, "main.py").writeText(
            """
            from fastapi import FastAPI
            application = FastAPI()
            """.trimIndent()
        )
        val target = PythonLaunchTarget.detectAsgiTarget(dir, "main.py")
        assertThat(target.spec).isEqualTo("main:application")
        assertThat(target.factory).isFalse()
    }

    @Test
    fun `detects Starlette and Quart assignments and nested modules`() {
        val dir = tmp.newFolder("proj")
        File(dir, "src").mkdirs()
        File(dir, "src/api.py").writeText("from starlette.applications import Starlette\napi = Starlette()\n")
        val target = PythonLaunchTarget.detectAsgiTarget(dir, "src/api.py")
        assertThat(target.spec).isEqualTo("src.api:api")
    }

    @Test
    fun `detects a create_app factory`() {
        val dir = tmp.newFolder("proj")
        File(dir, "main.py").writeText(
            """
            from fastapi import FastAPI
            def create_app() -> FastAPI:
                return FastAPI()
            """.trimIndent()
        )
        val target = PythonLaunchTarget.detectAsgiTarget(dir, "main.py")
        assertThat(target.spec).isEqualTo("main:create_app")
        assertThat(target.factory).isTrue()
    }

    @Test
    fun `uses an explicit dotted ASGI factory from the saved configuration`() {
        val dir = tmp.newFolder("proj")
        File(dir, "main.py").writeText("api = FastAPI()\n")
        val target = PythonLaunchTarget.detectAsgiTarget(dir, "main.py", "web.entry:routes.create()")
        assertThat(target.spec).isEqualTo("web.entry:routes.create")
        assertThat(target.factory).isTrue()
    }

    @Test
    fun `an old default app target does not hide the project factory`() {
        val dir = tmp.newFolder("proj")
        File(dir, "app.py").writeText("def create_app():\n    return FastAPI()\n")
        assertThat(PythonLaunchTarget.detectAsgiTarget(dir, "app.py", "app:app").configValue)
            .isEqualTo("app:create_app()")
    }

    @Test
    fun `falls back to app when nothing is detected`() {
        val dir = tmp.newFolder("proj")
        File(dir, "main.py").writeText("print('hello')\n")
        val target = PythonLaunchTarget.detectAsgiTarget(dir, "main.py")
        assertThat(target.spec).isEqualTo("main:app")
        assertThat(target.factory).isFalse()

        val missing = PythonLaunchTarget.detectAsgiTarget(dir, "absent.py")
        assertThat(missing.spec).isEqualTo("absent:app")
    }

    @Test
    fun `reads DJANGO_SETTINGS_MODULE from manage py setdefault`() {
        val dir = tmp.newFolder("proj")
        File(dir, "manage.py").writeText(
            """
            import os
            os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings.base')
            """.trimIndent()
        )
        assertThat(PythonLaunchTarget.djangoSettingsFromManagePy(dir)).isEqualTo("config.settings.base")
    }

    @Test
    fun `manage py without settings definition returns null`() {
        val dir = tmp.newFolder("proj")
        File(dir, "manage.py").writeText("import sys\nprint(sys.argv)\n")
        assertThat(PythonLaunchTarget.djangoSettingsFromManagePy(dir)).isNull()
        assertThat(PythonLaunchTarget.djangoSettingsFromManagePy(tmp.newFolder("empty"))).isNull()
    }

    @Test
    fun `django settings falls back to first level settings dir only when manage py is silent`() {
        val dir = tmp.newFolder("proj")
        File(dir, "manage.py").writeText("import sys\n")
        File(dir, "mysite").mkdirs()
        File(dir, "mysite/settings.py").writeText("DEBUG = False\n")
        assertThat(PythonLaunchTarget.detectDjangoSettings(dir)).isEqualTo("mysite.settings")

        val bare = tmp.newFolder("bare")
        assertThat(PythonLaunchTarget.detectDjangoSettings(bare)).isNull()
    }
}
