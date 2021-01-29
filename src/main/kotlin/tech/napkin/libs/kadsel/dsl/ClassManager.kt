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

import com.google.common.collect.ArrayListMultimap
import org.objectweb.asm.Attribute
import org.objectweb.asm.tree.*
import tech.napkin.libs.kadsel.MethodDescriptor
import tech.napkin.libs.kadsel.exception.MalformedDescriptorException
import tech.napkin.libs.kadsel.fullDescriptor

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
	var interfaces: List<String> get() = classNode.interfaces; set(value) { classNode.interfaces = value }

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
	var visibleAnnotations: List<AnnotationNode>? get() = classNode.visibleAnnotations
		set(value) { classNode.visibleAnnotations = value }

	/** The runtime invisible annotations of this class. */
	var invisibleAnnotations: List<AnnotationNode>? get() = classNode.invisibleAnnotations
		set(value) { classNode.invisibleAnnotations = value }

	/** The runtime visible type annotations of this class. */
	var visibleTypeAnnotations: List<TypeAnnotationNode>? get() = classNode.visibleTypeAnnotations
		set(value) { classNode.visibleTypeAnnotations = value }

	/** The runtime invisible type annotations of this class. */
	var invisibleTypeAnnotations: List<TypeAnnotationNode>? get() = classNode.invisibleTypeAnnotations
		set(value) { classNode.invisibleTypeAnnotations = value }

	/** The non standard attributes of this class. */
	var attrs: List<Attribute>? get() = classNode.attrs; set(value) { classNode.attrs = value }

	/** Information about the inner classes of this class. */
	var innerClasses: List<InnerClassNode> get() = classNode.innerClasses; set(value) { classNode.innerClasses = value }

	/** The fields of this class. */
	var fields: List<FieldNode> get() = classNode.fields; set(value) { classNode.fields = value }

	/** The methods of this class. */
	var methods: List<MethodNode> get() = classNode.methods; set(value) { classNode.methods = value }

	/* END OF NODE PROPERTIES */


	/** All methods that are modified by this [ClassManager]. */
	private val methodModifications = ArrayListMultimap.create<MethodDescriptor, MethodManager.() -> Unit>()


	/** Apply all modifications to the given [ClassNode]. */
	internal fun transform(classNode: ClassNode) = methodModifications.keySet().forEach { methodDesc ->
		classNode.methods.find(methodDesc::equals)?.apply {
			MethodManager(this).apply {
				methodModifications.get(methodDesc).forEach(::apply)
			}
		} ?: throw NoSuchMethodException("Could not find method: ${classNode.name}.${methodDesc.fullName}")
	}

	/** Select a method with a descriptor. */
	fun method(desc: String, config: MethodManager.() -> Unit) {
		fullDescriptor.matcher(desc).apply {
			if (matches()) {
				return method(group("name"), group("args"), group("return"), config)
			} else throw MalformedDescriptorException("Invalid descriptor: $desc")
		}
	}

	/** Select a method with no arguments. */
	fun method(name: String, ret: String, config: MethodManager.() -> Unit) = method(name, "", ret, config)

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
