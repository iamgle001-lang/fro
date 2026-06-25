package app.morphe.patches.fromm.misc.session

import app.morphe.patcher.fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.fromm.util.InstructionHelper
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

private val shouldReloadFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("Z")
    strings("shouldReloadApp")
    opcodes(
        Opcode.IGET_OBJECT,
        Opcode.CONST_STRING,
        Opcode.CONST_4,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT,
        Opcode.RETURN,
    )
}

@Suppress("unused")
val disableAppReloadPatch = bytecodePatch(
    name = "Disable app reload prompt",
    description = "Always returns false for shouldReloadApp, preventing forced app restarts.",
) {
    compatibleWith("com.knowmerce.fromm.fan")

    execute {
        val method = shouldReloadFingerprint.match().method
        InstructionHelper.replaceInstructions(
            method,
            0,
            """
            const/4 v0, 0x0
            return v0
            """.trimIndent(),
        )
    }
}
