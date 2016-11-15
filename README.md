## Running Anagram search tool locally
```
    git clone https://github.com/alexaopgit/anagrams
    cd anagrams
    ./mvnw spring-boot:run
```
You can then find the file with result in ./target/classes
You can also use additional parameters on start
    
    source - path to source file
    result - path to result file
Example:
    
    ./mvnw spring-boot:run -Drun.arguments="source=/tmp/anagrams/sample.txt,result=/tmp/anagrams/results.txt"

## Design decisions
An algorithm implemented in the class AnagramService. 
Uses spring-boot framework to simplify application start and to provide with additional monitoring info.
Uses 'commons-io' library to simplify work with files.

### Scalability
I've decided to implement an algorithm with Map/Reduce pattern.
On the map stage we prepare a map where key is sorted chars of words 
and the value is a list of words with equals set of letters.
This approach allows to scale the solution quite easy. We can parallel 
computation for 'Map' stage and merge found maps in one result on the 
reduce stage.
  
### Maintainability
The solution is easy to maintain. The algorithm is encapsulated in a class, 
good covered with text comments, so we can write unit tests for them. (One unit test included).
Additional logs could be included in the solution if necessary.

### Performance, Time and space consuming of algorithm
Time consuming is quite good:
'Map' stage - O(n) for main method and O(m*log(m)) for 'handleWord' method, 
where n is number of words in a file and m - number of letter in a word.
'Reduce' stage time consuming is depends on the number of anagram groups found.
The 'Reduce' stage of current implementation is quite space consuming.
We are keeping results from each 'map' stage in a memory.
 
From my point of view it is not necessary to use multithreading 
solution for datasets less than 10 million words. 
In case of billions words, it would be better to separate words 
into different files. Each file can be processed separately in a different
thread or different machine unit.
Look at the method 
        
    findAnagrams(List<File> sourceFiles, File resultFile)
 
Next method provides simple one-thread solution for small amount of words
 
    findAnagrams(File sourceFile, File resultFile)