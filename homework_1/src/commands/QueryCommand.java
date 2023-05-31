package commands;

import result.FileScannerResult;
import result.WebScannerResult;

import java.util.List;
import java.util.Map;

public class QueryCommand implements Command{

    private String args;

    private final List<Command> subCommands;

    public QueryCommand (
            Map<String, FileScannerResult> fileScannerResults,
            Map<String, WebScannerResult> webScannerResults,
            Map<String, Map<String, Integer>> cacheFileSummery,
            Map<String, Map<String, Integer>> cacheWebSummery
    ) {
        this.subCommands = List.of(
                new QueryFileResultCommand(fileScannerResults, cacheFileSummery),
                new QueryWebResultCommand(webScannerResults, cacheWebSummery)
        );
    }
    @Override
    public void execute() {
        String[] argsList = this.args.split("\\|");

        if (argsList.length < 2) {
            System.out.println("Wrong command params");
            return;
        }

        Command subCommand = new NotFoundCommand();
        for (Command c: subCommands) {
            if (c.getCommandName() != null && c.getCommandName().equals(argsList[0])) {
                subCommand = c;
                subCommand.setArguments(argsList[1]);
            }
        }

        subCommand.execute();

    }

    @Override
    public void setArguments(String args) {
        this.args = args;
    }

    @Override
    public String getCommandName() {
        return "query";
    }

    @Override
    public boolean isFatal() {
        return false;
    }
}
