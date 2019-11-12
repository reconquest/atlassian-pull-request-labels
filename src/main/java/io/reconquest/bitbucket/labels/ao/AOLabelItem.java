package io.reconquest.bitbucket.labels.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.Table;

@Preload({"LABEL_ID", "PROJECT_ID", "REPOSITORY_ID", "PULL_REQUEST_ID"})
@Table("v3items")
public interface AOLabelItem extends Entity {
  @NotNull
  int getLabelId();

  @NotNull
  int getProjectId();

  @NotNull
  int getRepositoryId();

  @NotNull
  long getPullRequestId();
}
