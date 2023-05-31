package scanner.file;

import jobs.Job;
import jobs.ScanType;
import jobs.Stoppable;
import result.FileScannerResult;
import result.Result;
import utils.PropertiesLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FileScanner implements Runnable, Stoppable {

    private final ExecutorService threadPool;
    private final Map<String, Object> properties;
    private final ExecutorCompletionService<Map<String, Integer>> completionService;// dohvatanje rezultata
    private final BlockingQueue<Job> fileScannerJobQueue;
    private final BlockingQueue<Result> resultQueue;

    private volatile boolean forever = true;


    public FileScanner(BlockingQueue<Job> fileScannerJobQueue, BlockingQueue<Result> resultQueue) {
        this.properties = PropertiesLoader.getInstance().getProperties();
        this.threadPool = Executors.newCachedThreadPool();
        this.fileScannerJobQueue = fileScannerJobQueue;
        this.resultQueue = resultQueue;
        this.completionService = new ExecutorCompletionService<>(this.threadPool);
    }

    @Override
    public void run() {
        while(this.forever) {
            try {

                //konstantno cekamo pojavljivanje fileJoba i uzimamo ga sa take

                Job job = this.fileScannerJobQueue.take();

                //u slucaju da je poisonous, prekidamo
                if(job.getScanType().equals(ScanType.POISON)){
                    break;
                }
                //za svaki posao pozivamo devideWork
                divideWork(job.getName(),job.getPath(),job.isLoggerON());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        this.threadPool.shutdownNow();

        System.out.println("File scanner shutting down");
    }

    private void divideWork(String corpusName, String path, boolean isLoggerOn) {

        List<File> files = new ArrayList<>();
        List<Future<Map<String, Integer>>> results = new ArrayList<>();

        if(isLoggerOn) {
            System.out.println("Starting file scan for file|" + corpusName);
        }


        //izlistavamo sve fajlove prosledjenog direktorijuma
        File[] childrenFiles = new File(path).listFiles();

        long fileScanningSizeLimit = (Integer) this.properties.get("file_scanning_size_limit");
        long currentSize = 0;

        if (childrenFiles != null) {

            for (File f : childrenFiles) {

                //svaki fajl iz direktorijuma dodajemo u listu files, sve dok ili ne zavrsimo sa obradom celog dira
                //ili dok ne predjemo zadati scan limit. Kada se desi nesto od to dvoje, pozivamo doWork sa fajlovima
                //i praznom listom  za rezultate

                currentSize += f.length();
                files.add(f);

                if (currentSize > fileScanningSizeLimit) {

                    List<File> filesKoopija = new ArrayList<>(files);
                    doWork(filesKoopija, results);

                    currentSize = 0;
                    files.clear();
                }
            }

            if (!files.isEmpty()) {
                doWork(files, results);
            }


              //results ce predstavljati broj pojavljivanja kljucnih reci u svakom fajlu unutar corpus fajlova
              //unutar klase fileScannerResult cemo sabrati pojavljivanja u zajednicki rez
              //u zajednicki red svih rezultata u sistemu resultQueue dodajemo fileScannerResult

              FileScannerResult fileScannerResult = new FileScannerResult(corpusName, results);
              resultQueue.add(fileScannerResult);


        }

    }

    private void doWork (List<File> files, List<Future<Map<String, Integer>>> results) {
        List<String> searchKeywords = (List<String>) properties.get("keywords");

        //pomocu threadPoola pokrecemo potrebni broj niti i dobijamo Future kao rezultat

        FileScannerWorker fileScannerWorker = new FileScannerWorker(searchKeywords, files);
        Future<Map<String, Integer>> result = this.completionService.submit(fileScannerWorker);
        results.add(result);

    }

    @Override
    public void stop() {
        this.forever = false;

        resultQueue.add(new FileScannerResult());
    }
}
