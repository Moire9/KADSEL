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

package tech.napkin.libs.kadsel

import org.objectweb.asm.tree.MethodNode
import tech.napkin.libs.kadsel.exception.*

/** Class to simplify storage and parsing of method descriptors. */
internal class MethodDescriptor(parent: String?, val name: String, val args: String, val returnType: String) {

//	/**
//	 * Takes a
//	 */
//	constructor(fullPath: String) {
//		fullPath.split('.', '(', ')').apply {
//			when (size) {
//				3 -> MethodDescriptor(get(0), get(1), get(2))
//				4 -> MethodDescriptor(get(0), get(1), get(2), get(3))
//				else -> throw MalformedDescriptorException("Invalid descriptor: $fullPath", null)
//			}
//		}
//	}


	/** Optional parent class */
	var parent: String? = null; set(value) { // effectively lateinit val
		if (field == null) field = value
	}

	/**
	 * Full descriptor.
	 *
	 * Example: `(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V`
	 */
	val desc = "($args)$returnType"

	/**
	 * Full class name, with [parent] if available.
	 *
	 * Example: `net/minecraft/world/World.spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V`
	 */
	val fullName get() = (if (parent == null) "" else "$parent.") + "$name$desc"

	/** Prevent having to make a new reference array every time you want to parse it */
	val refArray = args.toRefArray()

	/** If the Method is valid but won't pass typical JVM name validity checks */
	val isSpecial = returnType == "V" && (name == "<clinit>" && args.isEmpty() || name == "<init>")

	init {
		this.parent = parent

		if (!isSpecial && !name.isLegalMethodName()) {
			throw MalformedNameException(this)
		}

		if (!returnType.isLegalDescriptor(true)) {
			throw MalformedReturnTypeException(this)
		}

		refArray.forEachIndexed { index, ref ->
			if (!ref.isLegalDescriptor(false)) {
				throw MalformedArgumentException(this, index)
			}
		}

	}

	constructor(name: String, args: String, returnType: String): this(null, name, args, returnType)

	constructor(name: String, returnType: String): this(name, "", returnType)

	/** Makes checking if this matches a MethodNode easier. */
	override fun equals(other: Any?): Boolean =
		if (other is MethodNode) other.name == name && other.desc == desc else super.equals(other)

	/** IntelliJ yelled at me to make this. */
	override fun hashCode(): Int =
		(((name.hashCode() * 31 + args.hashCode()) * 31 + returnType.hashCode()) * 31 + (parent?.hashCode() ?: 0))

	companion object {

		/** Can't use a constructor for this, because Kotlin™. */
		internal fun fromFullPath(path: String): MethodDescriptor {
			fullDescriptor.matcher(path).apply {
				if (matches()) {
					return MethodDescriptor(group("name"), group("args"), group("return"))
				} else throw MalformedDescriptorException("Invalid descriptor: '$path'")
			}
		}

	}
}
