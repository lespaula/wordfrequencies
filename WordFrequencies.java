import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WordFrequencies {

    public static void main(String[] args) throws Exception {

        Path start = Paths.get("YOUR_REPO_START_DIR");

        try (Stream<Path> stream = Files.walk(start, Integer.MAX_VALUE).parallel()) {
            stream
                    .filter(f -> f.toString().endsWith(".java"))
                    .map(getData)
                    .map(toSingleTokens)
                    .flatMap(Collection::stream)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                    .entrySet()
                    .stream()
                    // compare entry.value descending
                    .sorted((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue()))
                    .forEach(entry -> System.out.println(entry.getKey() + "," + entry.getValue()));
        }
    }

    private static Function<Path, String> getData = (path) -> {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            return "NOT_FOUND";
        }
    };

    private static Function<String, List<String>> toSingleTokens = (data) -> {
        StringTokenizer tokenizer = new StringTokenizer(data);
        List<String> tokens = new ArrayList<>();
        // Split string into tokens
        while (tokenizer.hasMoreElements()) {
            // clean all special chars apart form dot and _ to keep imports and variables
            String cleaned = tokenizer.nextToken().replaceAll("[^a-zA-Z0-9._]", "");
            if (!cleaned.isEmpty()) {
                // also split imports to single words now
                if (cleaned.contains(".")) {
                    Arrays.asList(cleaned.split(".")).forEach(tokens::add);
                } else {
                    tokens.add(cleaned);
                }
            }
        }
        return tokens;
    };

}
