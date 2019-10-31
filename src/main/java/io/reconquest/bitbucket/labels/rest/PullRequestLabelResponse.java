package io.reconquest.bitbucket.labels.rest;

import javax.xml.bind.annotation.*;

public class PullRequestLabelResponse {
  @XmlElement(name = "id")
  private Integer id;

  @XmlElement(name = "name")
  private String name;

  @XmlElement(name = "color")
  private String color;

  public PullRequestLabelResponse() {}

  public PullRequestLabelResponse(Integer id, String name, String color color) {
    this.id = id;
    this.name = name;
    this.color = color;
  }

  public Integer getID() {
    return this.id;
  }

  public void setName(String name) {
    this.name = name;
  }
}
