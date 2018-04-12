object Extension {

  implicit class RichString(s: String) {
    def toDigit: Double = {
      val str = s.filter(char => Character.isDigit(char) || char == '-' || char == '.')
      if (str.nonEmpty && str != "-") java.lang.Double.parseDouble(str) else 0
    }
  }

}
