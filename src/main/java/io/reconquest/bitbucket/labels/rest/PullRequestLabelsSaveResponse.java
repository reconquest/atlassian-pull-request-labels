package io.reconquest.bitbucket.labels.rest;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "success")
@XmlAccessorType(XmlAccessType.FIELD)

public class PullRequestLabelsSaveResponse {
    @XmlElement(name = "success")
    private boolean success;

    public PullRequestLabelsSaveResponse() {
    }

    public PullRequestLabelsSaveResponse(boolean success) {
        this.success = success;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
