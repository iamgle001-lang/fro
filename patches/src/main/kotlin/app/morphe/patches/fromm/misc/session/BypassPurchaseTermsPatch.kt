package app.morphe.patches.fromm.misc.session

import app.morphe.patcher.fingerprint
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.fromm.util.InstructionHelper
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.BuilderInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

private val purchaseTermsFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/knowmerce/fromm/domain/fan/usecases/subscription/b;" &&
            method.implementation?.instructions?.any { instr ->
                instr is ReferenceInstruction &&
                    instr.reference.toString().contains("CheckIfPurchaseTermsAgreedUseCase\$invoke\$1")
            } == true
    }
}

@Suppress("unused")
val bypassPurchaseTermsPatch = bytecodePatch(
    name = "Bypass purchase terms agreement",
    description = "Skips the purchase terms agreement screen by always reporting it as agreed.",
) {
    compatibleWith("com.knowmerce.fromm.fan")

    execute {
        val method = purchaseTermsFingerprint.match().method
        val instructions = InstructionHelper.getInstructions(method)

        val suspendIdx = instructions.indexOfFirst { instr: BuilderInstruction ->
            instr.opcode == Opcode.SGET_OBJECT &&
                (instr as? ReferenceInstruction)?.reference?.toString()?.contains("CoroutineSingletons") == true
        }
        if (suspendIdx == -1) throw PatchException("Coroutine suspended object not found")

        InstructionHelper.addInstructions(
            method,
            0,
            """
            const/4 v0, 0x1
            invoke-static {v0}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
            move-result-object v0
            return-object v0
            """.trimIndent(),
        )
    }
}
