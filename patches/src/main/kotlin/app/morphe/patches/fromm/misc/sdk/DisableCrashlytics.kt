package app.morphe.patches.fromm.misc.sdk

import app.morphe.patcher.fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.fromm.util.InstructionHelper
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.BuilderInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

private val crashlyticsFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/knowmerce/fromm/fan/FanApplication;" &&
            method.implementation?.instructions?.any { instr ->
                instr is ReferenceInstruction &&
                    instr.reference.toString().contains("FirebaseCrashlytics;->recordException")
            } == true
    }
}

@Suppress("unused")
val disableCrashlyticsPatch = bytecodePatch(
    name = "Disable Crashlytics",
    description = "Prevents crash reports from being sent to Firebase Crashlytics.",
) {
    compatibleWith("com.knowmerce.fromm.fan")

    execute {
        val method = crashlyticsFingerprint.match().method
        val instructions = InstructionHelper.getInstructions(method)

        instructions.forEachIndexed { idx, instr: BuilderInstruction ->
            if (
                instr.opcode == Opcode.INVOKE_VIRTUAL &&
                (instr as? ReferenceInstruction)?.reference?.toString()?.contains("FirebaseCrashlytics;->recordException") == true
            ) {
                InstructionHelper.replaceInstruction(method, idx, "nop")
            }
        }
    }
}
