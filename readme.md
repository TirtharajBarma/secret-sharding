# Secret Sharing Solution - Shamir's Secret Sharing Implementation

This Java program implements a simplified version of **Shamir's Secret Sharing** algorithm to reconstruct the constant term of an unknown polynomial from encoded roots.

## Problem Overview

The program reconstructs the **constant term `c`** of an unknown polynomial of degree **m**, given **k = m + 1** encoded roots. Each root is provided in JSON format with:
- `x`: integer key of the root
- `y`: string representing a number encoded in a specific base
- `base`: the base system used for encoding the y-value

## Features

- **No External Dependencies**: Built-in JSON parser, no external libraries required
- **Large Number Support**: Uses `BigInteger` for handling up to 256-bit numbers
- **Base Conversion**: Supports conversion from any base (2-36) to decimal
- **Robust Algorithm**: Tests all combinations of k points to handle invalid shares
- **Frequency Analysis**: Identifies the correct secret by finding the most frequent result

## Algorithm

1. **JSON Parsing**: Reads and parses JSON input file
2. **Base Conversion**: Converts y-values from their specified base to decimal
3. **Combination Generation**: Creates all possible combinations of k points
4. **Lagrange Interpolation**: Uses Lagrange interpolation to find f(0) for each combination
5. **Secret Detection**: Identifies the most frequently occurring constant term as the secret

## File Structure

```
secret-sharing-project/
├── SecretSharing.java    # Main implementation
├── testcase1.json       # Sample test case 1
├── testcase2.json       # Sample test case 2
└── README.md           # This file
```

## Prerequisites

- **Java Development Kit (JDK)** 8 or higher
- No external libraries required

### Installing Java on Mac

```bash
# Check if Java is installed
java -version
javac -version

# If not installed, install via Homebrew:
brew install openjdk@17

# Or download from Oracle/OpenJDK website
```

## Setup and Usage

### 1. Clone/Download the Project

```bash
mkdir secret-sharing-project
cd secret-sharing-project
```

### 2. Create the Java File

Save the provided Java code as `SecretSharing.java`

### 3. Create Test Files

Create JSON test files with the required format:

**testcase1.json:**
```json
{
    "keys": {
        "n": 4,
        "k": 3
    },
    "1": {
        "base": "10",
        "value": "4"
    },
    "2": {
        "base": "2",
        "value": "111"
    },
    "3": {
        "base": "10",
        "value": "12"
    },
    "6": {
        "base": "4",
        "value": "213"
    }
}
```

### 4. Compile the Program

```bash
javac SecretSharing.java
```

### 5. Run the Program

```bash
# Test case 1
java SecretSharing testcase1.json

# Test case 2
java SecretSharing testcase2.json

# Or any other JSON file
java SecretSharing <path_to_json_file>
```

## Input Format

The JSON input file must contain:

- **keys**: Object with `n` (total roots) and `k` (minimum roots needed)
- **Root entries**: Numbered objects (1, 2, 3, etc.) containing:
  - `base`: String representing the base system (2-36)
  - `value`: String representing the y-value in the specified base

## Output

The program outputs:
1. Parsed values of n and k
2. Converted points showing x, y, base, and original value
3. Number of combinations being tested
4. Secret frequency analysis
5. **Final secret (constant term)**

### Sample Output

```
n = 4, k = 3
Point: x=1, y=4 (base 10 value: 4)
Point: x=2, y=7 (base 2 value: 111)
Point: x=3, y=12 (base 10 value: 12)
Point: x=6, y=39 (base 4 value: 213)
Testing 4 combinations...
Secret frequency analysis:
Secret 3 appears 3 times
Secret 15 appears 1 time
Secret (constant term): 3
```

## Algorithm Details

### Lagrange Interpolation

The program uses Lagrange interpolation to find f(0):

```
f(0) = Σ(i=0 to k-1) yi * Π(j=0 to k-1, j≠i) (-xj) / (xi - xj)
```

### Handling Invalid Shares

- Tests all C(n,k) combinations of points
- Invalid combinations are skipped if they produce non-integer results
- The most frequently occurring secret is selected as the correct answer

## Error Handling

- **File I/O Errors**: Handles missing or unreadable JSON files
- **JSON Parsing Errors**: Robust parsing with error reporting
- **Invalid Base Conversion**: Catches and reports base conversion errors
- **Non-integer Results**: Skips combinations that don't produce integer constants

## Troubleshooting

### Common Issues

1. **"Could not find or load main class"**
   ```bash
   # Make sure you're in the correct directory and file is compiled
   javac SecretSharing.java
   java SecretSharing testcase1.json
   ```

2. **JSON Parsing Errors**
   - Verify JSON syntax is correct
   - Ensure all required fields are present
   - Check that base values are valid (2-36)

3. **Base Conversion Errors**
   - Verify that value strings are valid for their specified base
   - For example, "111" is valid for base 2, but "9" is not

## Mathematical Background

This implementation is based on Shamir's Secret Sharing scheme, which uses polynomial interpolation over finite fields. The key insight is that any polynomial of degree m can be uniquely determined by m+1 points, and evaluating this polynomial at x=0 gives us the constant term (the secret).
