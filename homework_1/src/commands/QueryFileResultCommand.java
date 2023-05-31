package commands;

import result.FileScannerResult;

import java.util.HashMap;
import java.util.Map;

public class QueryFileResultCommand implements Command{

    private String args;

    private final Map<String, FileScannerResult> fileScannerResults;
    private Map<String, Map<String, Integer>> cacheFileSummery;
    public QueryFileResultCommand (
            Map<String, FileScannerResult> fileScannerResults,
            Map<String, Map<String, Integer>> cacheFileSummery
    ) {
        this.fileScannerResults = fileScannerResults;
        this.cacheFileSummery = cacheFileSummery;
    }

    @Override
    public void execute() {
        if (args.equals("summary")) {
            this.callSummary();
            return;
        }

        Map<String, Integer> result = null;

        FileScannerResult fileScannerResult = fileScannerResults.get(this.args);
        if (fileScannerResult != null) {
            result = fileScannerResult.queryResult();
        }

        if (result == null) {
            System.out.println("Corpus: [" + this.args + "] doesn't exist");
            return;
        }

        System.out.println(result);

    }

    private void callSummary() {
        if (!cacheFileSummery.isEmpty()) {
            for (Map.Entry<?, ?> entry : cacheFileSummery.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            return;
        }

        Map<String, Map<String, Integer>> summary = new HashMap<>();

        for (Map.Entry<String, FileScannerResult> entry: fileScannerResults.entrySet()) {
            FileScannerResult fileScannerResult = entry.getValue();
            Map<String, Integer> result = fileScannerResult.queryResult();

            if (result == null) {
                System.out.println("[File Summary] doesn't ready yet...");
                return;
            }

            summary.put(fileScannerResult.getCorpusName(), result);
        }

        if (summary.isEmpty()) {
            System.out.println("Summary doesn't exist");
            return;
        }

        cacheFileSummery = summary;

        for (Map.Entry<?, ?> entry : summary.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    @Override
    public void setArguments(String args) {
        this.args = args;
    }

    @Override
    public String getCommandName() {
        return "file";
    }

    @Override
    public boolean isFatal() {
        return false;
    }
}
