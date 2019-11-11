package io.reconquest.bitbucket.labels.rest.response;

import javax.xml.bind.annotation.XmlElement;

public class PullRequestLabelResponse {
  @XmlElement(name = "id")
  private Integer id;

  @XmlElement(name = "label_id")
  private Integer labelId;

  @XmlElement(name = "name")
  private String name;

  @XmlElement(name = "color")
  private String color;

  public PullRequestLabelResponse() {}

  public PullRequestLabelResponse(Integer id, Integer labelId, String name, String color) {
    this.id = id;
    this.labelId = labelId;
    this.name = name;
    this.color = color;
  }

  public Integer getID() {
    return this.id;
  }

  public Integer getLabelId() {
    return this.labelId;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setColor(String color) {
    this.color = color;
  }
}
