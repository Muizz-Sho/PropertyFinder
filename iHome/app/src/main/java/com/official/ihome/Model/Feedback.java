package com.official.ihome.Model;

public class Feedback {
    String feedID,feedSubject,feedMessage;

    public Feedback() {
    }

    public Feedback(String feedID, String feedSubject, String feedMessage) {
        this.feedID = feedID;
        this.feedSubject = feedSubject;
        this.feedMessage = feedMessage;
    }

    public String getFeedID() {
        return feedID;
    }

    public void setFeedID(String feedID) {
        this.feedID = feedID;
    }

    public String getFeedSubject() {
        return feedSubject;
    }

    public void setFeedSubject(String feedSubject) {
        this.feedSubject = feedSubject;
    }

    public String getFeedMessage() {
        return feedMessage;
    }

    public void setFeedMessage(String feedMessage) {
        this.feedMessage = feedMessage;
    }
}
