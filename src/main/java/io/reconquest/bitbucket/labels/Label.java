package io.reconquest.bitbucket.labels;

import java.util.HashMap;

import io.reconquest.bitbucket.labels.ao.LabelEntity;
import io.reconquest.bitbucket.labels.ao.LabelItem;

public class Label {
  private int itemId;
  private int labelId;

  private int projectId;
  private int repositoryId;
  private long pullRequestId;

  private String name;
  private String color;

  public int getItemId() {
    return itemId;
  }

  public void setItemId(int itemId) {
    this.itemId = itemId;
  }

  public int getLabelId() {
    return labelId;
  }

  public void setLabelId(int labelId) {
    this.labelId = labelId;
  }

  public int getProjectId() {
    return projectId;
  }

  public void setProjectId(int projectId) {
    this.projectId = projectId;
  }

  public int getRepositoryId() {
    return repositoryId;
  }

  public void setRepositoryId(int repositoryId) {
    this.repositoryId = repositoryId;
  }

  public long getPullRequestId() {
    return pullRequestId;
  }

  public void setPullRequestId(long pullRequestId) {
    this.pullRequestId = pullRequestId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public static class Factory {
    public static Label getLabel(LabelItem aoItem, LabelEntity aoLabel) {
      Label label = new Label();
      label.setItemId(aoItem.getID());
      label.setProjectId(aoItem.getProjectId());
      label.setRepositoryId(aoItem.getRepositoryId());
      label.setPullRequestId(aoItem.getPullRequestId());
      label.setLabelId(aoItem.getLabelId());
      if (aoLabel != null) {
        label.setName(aoLabel.getName());
        label.setColor(aoLabel.getColor());
      }
      return label;
    }

    public static Label[] getLabels(LabelItem[] aoItems, LabelEntity[] aoLabels) {
      Label[] labels = new Label[aoItems.length];

      HashMap<Integer, LabelEntity> aoLabelsMap = new HashMap<Integer, LabelEntity>();
      if (aoLabels != null) {
        for (LabelEntity label : aoLabels) {
          aoLabelsMap.put(Integer.valueOf(label.getID()), label);
        }
      }

      for (int i = 0; i < aoItems.length; i++) {
        labels[i] = getLabel(
            aoItems[i], aoLabelsMap.getOrDefault(Integer.valueOf(aoItems[i].getLabelId()), null));
      }

      return labels;
    }
  }
}
