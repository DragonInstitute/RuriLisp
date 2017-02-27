fun main(args: Array<String>) {
    rep(commonEnv)
}

fun r(): String? {
    return readLine()
}

fun read(code: String?): RuriType {
    return readStr(code)
}

fun eval(_ast: RuriType, _env: Env): RuriType {
    var ast = _ast
    var env = _env
    while (true) {
        if (ast is RuriList) {
            if (ast.len() == 0) {
                return ast
            } else {
                val first = ast.first()

                    when ((first as? SymbolType)?.value) {
                        "def!" -> {
                            evalDefine(ast, env)
                        }
                        "let*" -> {
                            // evalLet(ast, env)
                            val inner = Env(env)
                            val bind = ast.at(1) as? RuriSequence ?: throw Exception("Expected sequence")
                            val iter = bind.iterator()
                            while (iter.hasNext()) {
                                val key = iter.next()
                                if (!iter.hasNext()) throw Exception("Let should have even number parameter")

                                val value = eval(iter.next(), inner)
                                inner[(key as SymbolType).value] = value
                            }
                            env = inner
                            ast = ast.at(2)
                        }
                        "fn*" -> {
                            return evalFn(ast, env)
                        }
                        "do" -> {
                            evalDo(ast, env)
                        }
                        "if" -> {
                            // evalIf(ast, env)
                            val condition = eval(ast.at(1), env)
                            if (condition !is FalseType && condition !is NilType) {
                                ast = ast.at(2)
                            } else if (ast.len() > 3) {
                                ast = ast.at(3)
                            } else {
                                return NilType()
                            }
                        }
                        else -> {
                            val evaluated = evalAst(ast, env) as RuriSequence
                            val result = evaluated.first() as? FunctionType ?: throw NonFunctionException("${evaluated.first()}")
                            when (result) {
                                is FnFunctionType -> {
                                    ast = result.ast
                                    env = Env(result.env, result.params, evaluated.rest().elements)
                                }
                                is FunctionType -> return result.apply(evaluated.rest())
                                else -> throw NonFunctionException("$result")
                            }
                        }
                    }

            }
        } else {
            return evalAst(ast, env)
        }
    }
}

fun evalFn(ast: RuriList, env: Env): RuriType {
    val bindList = ast.at(1) as? RuriSequence ?: throw RuriException("fn* needs a bind list as first parameter")
    val symbols = bindList.elements.filterIsInstance<SymbolType>()
    val exprs = ast.at(2)
    return FnFunctionType(exprs, symbols, env, {
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
    val first = evaluated.first() as? FunctionType ?: throw NonFunctionException("${evaluated.first()}")
    return first.apply(evaluated.rest())
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
