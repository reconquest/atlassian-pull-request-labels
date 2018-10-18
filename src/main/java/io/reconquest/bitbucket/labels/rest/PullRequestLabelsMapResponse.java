package io.reconquest.bitbucket.labels.rest;

import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.ArrayList;

@XmlRootElement(name = "labels")
@XmlAccessorType(XmlAccessType.FIELD)
public class PullRequestLabelsMapResponse {
    @XmlElement(name = "labels")
    private HashMap<Long,ArrayList<String>> labels;

    public PullRequestLabelsMapResponse() {
    }

    public PullRequestLabelsMapResponse(HashMap<Long,ArrayList<String>> labels) {
        this.labels = labels;
    }

    public HashMap<Long,ArrayList<String>> getLabels() {
        return labels;
    }

    public void setLabels(HashMap<Long,ArrayList<String>> labels) {
        this.labels = labels;
    }
}
