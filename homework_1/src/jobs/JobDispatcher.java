package jobs;

import java.util.concurrent.BlockingQueue;

public class JobDispatcher implements Runnable, Stoppable{

    private final BlockingQueue <Job> jobQueue;
    private final BlockingQueue <Job> fileScannerJobQueue;
    private final BlockingQueue <Job> webScannerJobQueue;


    private volatile boolean forever = true;

    public JobDispatcher(BlockingQueue jobQueue, BlockingQueue<Job> fileScannerJobQueue, BlockingQueue<Job> webScannerJobQueue) {
        this.jobQueue = jobQueue;
        this.fileScannerJobQueue = fileScannerJobQueue;
        this.webScannerJobQueue = webScannerJobQueue;
    }



    @Override
    public void run() {
            while (this.forever) {
                try {

                    //kada se nesto nadje u jobQueue, poziva se blokirajuci take() i posao se na osnovu tipa,
                    //smesta u neki od odgovarajucih redova

                    Job job = this.jobQueue.take();
                    ScanType scanType = job.getScanType();

                    if (scanType == ScanType.POISON) {
                        break;
                    } else if (scanType == ScanType.WEB) {
                        webScannerJobQueue.add(job);
                    } else if (scanType == ScanType.FILE) {
                        fileScannerJobQueue.add(job);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Job dispatcher shutting down");
        }



    @Override
    public void stop() {
        this.forever = false;

        fileScannerJobQueue.add(new Job());
        webScannerJobQueue.add(new Job());
    }
}
