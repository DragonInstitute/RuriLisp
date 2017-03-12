import java.util.*

fun dynamicLoadCore(): Env {
    dynamicRun("(def! not (fn* (a) (if a false true)))", coreEnv)

    return coreEnv
}

val coreEnv: Env = makeEnv(
        makePair("+", { a: RuriList -> a.elements.reduce({ acc, x -> acc as IntegerType + x as IntegerType }) }),
        makePair("-", { a: RuriList -> a.elements.reduce({ acc, x -> acc as IntegerType - x as IntegerType }) }),
        makePair("*", { a: RuriList -> a.elements.reduce({ acc, x -> acc as IntegerType * x as IntegerType }) }),
        makePair("/", { a: RuriList -> a.elements.reduce({ acc, x -> acc as IntegerType / x as IntegerType }) }),
        makePair("cons", { a: RuriList ->
            val list = a.at(1) as? RuriList ?: throw RuriException("cons requires a list as its second parameter")
            val mutableList = list.elements.toCollection(LinkedList<RuriType>())
            mutableList.addFirst(a.at(0))
            RuriList(mutableList)
        }),
        makePair("concat", { a: RuriList ->
            RuriList(a.elements.flatMap({ it -> (it as RuriSequence).elements }).toCollection(LinkedList<RuriType>()))
        }),
        makePair("throw", { a: RuriList ->
            val throwable = a.at(0)
            throw RuriException(throwable.toString())
        }),
        makePair("print", {a: RuriList ->
            p(a.at(1))
            NilType()
        })
)

fun makePair(symbol: String, lambda: (RuriList) -> RuriType): Pair<String, RuriType>{
    return Pair(symbol, FunctionType(lambda))
}

fun makeEnv(vararg pairs: Pair<String, RuriType>): Env {
    return Env(makeTuple(
            *pairs
    ))
}
