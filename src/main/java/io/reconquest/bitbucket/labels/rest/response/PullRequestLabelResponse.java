package io.reconquest.bitbucket.labels.rest.response;

import javax.xml.bind.annotation.XmlElement;

import io.reconquest.bitbucket.labels.Label;

public class PullRequestLabelResponse {
  @XmlElement(name = "item_id")
  private Integer itemId;

  @XmlElement(name = "label_id")
  private Integer labelId;

  @XmlElement(name = "name")
  private String name;

  @XmlElement(name = "color")
  private String color;

  public PullRequestLabelResponse() {}

  public PullRequestLabelResponse(Label label) {
    this.itemId = label.getItemId();
    this.labelId = label.getLabelId();
    this.name = label.getName();
    this.color = label.getColor();
  }
}
