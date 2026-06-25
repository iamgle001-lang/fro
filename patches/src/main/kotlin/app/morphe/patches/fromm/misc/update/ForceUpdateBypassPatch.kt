package app.morphe.patches.fromm.misc.update

import app.morphe.patcher.fingerprint
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.fromm.util.InstructionHelper
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.BuilderInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

private val checkAvailabilityFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type.startsWith("Lcom/knowmerce/fromm/domain/fan/usecases/common/") &&
            method.implementation?.instructions?.any { instr ->
                instr is ReferenceInstruction &&
                    instr.reference.toString().contains("getFanForceUpdateVersion")
            } == true
    }
}

@Suppress("unused")
val forceUpdateBypassPatch = bytecodePatch(
    name = "Force update bypass",
    description = "Skips mandatory version update check by always returning Success.",
) {
    compatibleWith("com.knowmerce.fromm.fan")

    execute {
        val method = checkAvailabilityFingerprint.match().method
        val instructions = InstructionHelper.getInstructions(method)

        val forceReturnIdx = instructions.indexOfFirst { instr: BuilderInstruction ->
            instr.opcode == Opcode.SGET_OBJECT &&
                (instr as? ReferenceInstruction)?.reference?.toString()?.contains("usecases/common/f;->a:") == true
        }
        if (forceReturnIdx == -1) throw PatchException("ForceUpdateRequired sget not found")

        val successIdx = instructions.indexOfLast { instr: BuilderInstruction ->
            instr.opcode == Opcode.SGET_OBJECT &&
                (instr as? ReferenceInstruction)?.reference?.toString()?.contains("usecases/common/g;->a:") == true
        }
        if (successIdx == -1) throw PatchException("Success sget not found")

        InstructionHelper.replaceInstruction(method, forceReturnIdx, "nop")
        InstructionHelper.replaceInstruction(method, forceReturnIdx + 1, "nop")
        val jumpOffset = successIdx - forceReturnIdx
        InstructionHelper.addInstruction(method, forceReturnIdx, "goto/16 +$jumpOffset")
    }
}
