package core;

import commands.*;
import crawler.DirectoryCrawler;
import jobs.JobDispatcher;
import result.FileScannerResult;
import result.ResultRetriever;
import result.WebScannerResult;
import scanner.file.FileScanner;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Processor {

    private final List<Command> commands;


    public Processor (
            ResultRetriever resultRetriever,
            DirectoryCrawler directoryCrawler,
            JobDispatcher jobDispatcher,
            CopyOnWriteArrayList<String> directoryCrawlerPaths,
            FileScanner fileScanner,
            Map<String, FileScannerResult> fileScannerResults,
            Map<String, WebScannerResult> webScannerResults,
            Map<String, Map<String, Integer>> cacheFileSummery,
            Map<String, Map<String, Integer>> cacheWebSummery) {
        commands = List.of(
                new ADCommand(directoryCrawlerPaths, directoryCrawler),
                new AWCommand(),
                new GetCommand(fileScannerResults,webScannerResults,cacheFileSummery,cacheWebSummery),
                new StopCommand(resultRetriever,directoryCrawler,jobDispatcher,fileScanner),
                new QueryCommand(fileScannerResults,webScannerResults,cacheFileSummery,cacheWebSummery),
                new CFSCommand(cacheFileSummery),
                new CWSCommand(cacheWebSummery)
        );
    }

    public Command getCommand (String line) {
        Command command = new NotFoundCommand();

        String[] args = line.split("\\s+");

        //prolazimo kroz sve poznate komande i ako se prvi argument neke komande poklapa sa komandom, vracamo je u main
        //ako ne nadjemo  nijednu, vracamo NotFoundCommand

        for (Command c: commands) {
            if (c.getCommandName() != null && c.getCommandName().equals(args[0])) {
                command = c;

                if(args.length <= 1 && !(args[0].equals( "cfs")) &&  !(args[0].equals( "cws")) && !(args[0].equals( "stop"))){
                    System.out.println("Wrong command params");
                    return new NotFoundCommand();
                }

                if (args.length > 1) {
                    //komandi prosledjujemo sve posle kljucne reci
                    command.setArguments(args[1]);
                }
            }
        }

        return command;
    }

}
