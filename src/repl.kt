import java.util.*

fun main(args: Array<String>) {
    val standardEnv = dynamicLoadCore()

    while (true) {
        prompt()
        try {
            println(rep(r(), standardEnv))
        } catch(e: RuriContinue) {
        } catch (e: NotFoundException) {
            println(e.message)
        } catch(e: NonFunctionException) {
            println(e.message)
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }
}

fun r(): String = readLine() ?: throw EOFException()


fun read(code: String?): RuriType {
    return readStr(code)
}

fun eval(_ast: RuriType, _env: Env): RuriType {
    var ast = _ast
    var env = _env
    while (true) {
        ast = macroExpand(ast, env)

        if (ast is RuriList) {
            if (ast.len() == 0) {
                return ast
            } else {
                val first = ast.first()
                when ((first as? SymbolType)?.value) {
                    "def!" -> {
                        return evalDefine(ast, env)
                    }
                    "let*" -> {
                        // evalLet(ast, env)
                        val inner = Env(env)
                        val bind = ast.at(1) as? RuriSequence ?: throw RuriException("Expected sequence")
                        val iter = bind.iterator()
                        while (iter.hasNext()) {
                            val key = iter.next()
                            if (!iter.hasNext()) throw RuriException("Let should have even number parameter")

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
                    "quote" -> {
                        return evalQuote(ast, env)
                    }
                    "quasiquote" -> {
                        ast = evalQuasiQuote(ast.at(1))
                    }
                    "defmacro!" -> return defMacro(ast, env)
                    "macroexpand" -> return macroExpand(ast.at(1), env)
                    "try*" -> return tryCatch(ast, env)
                    else -> {
                        val evaluated = evalAst(ast, env) as RuriSequence
                        val result = evaluated.first()
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

fun tryCatch(ast: RuriList, env: Env): RuriType {
    try {
        return eval(ast.at(1), env)
    } catch (e: Exception) {
        val thrown = e as? RuriException ?: RuriException(e.message)
        val catch = (ast.at(2) as RuriList)
        if ((catch.first() as? SymbolType)?.value != "catch*") {
            println("Syntax error: expect symbol catch* but got ${catch.first()}")
            println("But concrete syntax doesn't matter, yeah!")
        }
        val symbol = catch.at(1) as SymbolType

        val catchBody = (ast.at(2) as RuriList).at(2)
        val catchEnv = Env(env)
        catchEnv[symbol.value] = thrown

        return eval(catchBody, catchEnv)
    }
}

fun isMacro(ast: RuriType, env: Env): Boolean {
    val astNodes = ast as? RuriList ?: return false
    if (astNodes.len() <= 0) return false
    val symbol = astNodes.first() as? SymbolType ?: return false
    val function = env[symbol.value] as? FunctionType ?: return false
    return function.isMacro
}

fun defMacro(ast: RuriList, env: Env): RuriType {
    val macro = eval(ast.at(2), env) as FunctionType
    macro.isMacro = true
    env[(ast.at(1) as SymbolType).value] = macro
    return macro
}

fun macroExpand(_ast: RuriType, env: Env): RuriType {
    var ast = _ast
    while (isMacro(ast, env)) {
        val symbol = (ast as RuriList).first() as SymbolType
        val function = env[symbol.value] as FunctionType
        ast = function.apply(ast.rest())
    }
    return ast
}

fun isPair(target: RuriType) = (target as? RuriList)?.elements?.any() ?: false

fun evalQuasiQuote(ast: RuriType): RuriType {
    if (!isPair(ast)) {
        val re = RuriList()
        re.add(SymbolType("quote"))
        re.add(ast)
        return re
    }

    val sequence = ast as RuriList
    val first = sequence.first()
    if ((first as? SymbolType)?.value == "unquote") {
        return sequence.at(1)
    }

    if (isPair(first)
            && ((first as RuriList).first() as? SymbolType)?.value == "splice-unquote") {
        val splice = RuriList()
        splice.add(SymbolType("concat"))
        splice.add(first.at(1))
        splice.add(evalQuasiQuote(RuriList(sequence.elements.drop(1).toCollection(LinkedList<RuriType>()))))
        return splice
    }

    val cons = RuriList()
    cons.add(SymbolType("cons"))
    cons.add(evalQuasiQuote(ast.first()))
    cons.add(evalQuasiQuote(RuriList(sequence.elements.drop(1).toCollection(LinkedList<RuriType>()))))
    return cons
}

fun evalQuote(ast: RuriList, env: Env) = ast.at(1)

fun evalFn(ast: RuriList, env: Env): RuriType {
    val bindList = ast.at(1) as? RuriSequence ?: throw RuriException("fn* needs a bind list as first parameter")
    val params = bindList.elements.filterIsInstance<SymbolType>()
    val exprs = ast.at(2)
    return FnFunctionType(exprs, params, env, {
        s: RuriSequence ->
        // virtual env binding
        eval(exprs, Env(env, params, s.elements))
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
    val bind = ast.at(1) as? RuriSequence ?: throw RuriException("Expected sequence")
    val iter = bind.iterator()
    while (iter.hasNext()) {
        val key = iter.next()
        if (!iter.hasNext()) throw RuriException("Let should have even number parameter")

        val value = eval(iter.next(), inner)
        inner[(key as SymbolType).value] = value
    }
    return eval(ast.at(2), inner)
}

fun evalAst(ast: RuriType, env: Env): RuriType {
    when (ast) {
    // reduce
        is SymbolType -> return env[ast.value] ?: NotFoundException(ast.value)
        is RuriList -> return ast.elements.fold(RuriList(), { acc, x -> acc.add(eval(x, env)); acc })
        is RuriVector -> return ast.elements.fold(RuriVector(), { acc, x -> acc.add(eval(x, env)); acc })
        is RuriHashMap -> return ast.elements.entries.fold(RuriHashMap(), { a, b -> a.add(b.key, eval(b.value, env)); a })
        else -> return ast
    }
}

fun p(s: RuriType) {
    when (s) {
        // TODO: Need pretty printer but who cares
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
        else -> println("$s")
    }
}

fun dynamicRun(input: String, env: Env) {
    p(eval(read(input), env))
}

fun rep(s: String, env: Env) = p(eval(read(s), env))

fun prompt(text: String = "Ruri> ") {
    print(text)
}
