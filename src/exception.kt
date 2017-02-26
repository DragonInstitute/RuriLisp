open class RuriException(message: String) : Exception(message)

class EOFException(message: String) : RuriException(message)
class UnrecognizedException(message: String) : RuriException(message)
class NonFunctionException(message: String) : RuriException(message)
class NotFoundException(message: String) : RuriException(message)

