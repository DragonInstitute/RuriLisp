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
val a = Regex("""[\s,]*([^\s]*)""")
val TOKEN_REGEX = Regex("""[\s,]*(~@|[\[\]{}()'`~^@]|"(?:\\.|[^\\"])*"|;.*|[^\s\[\]{}('"`,;)]*)""")
val ATOM_REGEX = Regex("""(^-?[0-9]+$)|(^nil$)|(^true$)|(^false$)|^"(.*)"$|:(.*)|(^[^"]*$)""")

fun readStr(str: String?): Type {
    if (str == null) return NilType()
    val tokens = tokenize(str)
    return read_form(Reader(tokens))
}

fun tokenize(code: String): Sequence<String> {
    return TOKEN_REGEX.findAll(code)
            .map { it -> it.groups[1]?.value as String }
            .filter { it -> it != "" && !it.startsWith(";") }
}

fun read_form(reader: Reader): Type =
        when (reader.current()) {
            null -> throw Exception("Unexpected EOF")
            "(" -> read_list(reader, NodeList(), ")")
            ")" -> throw Exception("Unexpected ), expect form")
            else -> read_atom(reader)
        }

fun read_list(reader: Reader, list: NodeList, end: String): Type {
    reader.next()
    do {
        val form = when (reader.current()) {
            null -> throw Exception("Expected ), got EOF")
            end -> {
                reader.next(); null
            }
            else -> read_form(reader)
        }

        if (form != null) {
            list.add(form)
        }
    } while (form != null)
    return list
}

fun read_atom(reader: Reader): Type {
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
