class Env(var env: Tuple<String, Type>) {
    operator fun get(key: String): Type? {
        return env[key]
    }
}

val commonEnv: Env
        = Env(makeTuple(Pair("+", FunctionType({ a: NodeList -> a.elements.reduce({ acc, x -> acc as IntegerType + x as IntegerType }) })),
        Pair("-", FunctionType({ a: NodeList -> a.elements.reduce({ acc, x -> acc as IntegerType - x as IntegerType }) })),
        Pair("*", FunctionType({ a: NodeList -> a.elements.reduce({ acc, x -> acc as IntegerType * x as IntegerType }) })),
        Pair("/", FunctionType({ a: NodeList -> a.elements.reduce({ acc, x -> acc as IntegerType / x as IntegerType }) }))))

class Tuple<K, V>(var value: MutableMap<K, V>) {
    operator fun get(key: K): V? {
        return value[key]
    }
}

fun <K, V> makeTuple(vararg pairs: Pair<K, V>): Tuple<K, V> {
    return Tuple(mutableMapOf(*pairs))
}
