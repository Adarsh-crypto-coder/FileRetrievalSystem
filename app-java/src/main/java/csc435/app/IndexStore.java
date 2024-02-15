package csc435.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class IndexStore {
    private Map<String, Integer> index;
    private Map<String, Map<String, Integer>> indexlocal;
    private Map<String, Set<String>> filesContainingTerm = new HashMap<>();
    private long startTime = System.nanoTime();
    private final Object lock = new Object();

    public IndexStore() {
        this.index = new HashMap<>();
        this.indexlocal = new HashMap<>();
    }

    public void insertIndex(String term) {
        synchronized (lock) {
            index.put(term, index.getOrDefault(term, 0) + 1);
        }
    }

    public Map<String, Integer> lookupindex(String[] terms) {
        Map<String, Integer> termOccurrences = new HashMap<>();
        synchronized (lock) {
            for (String term : terms) {
                if (index.containsKey(term)) {
                    termOccurrences.put(term, index.get(term));
                }
            }
        }
        return termOccurrences;
    }

    public void updateIndex() {
        synchronized (lock) {
            for (Map.Entry<String, Map<String, Integer>> entry : indexlocal.entrySet()) {
                String term = entry.getKey();
                Map<String, Integer> fileTermCounts = entry.getValue();
                for (Map.Entry<String, Integer> fileEntry : fileTermCounts.entrySet()) {
                    fileEntry.getKey();
                    int count = fileEntry.getValue();
                    index.put(term, index.getOrDefault(term, 0) + count);
                }
            }
            indexlocal.clear();
        }
    }

    public void addToLocalIndex(String term, String filePath, int count) {
        synchronized (lock) {
            Map<String, Integer> fileTermCounts = indexlocal.getOrDefault(term, new HashMap<>());
            fileTermCounts.put(filePath, count);
            indexlocal.put(term, fileTermCounts);
        }
    }

    public Set<String> getAllTerms() {
        synchronized (lock) {
            return new HashSet<>(index.keySet());
        }
    }    

    public void processFile(String filePath) {
        String[] terms = tokenizeFile(filePath);
        for (String term : terms) {
            addToLocalIndex(term, filePath, 1); 
            addFileForTerm(term, filePath); 
            insertIndex(term); 
        }
    }
    

    private void addFileForTerm(String term, String filePath) {
        synchronized (lock) {
            filesContainingTerm.computeIfAbsent(term, key -> new HashSet<>()).add(filePath);
        }
    }

    private String[] tokenizeFile(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            return content.split("\\s+");
        } catch (IOException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    public Set<String> getFilesContainingTerm(String term) {
        synchronized (lock) {
            return filesContainingTerm.getOrDefault(term, Collections.emptySet());
        }
    }

    public String getIndexingTime() {
        long endTime = System.nanoTime();
        double elapsedTimeSeconds = (endTime - startTime) / 1_000_000_000.0;
        return String.format("%.3f", elapsedTimeSeconds);
    }
}
