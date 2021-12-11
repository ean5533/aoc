pub mod day2 {
    use crate::day2::Command;

    pub fn day2() {
        let commands = parse_input();

        part1(commands.as_slice());
        part2(commands.as_slice());
    }

    fn part1(commands: &[Command]) {
        let final_status = commands.iter().fold(Status1 { x: 0, y: 0 }, |status, command| {
            match command.direction.as_str() {
                "forward" => Status1 { x: status.x + command.magnitude, y: status.y },
                "up" => Status1 { x: status.x, y: status.y - command.magnitude },
                "down" => Status1 { x: status.x, y: status.y + command.magnitude },
                _ => panic!("Unexpected direction {}", command.direction)
            }
        });

        let multiplied = final_status.x * final_status.y;

        println!("Part 1: x={} y={} multiplied={}", final_status.x, final_status.y, multiplied)
    }

    struct Status1 {
        x: i32,
        y: i32,
    }

    fn part2(commands: &[Command]) {
        let final_status = commands.iter().fold(Status2 { x: 0, y: 0, aim: 0 }, |status, command| {
            match command.direction.as_str() {
                "forward" => Status2 { x: status.x + command.magnitude, y: status.y + command.magnitude * status.aim, aim: status.aim },
                "up" => Status2 { x: status.x, y: status.y, aim: status.aim - command.magnitude },
                "down" => Status2 { x: status.x, y: status.y, aim: status.aim + command.magnitude },
                _ => panic!("Unexpected direction {}", command.direction)
            }
        });

        let multiplied = final_status.x * final_status.y;

        println!("Part 2: x={} y={} aim={} multiplied={}", final_status.x, final_status.y, final_status.aim, multiplied)
    }

    struct Status2 {
        x: i32,
        y: i32,
        aim: i32,
    }

    fn parse_input() -> Vec<Command> {
        let bytes = include_bytes!("../assets/day2.txt");
        let string = String::from_utf8_lossy(bytes);
        string.lines().map(|line| {
            let pieces = line.split(" ").collect::<Vec<_>>();
            Command { direction: pieces[0].to_string(), magnitude: pieces[1].parse::<i32>().unwrap() }
        }).collect::<Vec<Command>>()
    }
}

struct Command {
    direction: String,
    magnitude: i32,
}

#[cfg(test)]
mod tests {
    use crate::day2;

    #[test]
    fn run_it() {
        day2()
    }
}