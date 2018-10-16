package io.reconquest.bitbucket.rest;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)

public class PullRequestLabelsModel {

    @XmlElement(name = "value")
    private String message;

    public PullRequestLabelsModel() {
    }

    public PullRequestLabelsModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
