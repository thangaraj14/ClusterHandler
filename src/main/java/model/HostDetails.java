package model;

import java.util.HashMap;

/**
 *
 */
public class HostDetails {

    private String hostName;
    private int hostSize;
    private HashMap<String, String> values;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getHostSize() {
        return hostSize;
    }

    public void setHostSize(int hostSize) {
        this.hostSize = hostSize;
    }

    public HashMap<String, String> getValues() {
        return values;
    }

    public void setValues(HashMap<String, String> values) {
        this.values = values;
    }

    public HostDetails(String hostName, int hostSize, HashMap<String, String> values) {
        this.hostName = hostName;
        this.hostSize = hostSize;
        this.values = values;
    }

    @Override
    public String toString() {
        return hostName + ": " + String.join(", ", values.keySet());
    }
}