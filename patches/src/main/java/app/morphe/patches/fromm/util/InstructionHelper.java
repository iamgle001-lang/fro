package app.morphe.patches.fromm.util;

import app.morphe.patcher.extensions.InstructionExtensions;
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod;
import com.android.tools.smali.dexlib2.builder.BuilderInstruction;
import java.util.List;

/**
 * Java wrapper for InstructionExtensions to avoid Kotlin context parameter issues.
 * Calls InstructionExtensions.INSTANCE methods directly at the JVM level.
 */
public final class InstructionHelper {
    private InstructionHelper() {}

    public static List<BuilderInstruction> getInstructions(MutableMethod method) {
        return InstructionExtensions.INSTANCE.getInstructions(method);
    }

    public static void replaceInstruction(MutableMethod method, int index, String smali) {
        InstructionExtensions.INSTANCE.replaceInstruction(method, index, smali);
    }

    public static void replaceInstructions(MutableMethod method, int index, String smali) {
        InstructionExtensions.INSTANCE.replaceInstructions(method, index, smali);
    }

    public static void addInstruction(MutableMethod method, int index, String smali) {
        InstructionExtensions.INSTANCE.addInstruction(method, index, smali);
    }

    public static void addInstructions(MutableMethod method, int index, String smali) {
        InstructionExtensions.INSTANCE.addInstructions(method, index, smali);
    }
}
