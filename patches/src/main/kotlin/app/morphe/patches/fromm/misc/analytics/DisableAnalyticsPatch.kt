package app.morphe.patches.fromm.misc.analytics

import app.morphe.patcher.fingerprint
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.fromm.util.InstructionHelper
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.BuilderInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private val analyticsInitFingerprint = fingerprint {
    strings("D6019753-6595-4CF9-966C-0CE3B4F3003F")
    opcodes(
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.CONST_STRING,
    )
}

@Suppress("unused")
val disableAnalyticsPatch = bytecodePatch(
    name = "Disable analytics",
    description = "Disables Mixpanel event tracking and Firebase Analytics collection.",
) {
    compatibleWith("com.knowmerce.fromm.fan")

    execute {
        val method = analyticsInitFingerprint.match().method
        val instructions = InstructionHelper.getInstructions(method)

        val firebaseInitIdx = instructions.indexOfFirst { instr: BuilderInstruction ->
            instr.opcode == Opcode.INVOKE_STATIC &&
                (instr as? ReferenceInstruction)?.reference?.toString()?.contains("AnalyticsKt;->getAnalytics") == true
        }
        if (firebaseInitIdx == -1) throw PatchException("Firebase Analytics init not found")

        val mixpanelFieldIdx = instructions.indexOfFirst { instr: BuilderInstruction ->
            instr.opcode == Opcode.IGET_OBJECT &&
                (instr as? ReferenceInstruction)?.reference?.toString()?.contains("FanApplication;->G:LB8/b;") == true
        }

        InstructionHelper.replaceInstruction(method, firebaseInitIdx - 1, "nop")
        InstructionHelper.replaceInstruction(method, firebaseInitIdx, "nop")

        if (mixpanelFieldIdx != -1) {
            val register = (instructions[mixpanelFieldIdx] as? TwoRegisterInstruction)?.registerA ?: 1
            InstructionHelper.replaceInstruction(method, mixpanelFieldIdx, "const/4 v$register, 0x0")
        }
    }
}
