package model;

/**
 *
 */
public class Result {
    private String fileName;
    private String sourceHost;
    private String destinationHost;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(String sourceHost) {
        this.sourceHost = sourceHost;
    }

    public String getDestinationHost() {
        return destinationHost;
    }

    public void setDestinationHost(String destinationHost) {
        this.destinationHost = destinationHost;
    }

    public Result(String fileName, String sourceHost, String destinationHost) {
        this.fileName = fileName;
        this.sourceHost = sourceHost;
        this.destinationHost = destinationHost;
    }

    @Override
    public String toString() {
        return fileName + ", " + sourceHost + ", " + destinationHost;
    }
}