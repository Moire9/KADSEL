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

package tech.napkin.libs.kadsel.dsl.initializer

import codes.som.anthony.koffee.insns.jvm.*
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*
import tech.napkin.libs.kadsel.*


internal class FieldInitializer(
	private val access: Int, private val name: String, private val type: String,
	private val value: Any?, private val initializer: InsnList?
) {

	private val static = access and Opcodes.ACC_STATIC != 0


	internal fun transform(node: ClassNode) {
		node.fields.add(FieldNode(access, name, type, null, null))
		node.methods.filter { it.name == if (static) "<clinit>" else "<init>" }.let { if (it.isEmpty()) null else it }
			?.forEach { it.instructions.insert(it.instructions.last.previous /* before return */,
				initializer?.add {
					if (this@FieldInitializer.static) putstatic(node.name, name, type)
					else {
						aload_0
						putfield(node.name, name, type)
					}
				} ?: asm {
					ldc(value)
					if (this@FieldInitializer.static) putstatic(node.name, name, type)
					else {
						aload_0
						putfield(node.name, name, type)
					}
				})
			} ?: node.methods.add(MethodNode(Opcodes.ACC_PUBLIC, if (static) "<clinit>" else
				throw Exception("Auto-generated <init> methods for fields are not supported! (Also, you are seeing " +
					"this error because this class has no constructor methods, which shouldn't be possible)"),
			"()V", null, arrayOf()).apply {
			instructions = (initializer ?: asm { ldc(value) }).add {
				putstatic(node.name, name, type)
				_return
			}}
		)
	}

}
