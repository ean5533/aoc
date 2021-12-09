Wherein an engineer (who primarily uses [Kotlin](https://github.com/ean5533/aoc-2021/kotlin), Java, Scala and C#) tries to teach themselves Go by solving [Advent of Code](https://adventofcode.com/) challenges. It's... not pretty.

Insights gained:

1. Go is not a functional or semi-functional language. Don't try to treat it like one.
2. Yeah, Go doesn't have generics, as we all know. And yes, despite the apologists, it really is painful.
3. Go's standard library is **extremely** spartan. Expect to write a lot of functions that you'd expect to be in the standard lib.
4. Because of these design choices, Go is not an ideal language choice for solving Advent of Code challenges, which often center around manipulating collections.

P.S. I used some pretty hacky BS to make a virtual GOPATH here which is managed for me intellij. It's not ideal, but it makes it possible to colocate Go and non-Go code without needing to move everything under some magic Go-specific system path.