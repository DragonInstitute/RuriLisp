import java.util.*

interface IRuriSequence<T> : RuriType {
    fun iterator(): Iterator    <T>
    fun at(n: Int): T
    fun len(): Int
    fun first(): T
    fun rest(): IRuriSequence<T>
    fun add(ele: T)
    fun remove(n: Int): T
}

abstract class RuriSequence<T>(var elements: MutableList<T>) : IRuriSequence<T> {
    override fun iterator() = elements.asSequence().iterator()
    override fun at(n: Int) = elements.elementAt(n)
    override fun len() = elements.count()
    override fun first() = at(0)
    override fun remove(n: Int) = elements.removeAt(n)

    override fun add(ele: T) {
        elements.add(ele)
    }
}

class RuriList(elements: LinkedList<RuriType>) : RuriSequence<RuriType>(elements) {
    // List -> LinkedList
    constructor() : this(LinkedList<RuriType>())

    override fun rest() = RuriList(elements.drop(1).toCollection(LinkedList<RuriType>()))
    override fun toString(): String {
        var re = "("
        elements.forEach { re += "$it, " }
        re = re.dropLast(2)
        re += ")"
        return re
    }
}

class RuriVector(elements: ArrayList<RuriType>) : RuriSequence<RuriType>(elements) {
    // Vector -> ArrayList
    constructor() : this(ArrayList<RuriType>())

    override fun rest() = RuriVector(elements.drop(1).toCollection(ArrayList<RuriType>()))
    override fun toString(): String {
        var re = "["
        elements.forEach { re += "$it, " }
        re = re.dropLast(2)
        re += "]"
        return re
    }
}

class RuriHashMap(var elements: HashMap<StringType, RuriType>) : RuriType {
    constructor() : this(HashMap<StringType, RuriType>())

    fun add(key: StringType, value: RuriType) = elements.put(key, value)
    fun remove(key: StringType) = elements.remove(key)
    override fun equals(other: Any?) = other is RuriHashMap && elements == other.elements
    override fun hashCode(): Int = elements.hashCode()

    override fun toString(): String = elements.toString()
}