package io.reconquest.bitbucket.labels.dao;

import java.util.Date;

import com.atlassian.activeobjects.external.ActiveObjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reconquest.bitbucket.labels.ao.Migration;
import net.java.ao.DBParam;
import net.java.ao.Query;

public class MigrationDao {
  private final ActiveObjects ao;
  private static Logger log = LoggerFactory.getLogger(MigrationDao.class.getSimpleName());

  public MigrationDao(ActiveObjects ao) {
    this.ao = ao;
  }

  public Migration create(String key) {
    return this.ao.create(
        Migration.class, new DBParam("KEY", key), new DBParam("FINISHED_AT", new Date()));
  }

  public Migration find(String key) {
    Migration[] migrations =
        this.ao.find(Migration.class, Query.select().from(Migration.class).where("KEY = ?", key));
    if (migrations.length == 0) {
      return null;
    }

    return migrations[0];
  }
}
