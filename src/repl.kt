fun main(args: Array<String>) {
    rep(commonEnv)
}

fun r(): String? {
    return readLine()
}

fun read(code: String?): Type {
    return readStr(code)
}

fun eval(ast: Type, env: Env): Type {
    if (ast is NodeList && ast.len() > 0) {
        val evaluated = eval_ast(ast, env) as NodeList
        if (evaluated.first() !is FunctionType) {
            error("Execute non-function")
        } else {
            return (evaluated.first() as FunctionType).apply(evaluated.rest())
        }
    } else {
        return eval_ast(ast, env)
    }
}

fun eval_ast(ast: Type, env: Env) : Type {
    when (ast) {
        is SymbolType -> return env[ast.value] ?: error("${ast.value} not found")
        is NodeList -> return ast.elements.fold(NodeList(), { acc, x -> acc.add(eval(x, env)); acc })

        else -> {return ast}
    }
}

fun p(s: Type) {
    when (s) {
        is NilType -> println("Nil")
        is IntegerType -> println("Integer $s")
        is FalseType -> println("False")
        is TrueType -> println("True")
        is SymbolType -> println("Symbol $s")
        is StringType -> println("String $s")
        is KeywordType -> println("Keyword $s")
        is NodeList -> println("List $s")
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
