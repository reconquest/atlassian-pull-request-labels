package io.reconquest.bitbucket.labels.ao;

import net.java.ao.Entity;
import net.java.ao.ManyToMany;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.Table;

@Preload
@Table("v6_pr")
public interface PullRequestShadow extends Entity {
  @NotNull
  @ManyToMany(value = PullRequestShadowToLabel.class)
  Labez[] getLabels();

  @NotNull
  int getProjectId();

  void setProjectId(int projectid);

  @NotNull
  int getRepositoryId();

  void setRepositoryId(int repositoryid);

  @NotNull
  long getPullRequestId();

  void setPullRequestId(long repositoryid);
}
