package crawler;

import jobs.Job;
import jobs.ScanType;
import jobs.Stoppable;
import utils.PropertiesLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class DirectoryCrawler implements Runnable, Stoppable {

    private final CopyOnWriteArrayList<String> rememberedPaths;
    private final Map<String, Object> properties;
    private final BlockingQueue<Job> jobQueue;

    private volatile boolean forever = true;
    private volatile boolean sleeping = false;
    private final HashMap<String, Long> lastModified = new HashMap<>();

    public DirectoryCrawler(CopyOnWriteArrayList<String> rememberedPaths, BlockingQueue<Job> jobQueue) {
        this.properties = PropertiesLoader.getInstance().getProperties();

        this.rememberedPaths = rememberedPaths;
        this.jobQueue = jobQueue;
    }

    @Override
    public void run() {
        while (this.forever) {
            try {
                sleeping = true;

                //u slucaju da nesto imamo u redu za direktorijume, vrsimo pretragu i onda cekammo vreme zadato
                //u properties fajlu.
                if (!this.rememberedPaths.isEmpty()) {
                    synchronized (this) {
                        wait((Long) this.properties.get("dir_crawler_sleep_time"));
                    }
                } else {
                    synchronized (this) {
                        wait(100);
                    }
                }
                sleeping = false;

                for (String rememberedPath: this.rememberedPaths) {
                    this.findCorpusAndCreateJob(rememberedPath, false);
                }
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }

        System.out.println("Directory crawler shutting down");
    }

    public void findCorpusAndCreateJob(String path, boolean isLoggerON) {
        ArrayList<File> corpusDirectories = new ArrayList<>();

        // Proveravamo da li je direktno zadati path ujedno i corpus
        File directory = new File(path);
        if (directory.isDirectory() && directory.getName().startsWith((String) this.properties.get("file_corpus_prefix"))) {
            corpusDirectories.add(directory);
        }

        if(isLoggerON) {
            System.out.println("Adding dir " + directory.getAbsolutePath());
        }


        // Pronalazimo sve corpuse i dodajemo ih u listu
        findCorpus(path, corpusDirectories);

        for (File corpusDirectory: corpusDirectories) {
            boolean valid = false;

            File[] corpusFiles = corpusDirectory.listFiles();

            if (corpusFiles != null) {
                // Proveravamo last modified vreme svih fajlova u diru
                for (File corpusFile: corpusFiles) {
                    try {
                        Long newLastModifiedTime = Files.getLastModifiedTime(Path.of(corpusFile.getAbsolutePath())).toMillis();
                        Long oldLastModifiedTime = this.lastModified.get(corpusFile.getAbsolutePath());

                        this.lastModified.put(corpusFile.getAbsolutePath(), newLastModifiedTime);

                        if (!newLastModifiedTime.equals(oldLastModifiedTime)) {
                            valid = true;
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (valid) {
                jobQueue.add(new Job(corpusDirectory.getName(),corpusDirectory.getAbsolutePath(), ScanType.FILE,isLoggerON));
            }
        }
    }

    private void findCorpus(String path, ArrayList<File> corpusDirectories) {
        File directory = new File(path);

        File[] dirFiles = directory.listFiles();

        if (dirFiles == null) {
            return;
        }

        //rekurzivno prolazimo kroz svaki direktorijum u njegovu dubinu

        for (File file: dirFiles) {
            if (file.isDirectory()) {
                if (file.getName().startsWith((String) this.properties.get("file_corpus_prefix"))) {
                    corpusDirectories.add(file);
                }

                findCorpus(file.getPath(), corpusDirectories);
            }
        }
    }

    @Override
    public void stop() {
        this.forever = false;

        //saljemo poisonus job kako bi zaustavili jobDispatcher
        jobQueue.add(new Job());

        if (sleeping)
            Thread.currentThread().interrupt();

    }
}
