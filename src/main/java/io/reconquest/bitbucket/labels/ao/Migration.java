package io.reconquest.bitbucket.labels.ao;

import java.util.Date;

import net.java.ao.Entity;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;
import net.java.ao.schema.Unique;

@Table("migrations")
public interface Migration extends Entity {
  @NotNull
  @StringLength(64)
  @Unique
  String getKey();

  @NotNull
  Date getFinishedAt();
}
