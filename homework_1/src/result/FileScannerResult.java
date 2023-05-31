package result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FileScannerResult implements Result{

    private final String corpusName;
    private final List<Future<Map<String, Integer>>> futureResults;

    private Map<String, Integer> cachedResults = new HashMap<>();
    public FileScannerResult(String name, List<Future<Map<String, Integer>>> futureResults) {
        this.corpusName = name;
        this.futureResults = futureResults;

    }

    public FileScannerResult() {
        this.corpusName = null;
        this.futureResults = null;
    }

    @Override
    public Map<String, Integer> getResult() {
        if (!cachedResults.isEmpty()) {
            return cachedResults;
        }

        if (futureResults == null) {
            return null;
        }

        //kreiramo mapu za smestanje rezultata
        Map<String, Integer> result = new HashMap<>();
        for (Future<Map<String, Integer>> futureResult: futureResults) {
            try {
                Map<String, Integer> r = futureResult.get();

                for (Map.Entry<String, Integer> entry : r.entrySet()) {
                    int count = result.getOrDefault(entry.getKey(), 0);
                    result.put(entry.getKey(), count + entry.getValue());
                }
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("Failed to return result");
            }
        }

        if (!result.isEmpty()) {
            cachedResults = result;
        }

        return result;
    }

    @Override
    public Map<String, Integer> queryResult() {
        if (!cachedResults.isEmpty()) {
            return cachedResults;
        }

        if (futureResults == null) {
            return null;
        }

        boolean ready = true;
        for (Future<Map<String, Integer>> futureResult: futureResults) {
            if (!futureResult.isDone()) {
                ready = false;
                break;
            }
        }

        if (ready) {
            return getResult();
        }

        return null;
    }

    public String getCorpusName() {
        return corpusName;
    }

    public List<Future<Map<String, Integer>>> getFutureResults() {
        return futureResults;
    }

    public Map<String, Integer> getCachedResults() {
        return cachedResults;
    }
}
