package commands;

import java.util.Map;

public class CWSCommand implements Command{

    private final Map<String, Map<String, Integer>> cacheWebSummery;

    public CWSCommand (Map<String, Map<String, Integer>> cacheWebSummery) {
        this.cacheWebSummery = cacheWebSummery;
    }

    @Override
    public void execute() {
        cacheWebSummery.clear();
        System.out.println("Web summary is cleared");
    }

    @Override
    public void setArguments(String args) {

    }

    @Override
    public String getCommandName() {
        return "cws";
    }

    @Override
    public boolean isFatal() {
        return false;
    }
}
