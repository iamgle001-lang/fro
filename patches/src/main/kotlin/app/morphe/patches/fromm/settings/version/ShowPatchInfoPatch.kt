package app.morphe.patches.fromm.settings.version

import app.morphe.patcher.fingerprint
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.fromm.PATCH_VERSION
import app.morphe.patches.fromm.util.InstructionHelper
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction22c
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

// ── Fingerprint ───────────────────────────────────────────────────────────
// Targets the VersionScreen composable method that reads both currentVersion
// and latestVersion from the VersionState (o.smali) — identified by having
// at least 2 iget-object references to each field.
private val versionScreenFp = fingerprint {
    custom { method, _ ->
        val instrs = method.implementation?.instructions ?: return@custom false
        val refsA = instrs.count { instr ->
            instr is ReferenceInstruction &&
                instr.reference.toString()
                    .contains("settings/screen/version/o;->a:Ljava/lang/String;")
        }
        val refsB = instrs.count { instr ->
            instr is ReferenceInstruction &&
                instr.reference.toString()
                    .contains("settings/screen/version/o;->b:Ljava/lang/String;")
        }
        refsA >= 2 && refsB >= 2
    }
}

// ── Patch ─────────────────────────────────────────────────────────────────

@Suppress("unused")
val showPatchInfoPatch = bytecodePatch(
    name = "패치 정보 표시",
    description = "설정 > 버전 정보 화면에 적용된 모르페 패치 버전을 표시합니다.",
) {
    compatibleWith("com.knowmerce.fromm.fan")

    execute {
        val method = versionScreenFp.match().method
        val instrs = InstructionHelper.getInstructions(method)

        // Find the first iget-object that reads currentVersion (o;->a:String).
        // This is the register later passed as the first argument to the
        // VersionScreen composable function — i.e. the displayed version text.
        val idx = instrs.indexOfFirst { instr ->
            instr is ReferenceInstruction &&
                instr.reference.toString()
                    .contains("settings/screen/version/o;->a:Ljava/lang/String;")
        }
        if (idx == -1) throw PatchException("currentVersion iget not found")

        val destReg = (instrs[idx] as? BuilderInstruction22c)?.registerA
            ?: throw PatchException("unexpected iget instruction type")
        // The next register (destReg + 1) is safe as a temporary because it is
        // reassigned to the VersionState object immediately after this block.
        val tempReg = destReg + 1

        // Append patch info string to the currentVersion register.
        InstructionHelper.addInstructions(
            method,
            idx + 1,
            """
            const-string v$tempReg, "\n모르페 패치 v$PATCH_VERSION 적용됨"
            invoke-virtual {v$destReg, v$tempReg}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
            move-result-object v$destReg
            """.trimIndent(),
        )
    }
}
