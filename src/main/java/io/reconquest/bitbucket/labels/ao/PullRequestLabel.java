package io.reconquest.bitbucket.labels.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

@Preload
@Table("PullRequestLabels")
public interface PullRequestLabel extends Entity {
  /** It exists here for back-compatibility, do not use that. */
  @NotNull
  @Deprecated
  String getName();

  @StringLength(250)
  @Deprecated
  void setName(String name);

  @NotNull
  String getLabelId();

  void setLabelId(int labelId);

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
