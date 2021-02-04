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

package tech.napkin.libs.kadsel.dsl.manager

import codes.som.anthony.koffee.BlockAssembly
import com.google.common.collect.ArrayListMultimap
import org.objectweb.asm.Attribute
import org.objectweb.asm.tree.*
import tech.napkin.libs.kadsel.*
import tech.napkin.libs.kadsel.dsl.initializer.FieldInitializer
import tech.napkin.libs.kadsel.exception.MalformedDescriptorException

/** Holds a set of modifications to the methods of a class. */
class ClassManager internal constructor(internal val classNode: ClassNode) {

	/* THESE PROPERTIES ARE FOR DIRECT INTERFACING WITH THE NODE */

	/** The class version. */
	var version get() = classNode.version; set(value) { classNode.version = value }

	/**
	 * The class's access flags (see [org.objectweb.asm.Opcodes]). This field
	 * also indicates if the class is deprecated.
	 */
	var access get() = classNode.access; set(value) { classNode.access = value }

	/** The internal name of the class. */
	var name: String get() = classNode.name; set(value) { classNode.name = value }

	/** The signature of the class. */
	var signature: String? get() = classNode.signature; set(value) { classNode.signature = value }

	/**
	 * The internal of the super class. For interfaces, the super class is the root
	 * class. **Only null if this node represents the root class.**
	 */
	var superName: String? get() = classNode.superName; set(value) { classNode.superName = value }

	/** The internal names of the class's interfaces. */
	var interfaces: MutableList<String> get() = classNode.interfaces; set(value) { classNode.interfaces = value }

	/** The name of the source file from which this class was compiled. */
	var sourceFile: String? get() = classNode.sourceFile; set(value) { classNode.sourceFile = value }

	/**
	 * Debug information to compute the correspondence between source and compiled
	 * elements of the class.
	 */
	var sourceDebug: String? get() = classNode.sourceDebug; set(value) { classNode.sourceDebug = value }

	/** The internal name of the enclosing class of the class. */
	var outerClass: String? get() = classNode.outerClass; set(value) { classNode.outerClass = value }

	/**
	 * The name of the method that contains the class, or `null` if the class is not
	 * enclosed in a method.
	 */
	var outerMethod: String? get() = classNode.outerMethod; set(value) { classNode.outerMethod = value }

	/**
	 * The descriptor of the method that contains the class, or `null` if the class is
	 * not enclosed in a method.
	 */
	var outerMethodDesc: String? get() = classNode.outerMethodDesc; set(value) { classNode.outerMethodDesc = value }

	/** The runtime visible annotations of this class. */
	var visibleAnnotations: MutableList<AnnotationNode>? get() = classNode.visibleAnnotations
		set(value) { classNode.visibleAnnotations = value }

	/** The runtime invisible annotations of this class. */
	var invisibleAnnotations: MutableList<AnnotationNode>? get() = classNode.invisibleAnnotations
		set(value) { classNode.invisibleAnnotations = value }

	/** The runtime visible type annotations of this class. */
	var visibleTypeAnnotations: MutableList<TypeAnnotationNode>? get() = classNode.visibleTypeAnnotations
		set(value) { classNode.visibleTypeAnnotations = value }

	/** The runtime invisible type annotations of this class. */
	var invisibleTypeAnnotations: MutableList<TypeAnnotationNode>? get() = classNode.invisibleTypeAnnotations
		set(value) { classNode.invisibleTypeAnnotations = value }

	/** The non standard attributes of this class. */
	var attrs: MutableList<Attribute>? get() = classNode.attrs; set(value) { classNode.attrs = value }

	/** Information about the inner classes of this class. */
	var innerClasses: MutableList<InnerClassNode> get() = classNode.innerClasses
		set(value) { classNode.innerClasses = value }

	/** The fields of this class. */
	var fields: MutableList<FieldNode> get() = classNode.fields; set(value) { classNode.fields = value }

	/** The methods of this class. */
	var methods: MutableList<MethodNode> get() = classNode.methods; set(value) { classNode.methods = value }

	/* END OF NODE PROPERTIES */


	/** All methods that are modified by this [ClassManager]. */
	private val methodModifications = ArrayListMultimap.create<MethodDescriptor, MethodManager.() -> Unit>()

	private val newFields = mutableListOf<FieldInitializer>()

	/** Save all modifications. */
	internal fun transform(mods: List<ClassManager.() -> Unit>) {
		mods.forEach(::apply)
		newFields.forEach { it.transform(classNode) }
		methodModifications.keySet().forEach { methodDesc ->
			classNode.methods.find(methodDesc::equals)?.apply {
				MethodManager(this).apply {
					methodModifications.get(methodDesc).forEach(::apply)
				}
			} ?: throw NoSuchMethodException("Could not find method: ${classNode.name}.${methodDesc.fullName}")
		}
	}

	fun createMethodAsm(access: Int, name: String, args: String, returnType: String,
						exceptions: Array<String> = arrayOf(), config: BlockAssembly.() -> Unit): Unit =
		createMethod(access, name, args, returnType, exceptions) { instructions = asm(config) }


	fun createMethodAsm(access: Int, name: String, returnType: String = "", exceptions: Array<String> = arrayOf(),
						config: BlockAssembly.() -> Unit): Unit =
		createMethod(access, name, returnType, exceptions) { instructions = asm(config) }


	fun createMethod(access: Int, name: String, returnType: String = "", exceptions: Array<String> = arrayOf(),
					 config: MethodManager.() -> Unit) =
		createMethod(access, name, "", returnType, exceptions, config)


	fun createMethod(access: Int, name: String, args: String, returnType: String, exceptions: Array<String> = arrayOf(),
					 config: MethodManager.() -> Unit) {
		methods.add(MethodNode(access, name, "($args)$returnType", null, exceptions))
		methodModifications.put(MethodDescriptor(name, args, returnType), config)
	}

	/** Create a new field, storing into it [value]. */
	fun createField(access: Int, name: String, type: String, value: Any?) {
		newFields.add(FieldInitializer(access, name, type, value, null))
	}

	/**
	 * Create a new field, and initialize it using the top item of the stack at the end
	 * of the provided initialization code.
	 */
	fun createField(access: Int, name: String, type: String, initializer: BlockAssembly.() -> Unit) {
		newFields.add(FieldInitializer(access, name, type, null, asm(initializer)))
	}

	/** Select a constructor. */
	fun constructor(args: String = "", config: MethodManager.() -> Unit) =
		method("<init>", args, "V", config)

	/** Select clinit. */
	fun clinit(config: MethodManager.() -> Unit) = method("<clinit>", "", "V", config)

	/** Select a method with a descriptor. */
	fun methodDesc(desc: String, config: MethodManager.() -> Unit) {
		fullDescriptor.matcher(desc).apply {
			if (matches()) {
				return method(group("name"), group("args"), group("return"), config)
			} else throw MalformedDescriptorException("Invalid descriptor: $desc")
		}
	}

	/** Select a method with no arguments. */
	fun method(name: String, returnType: String = "", config: MethodManager.() -> Unit) =
		method(name, "", returnType, config)

	/**
	 * Select a method within the class.
	 *
	 * Example: to select `boolean teleportTo(double x, double y, double z)`, use:
	 * `method("teleportTo", "DDD", "Z") {}`
	 */
	fun method(name: String, args: String, returnType: String, config: MethodManager.() -> Unit) {
		methodModifications.put(MethodDescriptor(name, args, returnType), config)
	}

}
