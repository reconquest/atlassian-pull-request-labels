package io.reconquest.bitbucket.labels.ao;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.Table;

@Preload("*")
@Table("v2items")
public interface AOLabelItem extends Entity {
  @NotNull
  int getLabelId();

  @NotNull
  int getProjectId();

  @NotNull
  int getRepositoryId();

  @NotNull
  long getPullRequestId();

  void setPullRequestId(long pullrequestId);
}
