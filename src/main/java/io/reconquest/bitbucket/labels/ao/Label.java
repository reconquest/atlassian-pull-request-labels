package io.reconquest.bitbucket.labels.ao;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;
import net.java.ao.schema.Unique;

@Preload("*")
@Table("RqPrLabels")
public interface Label extends Entity {
  @NotNull
  String getName();

  @StringLength(250)
  void setName(String name);

  String getColor();

  @StringLength(250)
  void setColor(String color);

  @NotNull
  int getProjectId();

  void setProjectId(int projectid);

  @NotNull
  int getRepositoryId();

  void setRepositoryId(int repositoryid);

  @NotNull
  @Unique
  String getHash();

  @StringLength(64)
  void setHash(String hash);
}
