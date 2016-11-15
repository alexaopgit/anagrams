package com.aop;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Anagram search service
 */
@Service
public class AnagramService implements CommandLineRunner {
    /**
     * map, where key - sorted character of the word, value - list of words
     */
    private Map<String, List<String>> map = new HashMap<>();

    /**
     * Handle one more word
     *
     * @param word - next word to handle
     */
    public void handleWord(String word) {
        // convert the word into lower case character array
        char[] chars = word.toLowerCase().toCharArray();
        // sort an array
        Arrays.sort(chars);
        // use sorted array as a key
        List<String> anagram = map.get(String.valueOf(chars));
        if (anagram == null) {
            anagram = new ArrayList<>();
            map.put(String.valueOf(chars), anagram);
        }
        anagram.add(word);
    }

    public Map<String, List<String>> getMap() {
        return map;
    }

    /**
     * Find anagrams where source is in file
     *
     * @param sourceFile source file with words
     * @throws RuntimeException in case of some exceptions
     */
    public static Map<String, List<String>> mapAnagrams(File sourceFile) {
        try {
            // use LineIterator, it gives us an ability to read line by line and do not store the hole file in the memory
            LineIterator it = FileUtils.lineIterator(sourceFile, "UTF-8");
            try {
                // instantiate service
                AnagramService service = new AnagramService();

                while (it.hasNext()) {
                    // read line and process with a service
                    service.handleWord(it.nextLine());
                }
                return service.getMap();
            } finally {
                LineIterator.closeQuietly(it);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Produce the result list of lines
     *
     * @return list of anagrams
     * @throws IOException
     */
    public static List<String> reduceAnagrams(Map<String, List<String>> map) throws IOException {
        return map.values().stream()
                // get only lines with more than 1 word
                .filter(list -> list.size() > 1)
                // separate words by space
                .map(list -> list.stream().collect(Collectors.joining(" ")))
                // as list
                .collect(Collectors.toList());
    }

    /**
     * Find anagrams where source and results are in files
     *
     * @param sourceFiles list of source files with words
     * @param resultFile  file for results
     * @throws IOException in case of some exceptions
     */
    public static void findAnagrams(List<File> sourceFiles, File resultFile) throws IOException {
        Map<String, List<String>> map = sourceFiles.parallelStream()
                // get Map of anagrams for each file
                .map(AnagramService::mapAnagrams)
                // converts each map into an entry set
                .map(Map::entrySet)
                // converts each set into an entry stream, then "concatenates" it in place of the original set
                .flatMap(Collection::stream)
                .collect(
                        Collectors.toConcurrentMap(  // collects into a map
                                Map.Entry::getKey,   // where each entry is based
                                Map.Entry::getValue, // on the entries in the stream
                                // in case if 2 maps have the same key, just merge two list of words into 1 list
                                (a, b) -> Stream.of(a, b).flatMap(Collection::stream).collect(Collectors.toList())
                        )
                );
        // prepare the result and write it to the file
        FileUtils.writeLines(resultFile, AnagramService.reduceAnagrams(map));
    }

    /**
     * Find anagrams where source and results are in files
     *
     * @param sourceFile source file with words
     * @param resultFile file for results
     * @throws IOException in case of some exceptions
     */
    public static void findAnagrams(File sourceFile, File resultFile) throws IOException {
        // prepare the result and write it to the file
        FileUtils.writeLines(resultFile,
                AnagramService.reduceAnagrams(
                        AnagramService.mapAnagrams(sourceFile)));
    }

    @Override
    public void run(String... strings) throws Exception {
        List<String> parameters = Arrays.asList(strings);

        File source;
        if (parameters.stream().filter(el -> el.startsWith("source=")).count() > 0) {
            source = new File(
                    parameters.stream()
                            .filter(el -> el.startsWith("source="))
                            .map(p -> p.substring("source=".length(), p.length()))
                            .findFirst().get());
            if (!source.exists()) {
                throw new RuntimeException("Source file doesn't exist: " + source.getPath());
            }
        } else {
            System.out.println("Use default source file dir:"
                    + AnagramService.class.getProtectionDomain().getCodeSource().getLocation().getPath());

            source = new File(
                    AnagramService.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
                    "sample.txt"
            );
        }

        File result;
        if (parameters.stream().filter(el -> el.startsWith("result=")).count() > 0) {
            result = new File(
                    parameters.stream()
                            .filter(el -> el.startsWith("result="))
                            .map(p -> p.substring("result=".length(), p.length()))
                            .findFirst().get());
            if (result.exists()) {
                result.delete();
            }
        } else {
            System.out.println("Use default result file dir:"
                    + AnagramService.class.getProtectionDomain().getCodeSource().getLocation().getPath());

            result = new File(
                    AnagramService.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
                    "result.txt"
            );
        }

        System.out.println("Source file: " + source.getPath());

        AnagramService.findAnagrams(Collections.singletonList(source), result);

        System.out.println("Result file: " + result.getPath());
    }
}
