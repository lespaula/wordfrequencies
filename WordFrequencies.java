import static java.util.Spliterators.spliteratorUnknownSize;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class WordFrequencies {

    static final String rootPath = "ROOT_DIR";
    static final String outputFilename = "frequencies.csv";
    static final Predicate<Path> fileExtensionFilter = path -> path.toString().endsWith(".java");
    static final Predicate<Entry<String, Long>> allAboveScore = entry -> entry.getValue() > 3;

    public static void main(String[] args) throws Exception {
        PrintStream out = new PrintStream(new File(outputFilename));

        long start = System.currentTimeMillis();
        Files.walk(Paths.get(rootPath), Integer.MAX_VALUE)
                //.parallel()
                .filter(fileExtensionFilter)
                .map(getData)
                .map(toSingleTokens)
                .flatMap(Collection::stream)
                .filter(Predicate.not(String::isEmpty))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                // compare entry.value descending
                //.filter(allAboveScore)
                .sorted((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue()))
                .forEach(entry -> out.println(entry.getKey() + "," + entry.getValue()));
        out.flush();
        System.out.println("\nFrequency calc took " + (System.currentTimeMillis() - start) + " ms");
    }

    private static Function<Path, String> getData = path -> {
        try { return Files.readString(path); } catch (IOException e) { return "NOT_FOUND"; }
    };

    private static Function<String, List<String>> toSingleTokens = data ->
        StreamSupport.stream(spliteratorUnknownSize(new StringTokenizer(data).asIterator(), Spliterator.NONNULL), true)
                .map(o -> ((String)o).replaceAll("[^a-zA-Z0-9.]", ""))
                .collect(Collectors.toList());
}
