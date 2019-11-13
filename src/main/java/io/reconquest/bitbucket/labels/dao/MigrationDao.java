package io.reconquest.bitbucket.labels.dao;

import java.sql.SQLException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reconquest.bitbucket.labels.ao.Migration;
import net.java.ao.DBParam;
import net.java.ao.EntityManager;
import net.java.ao.Query;

public class MigrationDao {
  private final EntityManager ao;
  private static Logger log = LoggerFactory.getLogger(MigrationDao.class.getSimpleName());

  public MigrationDao(EntityManager ao) {
    this.ao = ao;
  }

  public Migration create(String key) throws SQLException {
    return this.ao.create(
        Migration.class, new DBParam("KEY", key), new DBParam("FINISHED_AT", new Date()));
  }

  public Migration find(String key) throws SQLException {
    Migration[] migrations =
        this.ao.find(Migration.class, Query.select().from(Migration.class).where("KEY = ?", key));
    if (migrations.length == 0) {
      return null;
    }

    return migrations[0];
  }
}
