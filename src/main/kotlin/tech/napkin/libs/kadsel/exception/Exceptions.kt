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

package tech.napkin.libs.kadsel.exception

import tech.napkin.libs.kadsel.MethodDescriptor

/*
 * This is located in `exception` package to reduce clutter of the root package when compiled.
 */

/** When your descriptor is incorrect in some, unknown way. */
internal open class MalformedDescriptorException(error: String) : Exception(error) {
	constructor(error: String, desc: MethodDescriptor) : this(error + " - ${desc.fullName}")
}

/** If the arguments of your descriptor are invalid. */
internal class MalformedArgumentException(desc: MethodDescriptor, index: Int) :
	MalformedDescriptorException("Argument type '${desc.refArray[index]}' (index $index) is not valid!", desc)

/** If your method's name is illegal. */
internal class MalformedNameException(desc: MethodDescriptor) :
	MalformedDescriptorException("Method name '${desc.name}' is not valid!", desc)

/** If your method's return type is not valid. */
internal class MalformedReturnTypeException(desc: MethodDescriptor) :
	MalformedDescriptorException("Return type '${desc.returnType}' is not valid!", desc)
