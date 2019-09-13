package io.reconquest.bitbucket.labels.rest;

import javax.xml.bind.annotation.*;

public class PullRequestLabelResponse {
  @XmlElement(name = "id")
  private Integer id;

  @XmlElement(name = "name")
  private String name;

  public PullRequestLabelResponse() {}

  public PullRequestLabelResponse(Integer id, String name) {
    this.id = id;
    this.name = name;
  }

  public Integer getID() {
    return this.id;
  }

  public void setName(String name) {
    this.name = name;
  }
}
