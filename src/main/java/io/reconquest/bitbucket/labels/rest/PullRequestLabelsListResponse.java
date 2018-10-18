package io.reconquest.bitbucket.labels.rest;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "labels")
@XmlAccessorType(XmlAccessType.FIELD)

public class PullRequestLabelsListResponse {
    @XmlElement(name = "labels")
    private String[] labels;

    public PullRequestLabelsListResponse() {
    }

    public PullRequestLabelsListResponse(String[] labels) {
        this.labels = labels;
    }

    public String[] getLabels() {
        return labels;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }
}
