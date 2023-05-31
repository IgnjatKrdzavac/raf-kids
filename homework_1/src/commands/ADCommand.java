package commands;

import crawler.DirectoryCrawler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CopyOnWriteArrayList;

public class ADCommand implements Command{

    private String args;

    private final CopyOnWriteArrayList<String> directoryCrawlerPaths;
    private final DirectoryCrawler directoryCrawler;

    public ADCommand (CopyOnWriteArrayList<String> directoryCrawlerPaths, DirectoryCrawler directoryCrawler) {
        this.directoryCrawlerPaths = directoryCrawlerPaths;
        this.directoryCrawler = directoryCrawler;
    }

    @Override
    public void execute() {



        if (Files.notExists(Path.of(this.args))) {
            System.out.println("Couldn't find path: " + this.args);
            return;
        }

        if (!directoryCrawlerPaths.contains(this.args)) {
            System.out.println("postavljam path");
            directoryCrawlerPaths.add(this.args);
        }

        directoryCrawler.findCorpusAndCreateJob(this.args, true);

    }

    @Override
    public void setArguments(String args) {
        this.args = args;
    }

    @Override
    public String getCommandName() {
        return "ad";
    }

    @Override
    public boolean isFatal() {
        return false;
    }
}
