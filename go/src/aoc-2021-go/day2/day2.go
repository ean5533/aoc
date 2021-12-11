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
	commands := parseInput()

	part1(commands)
	part2(commands)
}

func part1(commands []Command) {
	var horizontal = 0
	var vertical = 0

	for _, command := range commands {
		switch command.direction {
		case "forward":
			horizontal += command.magnitude
		case "up":
			vertical -= command.magnitude
		case "down":
			vertical += command.magnitude
		}
	}

	var final = horizontal * vertical

	fmt.Printf("Part1: final position %d\n", final)
}

func part2(commands []Command) {
	var horizontal = 0
	var vertical = 0
	var aim = 0

	for _, command := range commands {
		switch command.direction {
		case "forward":
			horizontal += command.magnitude
			vertical += command.magnitude * aim
		case "up":
			aim -= command.magnitude
		case "down":
			aim += command.magnitude
		}
	}

	var final = horizontal * vertical

	fmt.Printf("Part2: final position %d\n", final)
}

func parseInput() []Command {
	var commands []Command

	f, err := fEmbed.Open("input.txt")
	Check(err)

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		var line = scanner.Text()
		split := strings.Split(line, " ")

		direction := split[0]
		magnitudeS := split[1]
		var magnitude, err = strconv.Atoi(magnitudeS)
		Check(err)

		commands = append(commands, Command{direction, magnitude})
	}

	return commands
}

type Command struct {
	direction string
	magnitude int
}
