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

import com.google.common.collect.ArrayListMultimap
import net.minecraft.launchwrapper.IClassTransformer
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import tech.napkin.libs.kadsel.dsl.ClassManager
import tech.napkin.libs.kadsel.dsl.TransformerManager
import java.io.File
import java.io.FileOutputStream

/**
 * In your [net.minecraftforge.fml.relauncher.IFMLLoadingPlugin], specify
 * `ClassTransformer::class.java.name` as one of your ASM Transformer classes.
 *
 * It is recommended to register your transformers in the clinit of that class as well.
 *
 * Thank you very much to [jack](https://github.com/asbyth/ASMWorkspace) for the
 * original Java code!
 */
class ClassTransformer : IClassTransformer {

	companion object {

		private val bytecodeDirectory = File("bytecode")

		private val logger = LogManager.getLogger(this)

		/**
		 * If true, dump transformed classes.
		 *
		 * Activate with `-DdebugBytecode` JVM argument.
		 */
		private val outputBytecode = (System.getProperty("debugBytecode") != null).also {
			if (it && !bytecodeDirectory.exists()) bytecodeDirectory.mkdirs()
		}

		/** Multimap of class managers to their name (to make finding them easier). */
		private val classMap = ArrayListMultimap.create<String, ClassManager.() -> Unit>()

		/** Register multiple [TransformerManager]s using DSL notation. */
		fun registerTransformer(vararg transformers: TransformerManager.() -> Unit): Unit = // must specify type
			transformers.forEach(::registerTransformer)

		/**
		 * Register a [TransformerManager] using DSL notation.
		 *
		 * It is highly recommended that you register your transformers at clinit!!
		 */
		fun registerTransformer(transformer: TransformerManager.() -> Unit) =
			classMap.putAll(TransformerManager(transformer).classModifications)


		/** Write bytecode to the `.minecraft/bytecode` directory. */
		private fun outputBytecode(writer: ClassWriter, className: String) {
			val output = File(bytecodeDirectory, className.replace('$', '.') + ".class")

			try {
				if (!output.exists()) output.createNewFile()

				FileOutputStream(output).use { it.write(writer.toByteArray()) }
			} catch (e: Exception) {
				logger.error("Failed to dump bytecode for $className", e)
			}
		}

		/** If the class is one we want to modify, return the modified version. */
		private fun transform(name: String, bytes: ByteArray): ByteArray = if (!classMap.containsKey(name)) bytes else {
			// todo see if i can have just one writer and re-use it constantly
			val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES)
			val node = ClassNode().apply {
				ClassReader(bytes).accept(this, ClassReader.EXPAND_FRAMES)
			}

			ClassManager(node).apply { classMap.get(name).forEach(::apply) }

			try {
				node.accept(writer)
			} catch (e: Exception) {
				logger.error("Exception writing transformed class $name", e)
			}

			if (outputBytecode) outputBytecode(writer, name)

			writer.toByteArray()
		}

	}

	/** Non-static wrapper that also eliminates pesky nulls. */
	override fun transform(name: String?, className: String?, bytes: ByteArray?): ByteArray? =
		if (bytes == null || className == null || name == null) null else transform(className, bytes)

}
