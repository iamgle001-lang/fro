package app.morphe.patches.fromm.chat.unread

import app.morphe.patcher.fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.fromm.util.InstructionHelper

// Finds the helper that formats an integer count as a display string,
// capping at "99+" when the count exceeds 99.
// Constraint: returns String, takes exactly one integral parameter (I or J).
private val unreadCountCapFingerprint = fingerprint {
    returns("Ljava/lang/String;")
    strings("99+")
    custom { method, _ ->
        method.parameterTypes.size == 1 &&
            (method.parameterTypes[0] == "I" || method.parameterTypes[0] == "J")
    }
}

@Suppress("unused")
val showActualMessageCountPatch = bytecodePatch(
    name = "Show actual message count",
    description = "Shows the real unread/message count instead of capping the display at 99+.",
) {
    compatibleWith("com.knowmerce.fromm.fan")

    execute {
        val method = unreadCountCapFingerprint.match().method

        // ACC_STATIC = 0x0008.  For static methods the first explicit parameter
        // is p0; for instance methods p0 is `this` and the count is p1.
        val isStatic = (method.accessFlags and 0x0008) != 0
        val paramType = method.parameterTypes[0]

        val smali = if (paramType == "J") {
            // Long parameter occupies two registers (wide pair).
            val regs = if (isStatic) "p0, p1" else "p1, p2"
            """
            invoke-static {$regs}, Ljava/lang/String;->valueOf(J)Ljava/lang/String;
            move-result-object v0
            return-object v0
            """.trimIndent()
        } else {
            // Int parameter — most common case.
            val reg = if (isStatic) "p0" else "p1"
            """
            invoke-static {$reg}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;
            move-result-object v0
            return-object v0
            """.trimIndent()
        }

        InstructionHelper.replaceInstructions(method, 0, smali)
    }
}
