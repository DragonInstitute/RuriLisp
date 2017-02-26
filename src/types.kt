import java.util.*

interface Type

open class AtomType : Type

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

interface ILambda : Type {
    fun apply(seqence: Sequence<Type>): Type
}

class FunctionType(val lambda: (NodeList) -> Type) : Type {
    fun apply(seq: NodeList): Type = lambda(seq)
}

class NodeList(var elements: LinkedList<Type>) : ValuedType<LinkedList<Type>>(elements) {
    constructor() : this(LinkedList<Type>())

    fun at(n: Int) = elements.elementAt(n)
    fun len() = elements.count()

    fun first() = at(0)

    fun rest() = NodeList(elements.drop(1).toCollection(LinkedList<Type>()))


    fun addFirst(ele: Type) {
        elements.addFirst(ele)
    }

    fun add(ele: Type) {
        elements.add(ele)
    }

    override fun toString(): String {
        var re = "("
        elements.forEach { re += it;re += " "; }
        re = re.dropLast(1)
        re += ")"
        return re
    }
}

