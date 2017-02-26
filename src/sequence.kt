import java.util.*

interface IRuriSequence : RuriType {
    fun iterator(): Iterator    <RuriType>
    fun at(n: Int): RuriType
    fun len(): Int
    fun first(): RuriType
    fun rest(): IRuriSequence
    fun add(ele: RuriType)
    fun remove(n: Int): RuriType
}

abstract class RuriSequence(var elements: MutableList<RuriType>) : IRuriSequence {
    override fun iterator() = elements.asSequence().iterator()
    override fun at(n: Int) = elements.elementAt(n)
    override fun len() = elements.count()
    override fun first() = at(0)
    override fun rest() = RuriList(elements.drop(1).toCollection(LinkedList<RuriType>()))
    override fun remove(n: Int) = elements.removeAt(n)

    override fun add(ele: RuriType) {
        elements.add(ele)
    }
}

class RuriList(elements: LinkedList<RuriType>) : RuriSequence(elements) {
    // List -> LinkedList
    constructor() : this(LinkedList<RuriType>())

    override fun toString(): String {
        var re = "("
        elements.forEach { re += "$it, " }
        re = re.dropLast(2)
        re += ")"
        return re
    }
}

class RuriVector(elements: ArrayList<RuriType>) : RuriSequence(elements) {
    // Vector -> ArrayList
    constructor() : this(ArrayList<RuriType>())

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