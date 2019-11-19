package io.reconquest.bitbucket.labels.rest.response;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.reconquest.bitbucket.labels.Label;

@XmlRootElement(name = "labels")
@XmlAccessorType(XmlAccessType.FIELD)
public class PullRequestLabelsTreeResponse {
  @XmlElement(name = "labels")
  private Tree labels;

  public Tree getLabels() {
    return labels;
  }

  public PullRequestLabelsTreeResponse(Label[] items) {
    labels = new Tree();

    for (Label item : items) {
      TreeBranch branch = labels.get(item.getRepositoryId());
      if (branch == null) {
        branch = new TreeBranch();
        labels.put(item.getRepositoryId(), branch);
      }

      ArrayList<PullRequestLabelResponse> pullRequestLabels = branch.get(item.getPullRequestId());
      if (pullRequestLabels == null) {
        pullRequestLabels = new ArrayList<PullRequestLabelResponse>();
        branch.put(item.getPullRequestId(), pullRequestLabels);
      }

      pullRequestLabels.add(new PullRequestLabelResponse(item));
    }
  }

  public class Tree extends HashMap<Integer, TreeBranch> {
    public Tree() {
      super();
    }
  }

  public class TreeBranch extends HashMap<Long, ArrayList<PullRequestLabelResponse>> {
    public TreeBranch() {
      super();
    }
  }
}
