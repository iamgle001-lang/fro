package app.morphe.patches.fromm.misc.ssl

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

@Suppress("unused")
val sslPinningBypassPatch = resourcePatch(
    name = "SSL pinning bypass",
    description = "Adds a network security config to bypass SSL certificate pinning.",
) {
    compatibleWith("com.knowmerce.fromm.fan")

    execute {
        // 1. res/xml/network_security_config.xml 생성
        get("res/xml/network_security_config.xml").outputStream().use { out ->
            out.write(
                """<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system" />
            <certificates overridePins="true" src="user" />
        </trust-anchors>
    </base-config>
    <debug-overrides>
        <trust-anchors>
            <certificates src="system" />
            <certificates overridePins="true" src="user" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>""".toByteArray()
            )
        }

        // 2. AndroidManifest.xml — <application> 태그에 networkSecurityConfig 추가
        document("AndroidManifest.xml").use { doc ->
            val app = doc.getElementsByTagName("application").item(0) as Element
            app.setAttribute("android:networkSecurityConfig", "@xml/network_security_config")

            // Sentry 비활성화 (크래시 리포트 차단)
            listOf(
                "io.sentry.enabled" to "false",
                "io.sentry.dsn" to "",
            ).forEach { (name, value) ->
                val meta = doc.createElement("meta-data")
                meta.setAttribute("android:name", name)
                meta.setAttribute("android:value", value)
                app.appendChild(meta)
            }
        }
    }
}
