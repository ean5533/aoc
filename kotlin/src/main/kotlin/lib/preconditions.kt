package lib

fun checkEquals(a: Any, b: Any, additionalMessage: () -> String = { "" }) =
  check(a == b) { "[$a] was not equal to [$b] (${additionalMessage()})" }
