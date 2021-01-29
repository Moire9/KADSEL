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

package tech.napkin.libs.kadsel.dsl

import org.objectweb.asm.Attribute
import org.objectweb.asm.tree.*

class MethodManager internal constructor(internal val methodNode: MethodNode) {

	/** The method's name. */
	var name: String get() = methodNode.name; set(value) { methodNode.name = value }

	/** The method's descriptor. */
	var desc: String get() = methodNode.desc; set(value) { methodNode.desc = value }

	/** The method's signature. */
	var signature: String? get() = methodNode.signature; set(value) { methodNode.signature = value }

	/** The internal names of the method's exception classes. */
	var exceptions: List<String> get() = methodNode.exceptions; set(value) { methodNode.exceptions = value }

	/** The method parameter info (access flags and name). */
	var parameters: List<ParameterNode> get() = methodNode.parameters; set(value) { methodNode.parameters = value }

	/** The runtime visible annotations of this method. */
	var visibleAnnotations: List<AnnotationNode>? get() = methodNode.visibleAnnotations
		set(value) { methodNode.visibleAnnotations = value }

	/** The runtime invisible annotations of this method. */
	var invisibleAnnotations: List<AnnotationNode>? get() = methodNode.invisibleAnnotations
		set(value) { methodNode.invisibleAnnotations = value }

	/** The runtime visible type annotations of this method. */
	var visibleTypeAnnotations: List<TypeAnnotationNode>? get() = methodNode.visibleTypeAnnotations
		set(value) { methodNode.visibleTypeAnnotations = value }

	/** The runtime invisible type annotations of this method. */
	var invisibleTypeAnnotations: List<TypeAnnotationNode>? get() = methodNode.invisibleTypeAnnotations
		set(value) { methodNode.invisibleTypeAnnotations = value }

	/** The non standard attributes of this method. */
	var attrs: List<Attribute>? get() = methodNode.attrs; set(value) { methodNode.attrs = value }

	/**
	 * The default value of this annotation interface method. This field must be a
	 * [Byte], [Boolean], [Character], [Short], [Integer], [Long], [Float], [Double],
	 * [String] or [org.objectweb.asm.Type], or a two elements String array (for
	 * enumeration values), a [AnnotationNode], or a [List] of values of one of the
	 * preceding types.
	 */
	var annotationDefault: Any? get() = methodNode.annotationDefault
		set(value) { methodNode.annotationDefault = value }

	/**
	 * The runtime visible parameter annotations of this method.
	 */
	var visibleParameterAnnotations: Array<List<AnnotationNode>>? get() = methodNode.visibleParameterAnnotations
		set(value) { methodNode.visibleParameterAnnotations = value }

	/** The runtime invisible parameter annotations of this method. */
	var invisibleParameterAnnotations: Array<List<AnnotationNode>>? get() = methodNode.invisibleParameterAnnotations
		set(value) { methodNode.invisibleParameterAnnotations = value }

	/** The instructions of this method. */
	var instructions: InsnList get() = methodNode.instructions; set(value) { methodNode.instructions = value }

	/** The try catch blocks of this method. */
	var tryCatchBlocks: List<TryCatchBlockNode> get() = methodNode.tryCatchBlocks
		set(value) { methodNode.tryCatchBlocks = value }

	/** The maximum stack size of this method. */
	val maxStack get() = methodNode.maxStack

	/** The maximum number of local variables of this method. */
	val maxLocals get() = methodNode.maxLocals

	/** The local variables of this method. */
	var localVariables: List<LocalVariableNode>? get() = methodNode.localVariables
		set(value) { methodNode.localVariables = value }

	/** The visible local variable annotations of this method. */
	var visibleLocalVariableAnnotations: List<LocalVariableAnnotationNode>?
		get() = methodNode.visibleLocalVariableAnnotations
		set(value) { methodNode.visibleLocalVariableAnnotations = value }

	/** The invisible local variable annotations of this method. */
	var invisibleLocalVariableAnnotations: List<LocalVariableAnnotationNode>?
		get() = methodNode.invisibleLocalVariableAnnotations
		set(value) { methodNode.invisibleLocalVariableAnnotations = value }


	fun instructions(config: InsnList.() -> Unit): Unit = methodNode.instructions.config()

	fun node(config: MethodNode.() -> Unit): Unit = methodNode.config()

//	companion object {
//		fun insnList(assembly: BlockAssembly.() -> Unit): Unit = InsnList().koffee(assembly).first
//	}

}
