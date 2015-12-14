package fi.vtt.dsp.service.serviceregistry.common;

public class ServiceRegistryLogEntry {
    private String timeStamp;
    private String hostingServiceId;
    private String hostingInstanceId;
    private String requestingServiceId;
    private String requestingInstanceId;
    private String timeSpent;
    private String request;

    public ServiceRegistryLogEntry(String timeStamp, String hostingServiceId, String hostingInstanceId, String requestingServiceId, String requestingInstanceId, String timeSpent, String request) {
        this.timeStamp = timeStamp;
        this.hostingServiceId = hostingServiceId;
        this.hostingInstanceId = hostingInstanceId;
        this.requestingServiceId = requestingServiceId;
        this.requestingInstanceId = requestingInstanceId;
        this.timeSpent = timeSpent;
        this.request = request;
    }
    
    public ServiceRegistryLogEntry() {
    }
    
    public String getHostingServiceId() {
        return hostingServiceId;
    }

    public void setHostingServiceId(String hostingServiceId) {
        this.hostingServiceId = hostingServiceId;
    }

    public String getHostingInstanceId() {
        return hostingInstanceId;
    }

    public void setHostingInstanceId(String hostingInstanceId) {
        this.hostingInstanceId = hostingInstanceId;
    }

    public String getRequestingServiceId() {
        return requestingServiceId;
    }

    public void setRequestingServiceId(String requestingServiceId) {
        this.requestingServiceId = requestingServiceId;
    }

    public String getRequestingInstanceId() {
        return requestingInstanceId;
    }

    public void setRequestingInstanceId(String requestingInstanceId) {
        this.requestingInstanceId = requestingInstanceId;
    }

    public String getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(String timeSpent) {
        this.timeSpent = timeSpent;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
