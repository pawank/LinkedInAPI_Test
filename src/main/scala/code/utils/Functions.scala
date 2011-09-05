package code.utils


object GeneralUtils { 

  def getCaseInsensitiveRegex(input: String):String = "(?i)[a-zA-Z]*" + input + "[a-zA-Z]*"
}
