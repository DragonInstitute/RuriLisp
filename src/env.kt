class Env(var env: Tuple<String, RuriType>) {
    constructor(env: Env): this(env.env)

    operator fun get(key: String): RuriType? {
        return env[key]
    }

    operator fun set(key: String, value: RuriType): RuriType{
         env.set(key, value)
        return value
    }
}

val commonEnv: Env
        = Env(makeTuple<String, RuriType>(Pair("+", FunctionType({ a: RuriList -> a.elements.reduce({ acc, x -> acc as IntegerType + x as IntegerType }) })),
        Pair("-", FunctionType({ a: RuriList -> a.elements.reduce({ acc, x -> acc as IntegerType - x as IntegerType }) })),
        Pair("*", FunctionType({ a: RuriList -> a.elements.reduce({ acc, x -> acc as IntegerType * x as IntegerType }) })),
        Pair("/", FunctionType({ a: RuriList -> a.elements.reduce({ acc, x -> acc as IntegerType / x as IntegerType }) }))))

class Tuple<K, V>(var elements: MutableMap<K, V>) {
    operator fun get(key: K): V? {
        return elements[key]
    }

    operator fun set(key: K, value: V): V? {
        return elements.put(key, value)
    }
}

fun <K, V> makeTuple(vararg pairs: Pair<K, V>): Tuple<K, V> {
    return Tuple(mutableMapOf(*pairs))
}
