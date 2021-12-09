package main

import (
	. "aoc-2021-go/utils"
	"bufio"
	"embed"
	"fmt"
	"strconv"
)

//go:embed input.txt
var fEmbed embed.FS

func main() {
	numbers := parseInput()

	part1(numbers)
	part2(numbers)
}

func part1(numbers []int) {
	var increases = 0

	pairs := Zip(numbers, numbers[1:])
	for _, pair := range pairs {
		if pair.A < pair.B {
			increases++
		}
	}

	fmt.Printf("Part1: There were %d increases\n", increases)
}

func part2(numbers []int) {
	// Insight: there is no need to actually calculate the sliding window of 3 values. When comparing (a, b, c) to (b, c, d),
	// we know that the difference between the two is just -a+d. Therefore, the second 3-window tuple is larger iff (d>a).
	// Therefore, we can use an identical approach as part one, just zipping with n+4 instead of n+1.

	var increases = 0

	for _, pair := range Zip(numbers, numbers[3:]) {
		if pair.A < pair.B {
			increases++
		}
	}

	fmt.Printf("Part2: There were %d increases\n", increases)
}

func parseInput() []int {
	var numbers []int

	f, err := fEmbed.Open("input.txt")
	Check(err)

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		var line = scanner.Text()
		var value, err = strconv.Atoi(line)
		Check(err)

		numbers = append(numbers, value)
	}
	return numbers
}
