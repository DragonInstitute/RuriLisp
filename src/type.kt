import java.util.*

interface RuriType

open class AtomType : RuriType

open class BooleanType : AtomType()
class TrueType : BooleanType(){
    override fun equals(other: Any?): Boolean {
        return true
    }
}
class FalseType : BooleanType(){
    override fun equals(other: Any?): Boolean {
        return true
    }
}

open class NilType : AtomType(){
    override fun equals(other: Any?): Boolean {
        return true
    }
}
open class ValuedType<T>(var value: T) : AtomType() {
    override fun toString(): String {
        return value.toString()
    }
}

class SymbolType(value: String) : ValuedType<String>(value)

class IntegerType(value: Long) : ValuedType<Long>(value) {
    operator fun plus(a: IntegerType) = IntegerType(this.value + a.value)
    operator fun minus(a: IntegerType) = IntegerType(this.value - a.value)
    operator fun times(a: IntegerType) = IntegerType(this.value * a.value)
    operator fun div(a: IntegerType) = IntegerType(this.value / a.value)
}

class StringType(value: String) : ValuedType<String>(value)

class KeywordType(value: String) : ValuedType<String>(value)

interface IRuriLambda : RuriType {
    fun apply(seqence: Sequence<RuriType>): RuriType
}

open class FunctionType(val lambda: (RuriList) -> RuriType) : RuriType {
    var isMacro: Boolean = false
    fun apply(seq: RuriList): RuriType = lambda(seq)
    override fun toString(): String {
        return "$lambda"
    }
}

class FnFunctionType(val ast: RuriType, val params:List<SymbolType>, val env:Env, lambda:(RuriList) -> RuriType) :
        FunctionType(lambda)