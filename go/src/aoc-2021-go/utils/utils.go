package utils

func Check(err error) {
	if err != nil {
		panic(err)
	}
}

func Zip(a, b []int) []intPair {
	length := min(len(a), len(b))

	zipped := make([]intPair, length, length)

	for i := 0; i < length; i++ {
		zipped[i] = intPair{a[i], b[i]}
	}

	return zipped
}

func min(a, b int) int {
	if a < b {
		return a
	} else {
		return b
	}
}

type intPair struct {
	A, B int
}
