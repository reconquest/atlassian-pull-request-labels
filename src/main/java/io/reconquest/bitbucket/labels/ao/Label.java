package io.reconquest.bitbucket.labels.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

@Preload
@Table("PullRequestLabels")
public interface Label extends Entity {
  @NotNull
  String getName();

  @StringLength(250)
  void setName(String name);

  @NotNull
  String getColor();

  @StringLength(16)
  void setColor(String color);

  @NotNull
  int getProjectId();

  void setProjectId(int projectid);

  @NotNull
  int getRepositoryId();

  void setRepositoryId(int repositoryid);

  @NotNull
  Long getPullRequestId();

  void setPullRequestId(Long pullrequestid);
}
