package aoc2022.day21

import lib.loadResourceMatchingPackageName
import java.lang.StringBuilder
import kotlin.math.absoluteValue

private val input = loadResourceMatchingPackageName(object {}.javaClass, "text/").trim()

/**
 * There's a boatload of code in here that was designed to solve this problem the "correct" way (actually solving for X) but that failed, I think due to "big number" precision problems (but I'm not really sure).
 * Instead I just solved it via binary search. Still, I left the solving code behind in case I ever decide to come back and try to fix it.
 */
fun main() {
    part1()
    part2()
}

private fun part1() {
    val monkies = parseInput()

    @Suppress("UNCHECKED_CAST")
    val root = monkies["root"] as BinaryNumericExpression
    println("part1: " + root.expandReferences(monkies).evaluate())
}

private fun part2() {
    val monkies = parseInput()

    @Suppress("UNCHECKED_CAST")
    val root = monkies["root"] as BinaryNumericExpression
    val equalityRoot = rewrite(monkies, EqualityExpression(root.left, root.right, "=="), NumericVariable("X"))

    val targetValue = equalityRoot.right.evaluate()
    val realSolution = generateSequence(-targetValue.absoluteValue to targetValue.absoluteValue) { (min, max) ->
        val guess = (max + min) / 2.toDouble()
        val result =
            rewrite(monkies, EqualityExpression(root.left, root.right, "=="), Literal(guess)).left.evaluate()
        when {
            result == targetValue -> null
            result < targetValue -> min to guess
            else -> guess to max
        }
    }.last().let { (it.first + it.second) / 2.toDouble() }

    println("part2 (correct): $realSolution")

    println(equalityRoot.right.evaluate().toString())
    println(equalityRoot.left.toString())
    println(equalityRoot.partiallySimplify())
    println(equalityRoot.fullySimplify())
    println(equalityRoot.partiallySimplify().fullySimplify())
//    @Suppress("UNCHECKED_CAST")
//    println((equalityRoot.partiallySimplify() as EqualityExpression<Double>).unwrapVariable().first.evaluate())
    val solution = equalityRoot.unwrapVariable().second.evaluate()

    println("part2 (incorrect): $solution")

    // REAL SOLUTION: 3876027196185
}

@Suppress("UNCHECKED_CAST")
private fun <T : Expression<*>> rewrite(
    monkies: Map<String, Expression<Double>>,
    newRoot: T,
    newHumanExpression: Expression<Double>,
): T =
    newRoot.expandReferences(monkies + listOf("root" to newRoot, "humn" to newHumanExpression)) as T

private fun parseInput() = input.lines().associate {
    val (name, exprString) = it.split(": ")
    val exprParts = exprString.split(" ")
    val expression = when (exprParts.size) {
        1 -> Literal(exprParts[0].toDouble())
        3 -> BinaryExpression(MonkeyRef(exprParts[0]), MonkeyRef(exprParts[2]), exprParts[1])
        else -> throw IllegalStateException()
    }
    name to expression
}

private sealed interface Expression<T> {
    fun evaluate(): T = (fullySimplify() as Literal<T>).value
    fun fullySimplify(): Expression<T>
    fun partiallySimplify(): Expression<T>
    fun expandReferences(expressions: Map<String, Expression<*>>): Expression<T>
    fun containsVariable(): Boolean
}

private typealias NumericLiteral = Literal<Double>

private data class Literal<T>(val value: T) : Expression<T> {
    override fun fullySimplify(): Expression<T> = this
    override fun expandReferences(expressions: Map<String, Expression<*>>): Expression<T> = this
    override fun containsVariable(): Boolean = false
    override fun partiallySimplify(): Expression<T> = this

    override fun toString(): String = value.toString()
}

private typealias BinaryNumericExpression = BinaryExpression<Double, Double>
private typealias EqualityExpression<T> = BinaryExpression<T, Boolean>

private data class BinaryExpression<T, V>(
    val left: Expression<T>,
    val right: Expression<T>,
    val operator: String,
) : Expression<V> {
    override fun fullySimplify(): Expression<V> {
        val leftSimplified = left.fullySimplify()
        val rightSimplified = right.fullySimplify()
        @Suppress("UNCHECKED_CAST")
        return when {
            leftSimplified is NumericVariable && rightSimplified is Literal ->
                simplifyVariable(leftSimplified, rightSimplified)

            rightSimplified is NumericVariable && leftSimplified is Literal ->
                simplifyVariable(rightSimplified, leftSimplified)

            leftSimplified is Literal && rightSimplified is Literal -> when (operator) {
                "+" -> Literal(leftSimplified.value as Double + rightSimplified.value as Double)
                "-" -> Literal(leftSimplified.value as Double - rightSimplified.value as Double)
                "*" -> Literal(leftSimplified.value as Double * rightSimplified.value as Double)
                "/" -> Literal(leftSimplified.value as Double / rightSimplified.value as Double)
                "==" -> Literal(leftSimplified.value == rightSimplified.value)
                else -> throw IllegalStateException()
            } as Expression<V>

            else -> throw IllegalStateException()
        }
    }

    override fun expandReferences(expressions: Map<String, Expression<*>>): Expression<V> =
        BinaryExpression(left.expandReferences(expressions), right.expandReferences(expressions), operator)

    override fun containsVariable(): Boolean = left.containsVariable() || right.containsVariable()
    override fun partiallySimplify(): Expression<V> = BinaryExpression(
        if (left.containsVariable()) left.partiallySimplify() else left.fullySimplify(),
        if (right.containsVariable()) right.partiallySimplify() else right.fullySimplify(),
        operator
    )

    override fun toString(): String = "($left$operator$right)"

    @Suppress("UNCHECKED_CAST")
    private fun simplifyVariable(variable: NumericVariable, literal: Expression<T>): Expression<V> = when (operator) {
        "+" -> variable + literal.evaluate() as Double
        "-" -> variable - literal.evaluate() as Double
        "*" -> variable * literal.evaluate() as Double
        "/" -> variable / literal.evaluate() as Double
        "==" -> BinaryExpression(variable, literal as Expression<Double>, operator)
        else -> throw IllegalStateException()
    } as Expression<V>

    /**
     * @return The variable expression and the literal expression (in that order)
     */
    fun unwrapVariable(parent: Expression<Double>? = null): Pair<Expression<Double>, Expression<Double>> {
        val (nonVariableExpression, variableExpression, variable) = when(left.containsVariable() to right.containsVariable()) {
            false to true -> Triple(left.fullySimplify(), right, right)
            true to false -> Triple(right.fullySimplify(), left, left)
            else -> throw IllegalStateException()
        }.let {
            @Suppress("UNCHECKED_CAST")
            Triple(
                it.first as NumericLiteral,
                it.second as? BinaryNumericExpression,
                it.third as? NumericVariable
            )
        }

        if (parent == null) return variableExpression!!.unwrapVariable(nonVariableExpression)

        val invertedOperator = when (operator) {
            "+" -> "-"
            "-" -> "+"
            "*" -> "/"
            "/" -> "*"
            else -> throw IllegalStateException()
        }

        val newParent = BinaryNumericExpression(parent, nonVariableExpression, invertedOperator) as Expression<Double>
//        val simplified = BinaryNumericExpression(variableExpression?:variable!! , nonVariableExpression.fullySimplify(), operator) as Expression<Double>
//        println("a: $simplified = ${parent!!.evaluate()}")
//        println("b: ${variableExpression?:variable} = ${newParent.evaluate()}")
        return when {
            variable != null -> 
                variable to newParent
            variableExpression != null -> variableExpression.unwrapVariable(newParent)
            else -> throw IllegalStateException()
        }
    }
}

private data class MonkeyRef(val name: String) : Expression<Double> {
    override fun fullySimplify(): Expression<Double> =
        throw IllegalStateException("Can't evaluate expressions that still contain references")

    override fun expandReferences(expressions: Map<String, Expression<*>>): Expression<Double> =
        lookupExpression(expressions).expandReferences(expressions)

    override fun containsVariable(): Boolean {
        throw IllegalStateException("Can't evaluate expressions that still contain references")
    }

    override fun partiallySimplify(): Expression<Double> {
        throw IllegalStateException("Can't evaluate expressions that still contain references")
    }

    @Suppress("UNCHECKED_CAST")
    private fun lookupExpression(expressions: Map<String, Expression<*>>) =
        expressions[name]!! as Expression<Double>
}

private data class NumericVariable(
    val name: String,
    val mult: Double = 1.0,
    val add: Double = 0.0,
) : Expression<Double> {
    override fun fullySimplify(): Expression<Double> = this
    override fun expandReferences(expressions: Map<String, Expression<*>>): Expression<Double> = this
    override fun containsVariable(): Boolean = true
    override fun partiallySimplify(): Expression<Double> = this

    override fun toString(): String = StringBuilder().also { 
        if(add != 0.0 ) it.append("(")
        if(mult != 1.0) it.append(mult)
        it.append(name)
        if(add != 0.0 ) {
            if (add >= 0.0) it.append("+")
            it.append(add)
        }
        if(add != 0.0 ) it.append(")")
    }.toString()

    operator fun plus(amount: Double) = this.copy(add = add + amount)
    operator fun minus(amount: Double) = this.copy(add = add - amount)
    operator fun times(amount: Double) = this.copy(mult = mult * amount, add = add * amount)
    operator fun div(amount: Double) = this.copy(mult = mult / amount, add = add / amount)
}
