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

import java.util.regex.Pattern // fuck kotlin.text.Regex, all my homies hate kotlin.text.Regex


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
