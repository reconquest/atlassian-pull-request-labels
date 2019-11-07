package io.reconquest.bitbucket.labels.ao;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.Table;

@Preload("*")
@Table("RqPrLabelItem")
public interface LabelItem extends Entity {
  @NotNull
  Label getLabel();

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "LABEL_ID")
  void setLabel(Label label);

  @NotNull
  int getProjectId();

  void setProjectId(int projectId);

  @NotNull
  int getRepositoryId();

  void setRepositoryId(int repositoryId);

  @NotNull
  long getPullRequestId();

  void setPullRequestId(long pullrequestId);
}
