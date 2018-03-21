object Extension {

  implicit class RichString(s: String) {
    def toDigit: Double = java.lang.Double.parseDouble(s.filter(char => Character.isDigit(char) || char == '.'))
  }

}
