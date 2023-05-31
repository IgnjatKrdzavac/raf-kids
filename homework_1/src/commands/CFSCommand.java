package commands;

import java.util.Map;

public class CFSCommand implements Command{

    private final Map<String, Map<String, Integer>> cacheFileSummery;

    public CFSCommand (Map<String, Map<String, Integer>> cacheFileSummery) {
        this.cacheFileSummery = cacheFileSummery;
    }


    @Override
    public void execute() {

        cacheFileSummery.clear();
        System.out.println("File summary is cleared");

    }

    @Override
    public void setArguments(String args) {

    }

    @Override
    public String getCommandName() {
        return "cfs";
    }

    @Override
    public boolean isFatal() {
        return false;
    }
}
