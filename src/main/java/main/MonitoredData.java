package main;

import java.time.LocalDateTime;

/**
 * Created by Rona Dumitrescu on 24.05.2019.
 */
public class MonitoredData {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String activity;

    // GETTERS & SETTERS
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }



}
