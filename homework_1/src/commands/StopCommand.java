package commands;

import crawler.DirectoryCrawler;
import jobs.JobDispatcher;
import result.ResultRetriever;
import scanner.file.FileScanner;

public class StopCommand implements Command{

    private final ResultRetriever resultRetriever;
    private final DirectoryCrawler directoryCrawler;
    private final JobDispatcher jobDispatcher;
    private final FileScanner fileScanner;

    public StopCommand (
            ResultRetriever resultRetriever,
            DirectoryCrawler directoryCrawler,
            JobDispatcher jobDispatcher,
            FileScanner fileScanner
    ) {
        this.resultRetriever = resultRetriever;
        this.directoryCrawler = directoryCrawler;
        this.jobDispatcher = jobDispatcher;
        this.fileScanner = fileScanner;
    }
    @Override
    public void execute() {
        directoryCrawler.stop();
        jobDispatcher.stop();
        fileScanner.stop();
        resultRetriever.stop();

    }

    @Override
    public void setArguments(String args) {

    }

    @Override
    public String getCommandName() {
        return "stop";
    }

    @Override
    public boolean isFatal() {
        return true;
    }
}
