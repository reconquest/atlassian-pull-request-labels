package io.reconquest.bitbucket.labels.rest.response;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "label_id")
@XmlAccessorType(XmlAccessType.FIELD)
public class PullRequestLabelsSaveResponse {
  @XmlElement(name = "id")
  private int id;

  public PullRequestLabelsSaveResponse() {}

  public PullRequestLabelsSaveResponse(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public void setSuccess(int id) {
    this.id = id;
  }
}
