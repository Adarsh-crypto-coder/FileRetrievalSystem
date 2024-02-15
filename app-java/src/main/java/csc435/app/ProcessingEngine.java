package csc435.app;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProcessingEngine {
    private IndexStore store;
    private ExecutorService executor;
    private long startTime;
    private final int TOP_N_FILES = 10;

    public ProcessingEngine(IndexStore store, int threadNum) {
        this.store = store;
        this.executor = Executors.newFixedThreadPool(threadNum);
    }

    public void indexFiles(String datasetPath) {
        long startTime = System.nanoTime();
        File dataset = new File(datasetPath);
        if (!dataset.exists() || !dataset.isDirectory()) {
            System.out.println("Invalid dataset path. Please provide a valid directory.");
            return;
        }

        try {
            Files.walkFileTree(dataset.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".txt")) {
                        executor.execute(() -> {
                            store.processFile(file.toAbsolutePath().toString());
                        });
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            store.updateIndex();
            long endTime = System.nanoTime();
            double elapsedTimeSeconds = (endTime - startTime) / 1_000_000_000.0;
            System.out.println("Indexing completed in " + elapsedTimeSeconds + " seconds.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void searchFiles(String query) {
        long startTime = System.nanoTime();
        String[] terms = query.split(" AND ");
        Map<String, Integer> filesOccurrences = new ConcurrentHashMap<>();

        for (String term : terms) {
            Map<String, Integer> termOccurrences = store.lookupindex(new String[]{term});
            for (Map.Entry<String, Integer> entry : termOccurrences.entrySet()) {
                Set<String> files = store.getFilesContainingTerm(entry.getKey());
                for (String file : files) {
                    filesOccurrences.put(file, filesOccurrences.getOrDefault(file, 0) + entry.getValue());
                }
            }
        }

        List<Map.Entry<String, Integer>> fileList = new ArrayList<>(filesOccurrences.entrySet());
        fileList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue())); 
        long endTime = System.nanoTime();
            double elapsedTimeSeconds = (endTime - startTime) / 1_000_000_000.0;
            System.out.println("Search completed in " + elapsedTimeSeconds + " seconds.");
        
        
        System.out.println("Search results (top 10):");
        int count = 0;
        for (Map.Entry<String, Integer> entry : fileList) {
            if (count >= 10) { 
                break;
            }
            System.out.println(entry.getKey() + " " + entry.getValue());
            count++;
        }
    }     

    public void stopWorkers() {
        executor.shutdownNow();
    }
}
