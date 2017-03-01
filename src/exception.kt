open class RuriException(message: String?) : Exception(message), RuriType

class UnrecognizedException(message: String?) : RuriException("Unrecognized $message")
class NonFunctionException(message: String?) : RuriException("$message is not a function")
class NotFoundException(message: String?) : RuriException("Expected $message but not found")

open class UnexpectedException(message: String?, expect: String = "") :
        RuriException("Unexpected $message" + if (expect == "") "" else ", expect $expect")

class EOFException(expect: String = "") : UnexpectedException("EOF", expect)
class RuriContinue : RuriException(null)