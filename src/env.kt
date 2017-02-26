class Env(var env: Tuple<String, RuriType>, binds: List<SymbolType>? = null, exprs: MutableList<RuriType>? = null) {
    constructor(env: Env, binds: List<SymbolType>? = null, exprs: MutableList<RuriType>? = null) : this(env.env, binds, exprs)

    init {
        if (binds != null && exprs != null) {
            val bindsIter = binds.iterator()
            val exprsIter = exprs.iterator()
            while (bindsIter.hasNext()) {
                val b = bindsIter.next()
                this.set(b.value, if (exprsIter.hasNext()) exprsIter.next() else NilType())
            }
        }
    }

    operator fun get(key: String): RuriType? {
        return env[key]
    }

    operator fun set(key: String, value: RuriType): RuriType {
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
