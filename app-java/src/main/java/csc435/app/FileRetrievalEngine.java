package csc435.app;

public class FileRetrievalEngine {

    public static void main(String[] args) {
        IndexStore store = new IndexStore();
        int threadNum = 1; 
        ProcessingEngine engine = new ProcessingEngine(store, threadNum);
        AppInterface appInterface = new AppInterface(engine);

        appInterface.readCommands();
    }
}
