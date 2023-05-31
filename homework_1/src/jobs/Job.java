package jobs;

public class Job {

    private final String path;
    private String name;
    private final ScanType scanType;

    private final boolean loggerON;

    public Job() {
        this(null,null, ScanType.POISON, false);
    }

    public Job(String name, String path, ScanType scanType, boolean isLoggerOn) {
        this.name = name;
        this.path = path;
        this.scanType = scanType;

        this.loggerON = isLoggerOn;
    }

    public String getPath() {
        return path;
    }

    public ScanType getScanType() {
        return scanType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLoggerON() {
        return loggerON;
    }
}
