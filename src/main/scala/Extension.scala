object Extension {

  implicit class RichOption(someString: Option[String]) {
    def toDigit: Double = {
      someString match {
        case Some(s) =>
          val str = s.filter(char => Character.isDigit(char) || char == '-' || char == '.')
          if (str.nonEmpty && str != "-") java.lang.Double.parseDouble(str) else 0
        case None => 0
      }
    }
  }

}
