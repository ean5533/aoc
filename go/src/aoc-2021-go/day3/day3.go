package main

import (
	. "aoc-2021-go/utils"
	"bufio"
	"embed"
	"fmt"
	"strconv"
	"strings"
)

//go:embed input.txt
var fEmbed embed.FS

func main() {
	numbers := parseInput()

	part1(numbers)
	part2(numbers)
}

func part1(numbers [][]int) {
	width := len(numbers[0])
	height := len(numbers)

	var gammaBits string
	var epsilonBits string

	for col := 0; col < width; col++ {
		var sum = 0
		for row := 0; row < height; row++ {
			sum += numbers[row][col]
		}

		if sum >= height/2 {
			gammaBits += "1"
			epsilonBits += "0"
		} else {
			gammaBits += "0"
			epsilonBits += "1"
		}
	}

	gamma, _ := strconv.ParseInt(gammaBits, 2, 0)
	epsilon, _ := strconv.ParseInt(epsilonBits, 2, 0)
	total := gamma * epsilon

	fmt.Printf("Part1: gamma %d epsilon %d total %d\n", gamma, epsilon, total)
}

func part2(numbers [][]int) {
	//total := gamma * epsilon
	//
	//fmt.Printf("Part2: gamma %d epsilon %d total %d\n", gamma, epsilon, total)
}

func parseInput() [][]int {
	var numbers [][]int

	f, err := fEmbed.Open("input.txt")
	Check(err)

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		var numberLine []int

		var line = scanner.Text()
		split := strings.Split(line, "")

		for _, char := range split {
			num, _ := strconv.Atoi(char)
			numberLine = append(numberLine, num)
		}

		numbers = append(numbers, numberLine)
	}

	return numbers
}
