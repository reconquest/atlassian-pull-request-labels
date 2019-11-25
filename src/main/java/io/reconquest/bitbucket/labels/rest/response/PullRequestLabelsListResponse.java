package io.reconquest.bitbucket.labels.rest.response;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "labels")
@XmlAccessorType(XmlAccessType.FIELD)
public class PullRequestLabelsListResponse {
  @XmlElement(name = "labels")
  private PullRequestLabelResponse[] labels;

  public PullRequestLabelsListResponse() {}

  public PullRequestLabelsListResponse(PullRequestLabelResponse[] labels) {
    this.labels = labels;
  }

  public PullRequestLabelResponse[] getLabels() {
    return labels;
  }

  public void setLabels(PullRequestLabelResponse[] labels) {
    this.labels = labels;
  }
}
