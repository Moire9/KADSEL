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

import com.google.common.collect.ArrayListMultimap
import tech.napkin.libs.kadsel.exception.MalformedDescriptorException
import tech.napkin.libs.kadsel.fullDescriptor

/** A class to hold a set of modifications to different classes. */
class TransformerManager internal constructor(config: TransformerManager.() -> Unit) {


	/** All the classes' modifications. */
	internal val classModifications = ArrayListMultimap.create<String, ClassManager.() -> Unit>()

	/**
	 * Select a method with it's full JVM name.
	 *
	 * **THIS TAKES A JVM NAME!!!**
	 *
	 * Example:
	 * `net.minecraft.entity.monster.EntityEnderman#teleportTo(double, double, double)`
	 * -> `method(net/minecraft/entity/monster/EntityEnderman.teleportTo(DDD)V`
	 */
	@Suppress("UNREACHABLE_CODE")
	fun method(desc: String, config: MethodManager.() -> Unit) = fullDescriptor.matcher(desc).apply {
		if (matches()) return method(group("parent") ?: throw MalformedDescriptorException(
				"Method selector in transformer block must have a parent!"), group("name"), group("args"),
			group("return"), config) else throw MalformedDescriptorException("Invalid descriptor - $desc")
	}.let{}

	/**
	 * Select a method with no arguments directly without needing a class specifier.
	 *
	 * @param[parent] the class to which the method belongs
	 * @param[returnType] the return type of the method
	 */
	fun method(parent: String, name: String, returnType: String, config: MethodManager.() -> Unit) =
		method(parent, name, "", returnType, config)

	/**
	 * Select a method directly without needing a class specifier.
	 *
	 * @param[parent] the class to which the method belongs
	 * @param[args] the arguments of the method
	 * @param[returnType] the return type of the method
	 */
	fun method(parent: String, name: String, args: String, returnType: String, config: MethodManager.() -> Unit) =
		clazz(parent) { method(name, args, returnType, config) }

	/** Select a class, via it's **Java name**. */
	fun clazz(className: String, config: ClassManager.() -> Unit) {
		classModifications.put(className, config)
	}


	/**
	 * Put this at the bottom so that it is run after any fields are initialized, and
	 * so that if any future fields are added it will always be last.
	 */
	init { config() }

}
