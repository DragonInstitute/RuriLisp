import java.util.*

interface RuriType

open class AtomType : RuriType

open class NilType : AtomType()

open class BooleanType : AtomType()
class TrueType : BooleanType()
class FalseType : BooleanType()

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

class FunctionType(val lambda: (RuriList) -> RuriType) : RuriType {
    fun apply(seq: RuriList): RuriType = lambda(seq)
    override fun toString(): String {
        return "$lambda"
    }
}

