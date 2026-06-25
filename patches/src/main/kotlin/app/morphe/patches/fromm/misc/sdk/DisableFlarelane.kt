package app.morphe.patches.fromm.misc.sdk

import app.morphe.patcher.fingerprint
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.fromm.util.InstructionHelper
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.BuilderInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

private val flarelaneInitFingerprint = fingerprint {
    opcodes(Opcode.INVOKE_STATIC, Opcode.INVOKE_STATIC)
    custom { method, _ ->
        method.implementation?.instructions?.any { instr ->
            instr is ReferenceInstruction &&
                instr.reference.toString().contains("com/flarelane/k;->b(")
        } == true
    }
}

@Suppress("unused")
val disableFlarelane = bytecodePatch(
    name = "Disable Flarelane push",
    description = "Disables Flarelane push marketing SDK initialization.",
) {
    compatibleWith("com.knowmerce.fromm.fan")

    execute {
        val method = flarelaneInitFingerprint.match().method
        val instructions = InstructionHelper.getInstructions(method)

        val flarelaneIdx = instructions.indexOfFirst { instr: BuilderInstruction ->
            instr.opcode == Opcode.INVOKE_STATIC &&
                (instr as? ReferenceInstruction)?.reference?.toString()?.contains("com/flarelane/k;->b(") == true
        }
        if (flarelaneIdx == -1) throw PatchException("Flarelane init call not found")

        InstructionHelper.replaceInstruction(method, flarelaneIdx, "nop")
    }
}
