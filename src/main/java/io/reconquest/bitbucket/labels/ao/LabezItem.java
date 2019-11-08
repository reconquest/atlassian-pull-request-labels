package io.reconquest.bitbucket.labels.ao;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.Table;

@Preload({"LABEZ_ID", "PULL_REQUEST_ID", "PROJECT_ID", "REPOSITORY_ID"})
@Table("qqlabez")
public interface LabezItem extends Entity {
  @NotNull
  Labez getLabez();

  void setLabez(Labez label);

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
