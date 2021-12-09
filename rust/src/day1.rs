pub mod day1 {
    pub fn day1() {
        let numbers = parse_input();

        part1(numbers.as_slice());
        part2(numbers.as_slice());
    }

    fn part1(numbers: &[i32]) {
        let increases = numbers.iter().zip(numbers.iter().skip(1)).filter(|(a, b)| a < b).count();
        println!("Part 1: There were {} increases", increases)
    }

    fn part2(numbers: &[i32]) {
        // Insight: there is no need to actually calculate the sliding window of 3 values. When comparing (a, b, c) to (b, c, d),
        // we know that the difference between the two is just -a+d. Therefore, the second 3-window tuple is larger iff (d>a).
        // Therefore, we can use an identical approach as part one, just zipping with n+3 instead of n+1.

        let increases = numbers.iter().zip(numbers.iter().skip(3)).filter(|(a, b)| a < b).count();
        println!("Part 2: There were {} increases", increases)
    }

    fn parse_input() -> Vec<i32> {
        let bytes = include_bytes!("../assets/day1.txt");
        let string = String::from_utf8_lossy(bytes);
        string.lines().map(|line| line.parse::<i32>().unwrap()).collect::<Vec<i32>>()
    }
}

#[cfg(test)]
mod tests {
    use crate::day1;

    #[test]
    fn run_it() {
        day1()
    }
}