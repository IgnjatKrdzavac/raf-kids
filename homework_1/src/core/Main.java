package core;

import commands.Command;
import crawler.DirectoryCrawler;
import jobs.Job;
import jobs.JobDispatcher;
import result.FileScannerResult;
import result.Result;
import result.ResultRetriever;
import result.WebScannerResult;
import scanner.file.FileScanner;
import utils.PropertiesLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;


public class Main {

    private static DirectoryCrawler directoryCrawler;
    private static JobDispatcher jobDispatcher;
    private static FileScanner fileScanner;
    private static ResultRetriever resultRetriever;
    private static final CopyOnWriteArrayList<String> directoryCrawlerPaths = new CopyOnWriteArrayList<>();
    private static final BlockingQueue<Job> jobQueue = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Result> resultQueue = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Job> fileScannerJobQueue = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Job> webScannerJobQueue = new LinkedBlockingQueue<>();
    private static final Map<String, FileScannerResult> fileScannerResults = new HashMap<>();
    private static final Map<String, WebScannerResult> webScannerResults = new HashMap<>();
    private static final Map<String, Map<String, Integer>> cacheFileSummery = new HashMap<>();
    private static final Map<String, Map<String, Integer>> cacheWebSummery = new HashMap<>();


    public static void main(String[] args) {
        System.out.println("LOL");
       PropertiesLoader.getInstance();

       initResultRetriever();
       initDirectoryCrawler();
       initJobDispatcher();
       initFileScanner();

       forever();
    }

    private static void forever () {
        Scanner sc = new Scanner(System.in);

        Processor processor = new Processor(
                resultRetriever,
                directoryCrawler,
                jobDispatcher,
                directoryCrawlerPaths,
                fileScanner,
                fileScannerResults,
                webScannerResults,
                cacheFileSummery,
                cacheWebSummery
        );

        while (true) {
            String args = sc.nextLine();

            //izvrsavamo komandu koja se poklapa sa ulazom
            Command command = processor.getCommand(args);
            command.execute();

            if (command.isFatal()) {
                break;
            }
        }

        sc.close();
        System.out.println("Main shutting down");
    }

    private static void initResultRetriever() {

        //prolazimo kroz resultQueue i na osnovu vrste posla, dodajemo ga u odg file ili web res
        resultRetriever = new ResultRetriever(
                resultQueue,
                fileScannerResults,
                webScannerResults,
                cacheFileSummery,
                cacheWebSummery
        );
        Thread thread = new Thread(resultRetriever, "ResultRetriever");
        thread.start();
    }

    private static void initDirectoryCrawler () {

        //pravimo instancu dir crawlera kome prosledjujemo prazan red za putanju, koja ce biti dodata 'ad' komandom.
        //takodje prosledjujemo jobqueue koji cemo popunjavati odgovarajucim poslovima za corpus fajlove.

        directoryCrawler = new DirectoryCrawler(directoryCrawlerPaths, jobQueue);
        Thread thread = new Thread(directoryCrawler, "DirectoryCrawler");
        thread.start();
    }

    private static void initJobDispatcher() {

        //kreiramo jobDispatcher koji ce konstantno gledati jobQueue i u zavisnosti od vrste posla,
        //isti dodeliti u fileScannerJobQueue ili webScannerJobQueue

        jobDispatcher = new JobDispatcher(jobQueue, fileScannerJobQueue, webScannerJobQueue);
        Thread thread = new Thread(jobDispatcher, "JobDispatcher");
        thread.start();
    }

    private static void initFileScanner() {

        //u fileScanneru prolazimo kroz fileScannerJobQueue i za svaki file kreiramo rezultat  pojavljivanja kljucnih
        //reci koji dodajemo u resultQueue

        fileScanner = new FileScanner(fileScannerJobQueue, resultQueue);
        Thread thread = new Thread(fileScanner, "FileScanner");
        thread.start();
    }



}