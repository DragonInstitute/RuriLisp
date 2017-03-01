class Reader(val tokens: Sequence<String>) {

    private var iter = tokens.iterator()
    private var current = _next()

    fun next(): String? {
        val cur = current
        current = _next()
        return cur
    }

    private fun _next() = if (iter.hasNext()) iter.next() else null

    fun current() = current

}

val TOKEN_REGEX = Regex("""[\s,]*(~@|[\[\]{}()'`~^@]|"(?:\\.|[^\\"])*"|;.*|[^\s\[\]{}('"`,;)]*)""")
val ATOM_REGEX = Regex("""(^-?[0-9]+$)|(^nil$)|(^true$)|(^false$)|^"(.*)"$|:(.*)|(^[^"]*$)""")

fun readStr(str: String?): RuriType {
    if (str == null) return NilType()
    val tokens = tokenize(str)
    return readForm(Reader(tokens))
}

fun tokenize(code: String): Sequence<String> {
    return TOKEN_REGEX.findAll(code)
            .map { it -> it.groups[1]?.value as String }
            .filter { it -> it != "" && !it.startsWith(";") }
}

fun readForm(reader: Reader): RuriType =
        when (reader.current()) {
            null -> throw EOFException()
            "(" -> readList(reader)
            ")" -> throw UnexpectedException(")")
            "[" -> readVector(reader)
            "]" -> throw UnexpectedException("]")
            "{" -> readHashMap(reader)
            "}" -> throw UnexpectedException("}")
            "'" -> readShorthand(reader, "quote")
            "`" -> readShorthand(reader, "quasiquote")
            "~" -> readShorthand(reader, "unquote")
            "~@" -> readShorthand(reader, "splice-unquote")
            else -> readAtom(reader)
        }

fun readShorthand(reader: Reader, symbol: String): RuriType {
    reader.next()
    val list = RuriList()
    list.add(SymbolType(symbol))
    list.add(readForm(reader))
    return list
}

fun readVector(reader: Reader) = readSequence(reader, RuriVector(), "]", ::readForm)
fun readList(reader: Reader) = readSequence(reader, RuriList(), ")", ::readForm)
fun readHashMap(reader: Reader): RuriHashMap {
    reader.next()
    val hashMap = RuriHashMap()
    do {
        var value: RuriType? = null
        val key = when (reader.current()) {
            null -> throw Exception("expected '}', got EOF")
            "}" -> {
                reader.next(); null
            }
            else -> {
                val key = readForm(reader) as? StringType ?: throw Exception("HashMap keys must be strings or keywords")
                value = when (reader.current()) {
                    null -> throw Exception("expected form, got EOF")
                    else -> readForm(reader)
                }
                key
            }
        }
        if (key != null) {
            hashMap.add(key, value as RuriType)
        }
    } while (key != null)

    return hashMap
}

fun readSequence(reader: Reader, list: IRuriSequence, end: String, process: (Reader) -> RuriType): RuriType {
    reader.next()
    do {
        val form = when (reader.current()) {
            null -> throw EOFException(")")
            end -> {
                reader.next(); null
            }
            else -> process(reader)
        }

        if (form != null) {
            list.add(form)
        }
    } while (form != null)
    return list
}

fun readAtom(reader: Reader): RuriType {
    val next = reader.next() ?: throw Exception("Expected atom, got null")
    val groups = ATOM_REGEX.find(next)?.groups ?: throw Exception("Unrecognized token: $next")


    return if (groups[1]?.value != null) {
        IntegerType(groups[1]?.value?.toLong() ?: throw Exception("Unparseable token: $next, should be number"))
    } else if (groups[2]?.value != null) {
        NilType()
    } else if (groups[3]?.value != null) {
        TrueType()
    } else if (groups[4]?.value != null) {
        FalseType()
    } else if (groups[5]?.value != null) {
        StringType((groups[5]?.value as String).replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\"))
    } else if (groups[6]?.value != null) {
        KeywordType(groups[6]?.value as String)
    } else if (groups[7]?.value != null) {
        SymbolType(groups[7]?.value as String)
    } else {
        throw Exception("Unrecognized token: $next")
    }
}
