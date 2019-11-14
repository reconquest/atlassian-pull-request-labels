package io.reconquest.bitbucket.labels.service;

import com.atlassian.activeobjects.external.ActiveObjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reconquest.bitbucket.labels.ao.Migration;
import io.reconquest.bitbucket.labels.dao.LabelDao;
import io.reconquest.bitbucket.labels.dao.LabelLegacyDao;
import io.reconquest.bitbucket.labels.dao.MigrationDao;

public class LabelsService {
  private static Logger log = LoggerFactory.getLogger(LabelsService.class.getSimpleName());
  public static String PLUGIN_KEY = "io.reconquest.bitbucket.labels";

  private ActiveObjects ao;
  private LabelDao labelDao;
  private LabelLegacyDao legacyDao;
  private MigrationDao migrationDao;

  public LabelsService(ActiveObjects ao) {
    this.ao = ao;

    this.labelDao = new LabelDao(ao);
    this.legacyDao = new LabelLegacyDao(ao);
    this.migrationDao = new MigrationDao(ao);
  }

  public void start() {
    this.migrate();
  }

  public void migrate() {
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
