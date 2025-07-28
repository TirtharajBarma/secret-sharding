import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.*;

public class SecretSharing {
    
    public static class Point {
        public BigInteger x;
        public BigInteger y;
        
        public Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java SecretSharing <json_file_path>");
            return;
        }
        
        String jsonFilePath = args[0];
        
        try {
            // Read JSON file content
            String jsonContent = readFile(jsonFilePath);
            
            // Parse JSON manually
            Map<String, Object> jsonData = parseJson(jsonContent);
            
            // Extract n and k
            @SuppressWarnings("unchecked")
            Map<String, Object> keys = (Map<String, Object>) jsonData.get("keys");
            int n = ((Number) keys.get("n")).intValue();
            int k = ((Number) keys.get("k")).intValue();
            
            System.out.println("n = " + n + ", k = " + k);
            
            // Extract points
            List<Point> points = new ArrayList<>();
            
            for (Map.Entry<String, Object> entry : jsonData.entrySet()) {
                String key = entry.getKey();
                if (!key.equals("keys")) {
                    int x = Integer.parseInt(key);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> pointData = (Map<String, Object>) entry.getValue();
                    
                    int base = Integer.parseInt((String) pointData.get("base"));
                    String value = (String) pointData.get("value");
                    
                    // Convert from given base to BigInteger
                    BigInteger y = new BigInteger(value, base);
                    points.add(new Point(BigInteger.valueOf(x), y));
                    
                    System.out.println("Point: x=" + x + ", y=" + y + " (base " + base + " value: " + value + ")");
                }
            }
            
            // Find the secret using all combinations of k points
            BigInteger secret = findSecret(points, k);
            System.out.println("Secret (constant term): " + secret);
            
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    private static Map<String, Object> parseJson(String json) {
        // Remove whitespace and newlines
        json = json.replaceAll("\\s+", "");
        
        // Remove outer braces
        json = json.substring(1, json.length() - 1);
        
        Map<String, Object> result = new HashMap<>();
        
        // Split by commas, but be careful about nested objects
        List<String> parts = splitJsonParts(json);
        
        for (String part : parts) {
            String[] keyValue = splitKeyValue(part);
            String key = keyValue[0].replaceAll("\"", "");
            String value = keyValue[1];
            
            if (value.startsWith("{")) {
                // Nested object
                result.put(key, parseJson(value));
            } else if (value.startsWith("\"")) {
                // String value
                result.put(key, value.replaceAll("\"", ""));
            } else {
                // Numeric value
                try {
                    result.put(key, Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    result.put(key, value);
                }
            }
        }
        
        return result;
    }
    
    private static List<String> splitJsonParts(String json) {
        List<String> parts = new ArrayList<>();
        int braceCount = 0;
        int start = 0;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
            } else if (c == ',' && braceCount == 0) {
                parts.add(json.substring(start, i));
                start = i + 1;
            }
        }
        parts.add(json.substring(start));
        
        return parts;
    }
    
    private static String[] splitKeyValue(String part) {
        int colonIndex = part.indexOf(':');
        return new String[] {
            part.substring(0, colonIndex),
            part.substring(colonIndex + 1)
        };
    }
    
    public static BigInteger findSecret(List<Point> points, int k) {
        Map<BigInteger, Integer> secretCounts = new HashMap<>();
        
        // Generate all combinations of k points
        List<List<Point>> combinations = generateCombinations(points, k);
        
        System.out.println("Testing " + combinations.size() + " combinations...");
        
        for (List<Point> combination : combinations) {
            try {
                BigInteger secret = lagrangeInterpolation(combination);
                secretCounts.put(secret, secretCounts.getOrDefault(secret, 0) + 1);
            } catch (Exception e) {
                // Skip invalid combinations
                continue;
            }
        }
        
        // Find the most frequent secret
        BigInteger mostFrequentSecret = null;
        int maxCount = 0;
        
        System.out.println("Secret frequency analysis:");
        for (Map.Entry<BigInteger, Integer> entry : secretCounts.entrySet()) {
            System.out.println("Secret " + entry.getKey() + " appears " + entry.getValue() + " times");
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequentSecret = entry.getKey();
            }
        }
        
        return mostFrequentSecret;
    }
    
    public static List<List<Point>> generateCombinations(List<Point> points, int k) {
        List<List<Point>> combinations = new ArrayList<>();
        generateCombinationsHelper(points, k, 0, new ArrayList<>(), combinations);
        return combinations;
    }
    
    private static void generateCombinationsHelper(List<Point> points, int k, int start, 
                                                 List<Point> current, List<List<Point>> combinations) {
        if (current.size() == k) {
            combinations.add(new ArrayList<>(current));
            return;
        }
        
        for (int i = start; i < points.size(); i++) {
            current.add(points.get(i));
            generateCombinationsHelper(points, k, i + 1, current, combinations);
            current.remove(current.size() - 1);
        }
    }
    
    public static BigInteger lagrangeInterpolation(List<Point> points) {
        BigInteger result = BigInteger.ZERO;
        
        for (int i = 0; i < points.size(); i++) {
            Point pi = points.get(i);
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;
            
            for (int j = 0; j < points.size(); j++) {
                if (i != j) {
                    Point pj = points.get(j);
                    // For f(0), we want (0 - x_j) / (x_i - x_j)
                    numerator = numerator.multiply(pj.x.negate()); // 0 - x_j = -x_j
                    denominator = denominator.multiply(pi.x.subtract(pj.x)); // x_i - x_j
                }
            }
            
            // Calculate the Lagrange basis polynomial at x=0
            // L_i(0) = numerator / denominator
            BigInteger term = pi.y.multiply(numerator);
            
            // Since we're working with integers, we need to ensure exact division
            if (!term.remainder(denominator).equals(BigInteger.ZERO)) {
                throw new RuntimeException("Non-integer result in Lagrange interpolation");
            }
            
            term = term.divide(denominator);
            result = result.add(term);
        }
        
        return result;
    }
}