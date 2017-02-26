fun main(args: Array<String>) {
    rep(commonEnv)
}

fun r(): String? {
    return readLine()
}

fun read(code: String?): RuriType {
    return readStr(code)
}

fun eval(ast: RuriType, env: Env): RuriType =
        if (ast is RuriList && ast.len() > 0) {
            val first = ast.first()
            if (first is SymbolType) {
                when (first.value) {
                    "def!" -> evalDefine(ast, env)
                    "let*" -> evalLet(ast, env)
                    "fn*" -> evalFn(ast, env)
                    "do" -> evalDo(ast, env)
                    "if" -> evalIf(ast, env)
                    else -> evalFunction(ast, env)
                }
            } else evalFunction(ast, env)
        } else
            evalAst(ast, env)

fun evalFn(ast: RuriList, env: Env): RuriType {
    val bindList = ast.at(1) as? RuriSequence ?: throw RuriException("fn* needs a bind list as first parameter")
    val symbols = bindList.elements.filterIsInstance<SymbolType>()
    val exprs = ast.at(2)
    return FunctionType({
        s: RuriSequence ->
        // virtual env binding
        eval(exprs, Env(env, symbols, s.elements))
    })

}

fun evalDo(ast: RuriList, env: Env): RuriType =
        (evalAst(ast.rest(), env) as RuriSequence).elements.last()

fun evalIf(ast: RuriList, env: Env): RuriType {
    val condition = eval(ast.at(1), env)
    return if (condition !is FalseType && condition !is NilType) {
        eval(ast.at(2), env)
    } else if (ast.len() > 3) {
        eval(ast.at(3), env)
    } else {
        NilType()
    }
}

fun evalFunction(ast: RuriType, env: Env): RuriType {
    val evaluated = evalAst(ast, env) as RuriSequence
    if (evaluated.first() !is FunctionType) throw NonFunctionException("${evaluated.first()}")
    return (evaluated.first() as FunctionType).apply(evaluated.rest())
}

fun evalDefine(ast: RuriList, env: Env): RuriType {
    return env.set((ast.at(1) as SymbolType).value, eval(ast.at(2), env))
}

fun evalLet(ast: RuriList, env: Env): RuriType {
    val inner = Env(env)
    val bind = ast.at(1) as? RuriSequence ?: throw Exception("Expected sequence")
    val iter = bind.iterator()
    while (iter.hasNext()) {
        val key = iter.next()
        if (!iter.hasNext()) throw Exception("Let should have even number parameter")

        val value = eval(iter.next(), inner)
        inner[(key as SymbolType).value] = value
    }
    return eval(ast.at(2), inner)
}

fun evalAst(ast: RuriType, env: Env): RuriType = when (ast) {
// reduce
    is SymbolType -> env[ast.value] ?: error("${ast.value} not found")
    is RuriList -> ast.elements.fold(RuriList(), { acc, x -> acc.add(eval(x, env)); acc })
    is RuriVector -> ast.elements.fold(RuriVector(), { acc, x -> acc.add(eval(x, env)); acc })
    is RuriHashMap -> ast.elements.entries.fold(RuriHashMap(), { a, b -> a.add(b.key, eval(b.value, env)); a })
    else -> ast
}

fun p(s: RuriType) {
    when (s) {
        is NilType -> println("Nil")
        is IntegerType -> println("Integer $s")
        is FalseType -> println("False")
        is TrueType -> println("True")
        is SymbolType -> println("Symbol $s")
        is StringType -> println("String $s")
        is KeywordType -> println("Keyword $s")
        is RuriList -> println("List $s")
        is RuriVector -> println("Vector $s")
        is RuriHashMap -> println("HashMap $s")
        is FunctionType -> println("Function $s")
        else -> println("What the hell?")
    }
}

fun rep(env: Env) {
    prompt()
    var s = r()
    while (s != null) {
        p(eval(read(s), env))
        prompt()
        s = r()
    }
}

fun prompt(text: String = "Lisp> ") {
    print(text)
}
