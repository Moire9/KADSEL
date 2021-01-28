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
import org.objectweb.asm.tree.ClassNode
import tech.napkin.libs.kadsel.MethodDescriptor
import tech.napkin.libs.kadsel.exception.MalformedDescriptorException
import tech.napkin.libs.kadsel.fullDescriptor

/** Holds a set of modifications to the methods of a class. */
class ClassManager internal constructor(internal val className: String) {

	/** All methods that are modified by this [ClassManager]. */
	private val methods = ArrayListMultimap.create<MethodDescriptor, MethodManager.() -> Unit>()


	/** Apply all modifications to the given [ClassNode]. */
	fun transform(classNode: ClassNode) = methods.keySet().forEach { methodDesc ->
		classNode.methods.find(methodDesc::equals)?.apply {
			MethodManager(this).apply {
				methods.get(methodDesc).forEach(::apply)
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
		methods.put(MethodDescriptor(name, args, returnType), config)
	}

}
