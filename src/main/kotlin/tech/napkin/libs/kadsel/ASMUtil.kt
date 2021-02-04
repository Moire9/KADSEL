/*
 * Copyright (c) 2021 SirNapkin1334 / Napkin Technologies
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

@file:JvmName("ASMUtil")
package tech.napkin.libs.kadsel

import codes.som.anthony.koffee.BlockAssembly
import codes.som.anthony.koffee.assembleBlock
import codes.som.anthony.koffee.insns.InstructionAssembly
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import java.util.regex.Pattern

/**
 * A whole bunch of miscellaneous functions that might come in handy.
 *
 * A lot of the ones at the beginning are or were used internally, the later ones
 * were mostly written by me whenever I needed a function to do that, and the last
 * couple were written by jack for ASMWorkspace, and I modified them here.
 */

/** Matches a valid reference type (e.g. `Ljava/lang/Object`). */
val reference =  Pattern.compile("L[^.;\\[/<>:]?[^.;\\[<>:]*?[^.;\\[/<>:];")!!

/** All disallowed characters in names. */
val disallowedCharacters = charArrayOf('.', ';', '[', '/', '<', '>', ':')

/** Regex to match a full method description. */
val fullDescriptor = Pattern.compile("""(?:(?<parent>[^.;\[/<>:]?[^.;\[<>:]*?[^.;\[/<>:])\.)?(?<name>[^.;\[/<>:]+)\((?:(?<args>\[*(?:[ZBCSIJFD]|L[^.;\[/<>:]?[^.;\[<>:]*?[^.;\[/<>:];))*)\)(?<return>\[*(?:[ZBCSIJFDV]|L[^.;\[/<>:]?[^.;\[<>:]*?[^.;\[/<>:];))""")!!


/**
 * Takes a method descriptor and returns the arguments and return type.
 * Example:```
 * "(IIILjava/lang/Object;)V".descriptorToArgsAndReturn() ==
 * ["I", "I", "I", "Ljava/lang/Object;"] to "V"```
 */
fun String.descriptorToArgsAndReturn(): Pair<Array<String>, String> {
	val endingArgIndex = indexOf(')')
	return substring(1, endingArgIndex).toRefArray() to if (get(endingArgIndex + 1) == 'L') {
		subSequence(endingArgIndex + 1..length).toString()
	} else {
		get(endingArgIndex + 1).toString()
	}
}

/**
 * Takes a JVM argument String and converts it to an array with each element that
 * type.
 *
 * Example:```"IIZZLjava/lang/Object;Ljava/lang/String;[I[[D".toRefArray() ==
 * ["I", "I", "Z", "Z", "Ljava/lang/Object;", "Ljava/lang/String;", "[I", "[[D"]
 * ```
 */
fun CharSequence.toRefArray(): Array<String> {
	if (isEmpty()) return arrayOf()
	var path: String? = null
	val theArgs = mutableListOf<String>()
	forEach {
		if (it == 'L' && path == null) path = "L"
		else if (it == ';') {
			theArgs.add("$path;")
			path = null
		} else if (path != null) path += it
		else theArgs.add(it.toString())
	}
	return theArgs.toTypedArray()
}

/** Check if String is a legal type descriptor. */
fun String.isLegalDescriptor(void: Boolean): Boolean = when (length) {
	0 -> false
	1 -> toCharArray()[0].isLegalDescriptor(void)
	else -> reference.matcher(this).matches()
}

/** Check if a Character is a legal type descriptor. */
fun Char.isLegalDescriptor(void: Boolean): Boolean = (void && this == 'V') || this == 'I' || this ==
	'Z' || this == 'J' || this == 'D' || this == 'B' || this == 'F' || this == 'S' || this == 'C'

/** Check if this is a legal method name. */
fun String.isLegalMethodName(): Boolean {
	if (equals("<init>") || equals("<clinit>")) return true
	disallowedCharacters.forEach {
		if (contains(it)) return false
	}
	return true
}

/**
 * Replaces long `.next.next`... code.
 *
 * Example: `ins.next.next.next.next` can be replaced with `ins.next(4)`.
 */
fun AbstractInsnNode.next(amnt: Int): AbstractInsnNode {
	var node = this
	repeat(amnt) { node = node.next }
	return node
}

/**
 * Replaces long `.previous.previous`... code.
 *
 * Example: `ins.previous.previous.previous.previous` can be replaced with `ins.previous(4)`.
 */
fun AbstractInsnNode.previous(amnt: Int): AbstractInsnNode {
	var node = this
	repeat(amnt) { node = node.previous }
	return node
}

/** Seek forward or backward a variable amount of times. */
fun AbstractInsnNode.seek(amnt: Int): AbstractInsnNode {
	if (amnt == 0) return this
	var node = this
	if (amnt > 0) repeat(amnt) { node = node.next }
	else repeat(-amnt) { node = node.previous }
	return node
}

/**
 * Taking a page out of C's `asm` instruction, this uses Koffee to allow you to
 * create InsnLists anywhere with `asm {}`.
 */
fun asm(asm: BlockAssembly.() -> Unit) = assembleBlock(asm).first

/** Adds the given instructions to the end of the list. */
fun InsnList.add(asm: BlockAssembly.() -> Unit) = apply { add(asm(asm)) }
/** Inserts the given instructions at the beginning of this list. */
fun InsnList.insert(asm: BlockAssembly.() -> Unit) = apply { insert(asm(asm)) }
/** Inserts the given instructions after the specified instruction. */
fun InsnList.insert(location: AbstractInsnNode, asm: BlockAssembly.() -> Unit) = apply { insert(location, asm(asm)) }
/** Inserts the given instructions before the specified instruction. */
fun InsnList.insertBefore(location: AbstractInsnNode, asm: BlockAssembly.() -> Unit) = apply { insertBefore(location, asm(asm)) }

/** Koffee ldc but with null values */
fun InstructionAssembly.ldc(v: Any?) = instructions.add(if (v == null) InsnNode(Opcodes.ACONST_NULL) else LdcInsnNode(v))


/*
 * Thanks to Jack for writing some of these for ASMWorkspace.
 */

/** Prevent having to type out that long reference. */
private val deobf: FMLDeobfuscatingRemapper get() = FMLDeobfuscatingRemapper.INSTANCE

/**
 * Map the class name from notch names
 *
 * @return the mapped class name
 */
fun String.getClassFromNotch(): String = deobf.mapType(this)

/**
 * Map the method desc from notch names
 *
 * @return a mapped method desc
 */
fun String.getMethodDescFromNotch(): String = deobf.mapMethodDesc(this)

/**
 * Map the method name from notch names
 *
 * @param[classNode]  the transformed class node
 * @param[methodNode] the transformed classes method node
 * @return a mapped method name
 * @author asbyth
 */
fun mapMethodName(classNode: ClassNode, methodNode: MethodNode): String =
	deobf.mapMethodName(classNode.name, methodNode.name, methodNode.desc)


/**
 * Map the field name from notch names
 *
 * @param[classNode] the transformed class node
 * @param[fieldNode] the transformed classes field node
 * @return a mapped field name
 * @author asbyth
 */
fun mapFieldName(classNode: ClassNode, fieldNode: FieldNode): String =
	deobf.mapFieldName(classNode.name, fieldNode.name, fieldNode.desc)

/**
 * Map the method name from notch names
 *
 * @return a mapped insn method
 * @author asbyth
 */
fun MethodInsnNode.mapMethodName(): String = deobf.mapMethodName(owner, name, desc)

/**
 * Map the field name from notch names
 *
 * @return a mapped insn field
 * @author asbyth
 */
fun FieldInsnNode.mapFieldNameFromNode(): String = deobf.mapFieldName(owner, name, desc)

/**
 * Function to inline the construction of an [InsnList]
 *
 * @param[nodes] nodes to be included in the list
 * @return new InsnList containing the nodes
 */
fun insnListOf(vararg nodes: AbstractInsnNode): InsnList = InsnList().apply { nodes.forEach(::add) }

/**
 * Remove instructions to this MethodNode.
 *
 * @author asbyth
 */
fun MethodNode.clear() {
	instructions.clear()

	// dont waste time clearing local variables if they're empty
	if (localVariables.isNotEmpty()) localVariables.clear()

	// dont waste time clearing try-catches if they're empty
	if (tryCatchBlocks.isNotEmpty()) tryCatchBlocks.clear()
}
