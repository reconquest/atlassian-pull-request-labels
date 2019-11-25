package io.reconquest.bitbucket.labels.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;
import net.java.ao.schema.Unique;

@Preload({"LABEL_ID", "PROJECT_ID", "REPOSITORY_ID", "PULL_REQUEST_ID"})
@Table("v5items")
public interface LabelItem extends Entity {
  @NotNull
  int getLabelId();

  @NotNull
  int getProjectId();

  @NotNull
  int getRepositoryId();

  @NotNull
  long getPullRequestId();

  @NotNull
  @Unique
  String getHash();

  @StringLength(64)
  void setHash(String hash);
}
