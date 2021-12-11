pub mod day3 {
    pub fn day3() {
        let numbers = parse_input();

        part1(numbers.as_slice());
    }

    fn part1(numbers: &[Vec<u32>]) {
        let width = numbers[0].len();
        let height = numbers.len();

        let gamma_bits = (0..width).map(|col| {
            let sum = (0..height).map(|row| {
                numbers[row][col]
            }).sum::<u32>();
            if sum >= (height / 2) as u32 { 1 } else { 0 }
        }).collect::<Vec<_>>();
        let epsilon_bits = gamma_bits.iter().map(|bit| bit ^ 1);

        let gamma = u32::from_str_radix(gamma_bits.iter().map(|bit| bit.to_string()).collect::<String>().as_str(), 2).unwrap();
        let epsilon = u32::from_str_radix(epsilon_bits.map(|bit| bit.to_string()).collect::<String>().as_str(), 2).unwrap();
        let multiplied = gamma * epsilon;

        println!("Part 1: gamma {} epsilon {} multiplied {}", gamma, epsilon, multiplied)
    }

    fn parse_input() -> Vec<Vec<u32>> {
        let bytes = include_bytes!("../assets/day3.txt");
        let string = String::from_utf8_lossy(bytes);
        string.lines()
            .map(|line| { line.chars().map(|char| char.to_digit(10).unwrap() as u32).collect::<Vec<u32>>() })
            .collect::<Vec<Vec<u32>>>()
    }
}

#[cfg(test)]
mod tests {
    use crate::day3;

    #[test]
    fn run_it() {
        day3()
    }
}