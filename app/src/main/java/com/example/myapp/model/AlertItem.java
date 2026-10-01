package bushfireAlert.com.example.myapp.model;

import java.io.Serializable;

public class AlertItem implements Serializable {
    public String title;
    public String message;
    public String timestamp;
    public String location;
    public String advice;
    public int severity; // 0: Low, 1: Moderate, 2: High, 3: Extreme, 4: Catastrophic

    public AlertItem() {
        // Required for Firebase
    }

    public AlertItem(String title, String message, String timestamp, String location, String advice, int severity) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.location = location;
        this.advice = advice;
        this.severity = severity;
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
    public String getLocation() { return location; }
    public String getAdvice() { return advice; }
    public int getSeverity() { return severity; }
}
