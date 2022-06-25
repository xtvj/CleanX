// SPDX-License-Identifier: GPL-3.0-or-later
package github.xtvj.cleanx.shell

import java.io.File
import java.io.IOException
import java.io.StringWriter
import java.io.Writer
import java.security.InvalidParameterException
import java.util.*

object RunnerUtils {

    private const val EMPTY = ""
    private var ESCAPE_XSI: LookupTranslator


    fun escape(input: String): String {
        return ESCAPE_XSI.translate(input)
    }

    fun isRootGiven(): Boolean {
        if (isRootAvailable()) {
            val output = Runner.runCommand(Runner.rootInstance(), "echo CleanXRoot").output
            return output.contains("CleanXRoot")
        }
        return false
    }

    fun isRootAvailable(): Boolean {
        val pathEnv = System.getenv("PATH")
        if (pathEnv != null) {
            for (pathDir in pathEnv.split(":").toTypedArray()) {
                try {
                    if (File(pathDir, "su").canExecute()) {
                        return true
                    }
                } catch (ignore: Exception) {
                }
            }
        }
        return false
    }


    class LookupTranslator(lookupMap: Map<CharSequence, CharSequence>?) {
        /**
         * The mapping to be used in translation.
         */
        private val lookupMap: MutableMap<String, String>

        /**
         * The first character of each key in the lookupMap.
         */
        private val prefixSet: BitSet

        /**
         * The length of the shortest key in the lookupMap.
         */
        private val shortest: Int

        /**
         * The length of the longest key in the lookupMap.
         */
        private val longest: Int

        /**
         * Translate a set of codepoints, represented by an int index into a CharSequence,
         * into another set of codepoints. The number of codepoints consumed must be returned,
         * and the only IOExceptions thrown must be from interacting with the Writer so that
         * the top level API may reliably ignore StringWriter IOExceptions.
         *
         * @param input CharSequence that is being translated
         * @param index int representing the current point of translation
         * @param out   Writer to translate the text to
         * @return int count of codepoints consumed
         * @throws IOException if and only if the Writer produces an IOException
         */
        @Throws(IOException::class)
        fun translate(input: CharSequence, index: Int, out: Writer): Int {
            // check if translation exists for the input at position index
            if (prefixSet[input[index].code]) {
                var max = longest
                if (index + longest > input.length) {
                    max = input.length - index
                }
                // implement greedy algorithm by trying maximum match first
                for (i in max downTo shortest) {
                    val subSeq = input.subSequence(index, index + i)
                    val result = lookupMap[subSeq.toString()]
                    if (result != null) {
                        out.write(result)
                        return i
                    }
                }
            }
            return 0
        }

        /**
         * Helper for non-Writer usage.
         *
         * @param input CharSequence to be translated
         * @return String output of translation
         */
        fun translate(input: CharSequence): String {
            return try {
                val writer = StringWriter(input.length * 2)
                translate(input, writer)
                writer.toString()
            } catch (ioe: IOException) {
                // this should never ever happen while writing to a StringWriter
                throw RuntimeException(ioe)
            }
        }

        /**
         * Translate an input onto a Writer. This is intentionally final as its algorithm is
         * tightly coupled with the abstract method of this class.
         *
         * @param input CharSequence that is being translated
         * @param out   Writer to translate the text to
         * @throws IOException if and only if the Writer produces an IOException
         */
        @Throws(IOException::class)
        fun translate(input: CharSequence, out: Writer) {
            var pos = 0
            val len = input.length
            while (pos < len) {
                val consumed = translate(input, pos, out)
                if (consumed == 0) {
                    // inlined implementation of Character.toChars(Character.codePointAt(input, pos))
                    // avoids allocating temp char arrays and duplicate checks
                    val c1 = input[pos]
                    out.write(c1.code)
                    pos++
                    if (Character.isHighSurrogate(c1) && pos < len) {
                        val c2 = input[pos]
                        if (Character.isLowSurrogate(c2)) {
                            out.write(c2.code)
                            pos++
                        }
                    }
                    continue
                }
                // contract with translators is that they have to understand codepoints
                // and they just took care of a surrogate pair
                for (pt in 0 until consumed) {
                    pos += Character.charCount(Character.codePointAt(input, pos))
                }
            }
        }

        /**
         * Define the lookup table to be used in translation
         *
         *
         * Note that, as of Lang 3.1 (the origin of this code), the key to the lookup
         * table is converted to a java.lang.String. This is because we need the key
         * to support hashCode and equals(Object), allowing it to be the key for a
         * HashMap. See LANG-882.
         *
         * lookupMap Map&lt;CharSequence, CharSequence&gt; table of translator
         * mappings
         */
        init {
            if (lookupMap == null) {
                throw InvalidParameterException("lookupMap cannot be null")
            }
            this.lookupMap = HashMap()
            prefixSet = BitSet()
            var currentShortest = Int.MAX_VALUE
            var currentLongest = 0
            for ((key, value) in lookupMap) {
                this.lookupMap[key.toString()] = value.toString()
                prefixSet.set(key[0].code)
                val sz = key.length
                if (sz < currentShortest) {
                    currentShortest = sz
                }
                if (sz > currentLongest) {
                    currentLongest = sz
                }
            }
            shortest = currentShortest
            longest = currentLongest
        }
    }

    init {
        val escapeXsiMap = mutableMapOf<CharSequence, CharSequence>()
        escapeXsiMap.plus(Pair("|", "\\|"))
        escapeXsiMap.plus(Pair("&", "\\&"))
        escapeXsiMap.plus(Pair(";", "\\;"))
        escapeXsiMap.plus(Pair("<", "\\<"))
        escapeXsiMap.plus(Pair(">", "\\>"))
        escapeXsiMap.plus(Pair("(", "\\("))
        escapeXsiMap.plus(Pair(")", "\\)"))
        escapeXsiMap.plus(Pair("$", "\\$"))
        escapeXsiMap.plus(Pair("`", "\\`"))
        escapeXsiMap.plus(Pair("\\", "\\\\"))
        escapeXsiMap.plus(Pair("\"", "\\\""))
        escapeXsiMap.plus(Pair("'", "\\'"))
        escapeXsiMap.plus(Pair(" ", "\\ "))
        escapeXsiMap.plus(Pair("\t", "\\\t"))
        escapeXsiMap.plus(Pair("\r\n", EMPTY))
        escapeXsiMap.plus(Pair("\n", EMPTY))
        escapeXsiMap.plus(Pair("*", "\\*"))
        escapeXsiMap.plus(Pair("?", "\\?"))
        escapeXsiMap.plus(Pair("[", "\\["))
        escapeXsiMap.plus(Pair("#", "\\#"))
        escapeXsiMap.plus(Pair("~", "\\~"))
        escapeXsiMap.plus(Pair("=", "\\="))
        escapeXsiMap.plus(Pair("%", "\\%"))
        ESCAPE_XSI = LookupTranslator(Collections.unmodifiableMap(escapeXsiMap))
    }
}