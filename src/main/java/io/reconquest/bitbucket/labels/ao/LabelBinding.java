package io.reconquest.bitbucket.labels.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

@Preload
@Table("RqPrLabelBinding")
public interface LabelBinding extends Entity {
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
