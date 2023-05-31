package commands;

import result.WebScannerResult;

import java.util.Map;

public class GetWebResultCommand implements Command{
    public GetWebResultCommand(Map<String, WebScannerResult> webScannerResults, Map<String, Map<String, Integer>> cacheWebSummery) {
    }

    @Override
    public void execute() {

    }

    @Override
    public void setArguments(String args) {

    }

    @Override
    public String getCommandName() {
        return null;
    }

    @Override
    public boolean isFatal() {
        return false;
    }
}
