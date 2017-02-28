import java.util.*

fun dynamicLoadCore(): Env {
    dynamicRun("(def! not (fn* (a) (if a false true))", coreEnv)


    return coreEnv
}

val coreEnv: Env = makeEnv(
        makePair("+", { a: RuriList -> a.elements.reduce({ acc, x -> acc as IntegerType + x as IntegerType }) }),
        makePair("-", { a: RuriList -> a.elements.reduce({ acc, x -> acc as IntegerType - x as IntegerType }) }),
        makePair("*", { a: RuriList -> a.elements.reduce({ acc, x -> acc as IntegerType * x as IntegerType }) }),
        makePair("/", { a: RuriList -> a.elements.reduce({ acc, x -> acc as IntegerType / x as IntegerType }) }),
        makePair("cons", { a: RuriSequence ->
            val list = a.at(1) as? RuriSequence ?: throw RuriException("cons requires a list as its second parameter")
            val mutableList = list.elements.toCollection(LinkedList<RuriType>())
            mutableList.addFirst(a.at(0))
            RuriList(mutableList)
        }),
        makePair("concat", { a: RuriSequence ->
            RuriList(a.elements.flatMap({ it -> (it as RuriSequence).elements }).toCollection(LinkedList<RuriType>()))
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

//fun makeEnv(env: Env, vararg pairs: Pair<String, RuriType>): Env {
//
//}