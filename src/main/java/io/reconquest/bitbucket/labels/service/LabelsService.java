package io.reconquest.bitbucket.labels.service;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reconquest.bitbucket.labels.ao.Migration;
import io.reconquest.bitbucket.labels.dao.LabelDao;
import io.reconquest.bitbucket.labels.dao.LabelLegacyDao;
import io.reconquest.bitbucket.labels.dao.MigrationDao;
import net.java.ao.EntityManager;

public class LabelsService {
  private static Logger log = LoggerFactory.getLogger(LabelsService.class.getSimpleName());
  public static String PLUGIN_KEY = "io.reconquest.bitbucket.labels";

  private EntityManager ao;
  private LabelDao labelDao;
  private LabelLegacyDao legacyDao;
  private MigrationDao migrationDao;

  public LabelsService(EntityManager ao) {
    this.ao = ao;

    this.labelDao = new LabelDao(ao);
    this.legacyDao = new LabelLegacyDao(ao);
    this.migrationDao = new MigrationDao(ao);
  }

  public void start() {
    try {
      this.migrate();
    } catch (SQLException e) {
      log.error("migration failed", e);
    }
  }

  public void migrate() throws SQLException {
    Migration fact = migrationDao.find(MigrationLabelsV5.MIGRATION_KEY);
    if (fact != null) {
      log.warn(
          "skipping migration {} because it is finished at {}",
          fact.getKey(),
          fact.getFinishedAt());
      return;
    }

    MigrationLabelsV5 legacyMigration = new MigrationLabelsV5(ao, labelDao);
    legacyMigration.process();

    fact = migrationDao.create(MigrationLabelsV5.MIGRATION_KEY);

    log.warn("migration {} has finished at {}", fact.getKey(), fact.getFinishedAt());
  }
}
