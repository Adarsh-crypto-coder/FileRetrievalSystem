package csc435.app;

import java.lang.System;
import java.util.Scanner;

public class AppInterface {
    private ProcessingEngine engine;

    public AppInterface(ProcessingEngine engine) {
        this.engine = engine;
    }

    public void readCommands() {
        
        Scanner sc = new Scanner(System.in);
        String command;
        
        while (true) {
            System.out.print("> ");
            
            // read from command line
            command = sc.next();

            // if the command is quit, terminate the program       
            if (command.compareTo("quit") == 0) {
                engine.stopWorkers();
                break;
            }
            
            // if the command begins with index, index the files from the specified directory
            if (command.length() >= 5 && command.substring(0, 5).compareTo("index") == 0) {
                String datasetPath = sc.nextLine().trim();
                if (!datasetPath.isEmpty()) {
                    engine.indexFiles(datasetPath);
                } else {
                    System.out.println("Please provide the dataset path.");
                }
                continue;
            }

            // if the command begins with search, search for files that matches the query
            if (command.length() >= 6 && command.substring(0, 6).compareTo("search") == 0) {
                String query = sc.nextLine().trim();
            if (!query.isEmpty()) {
                engine.searchFiles(query);
            } else {
                System.out.println("Please provide a search query.");
            }
            continue;
            }

            System.out.println("unrecognized command!");
        }
    }
}
