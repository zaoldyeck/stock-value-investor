object Extension {

  implicit class RichString(s: String) {
    def toDigit: Double = {
      s.con
      val str = s.filter(char => Character.isDigit(char) || char == '.')
      if (str.nonEmpty) java.lang.Double.parseDouble(str) else 0
    }
  }

}
