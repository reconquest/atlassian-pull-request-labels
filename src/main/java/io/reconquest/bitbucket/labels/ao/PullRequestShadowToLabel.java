package io.reconquest.bitbucket.labels.ao;

import io.reconquest.bitbucket.labels.ao.PullRequestShadow;
import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.Table;

@Preload
@Table("v7_pr_to_label")
public interface PullRequestShadowToLabel extends Entity {
  @NotNull
  Labez getLabel();

  void setLabel(Labez label);

  PullRequestShadow getPullRequestShadow();

  void setPullRequestShadow(PullRequestShadow pullRequestShadow);
}
