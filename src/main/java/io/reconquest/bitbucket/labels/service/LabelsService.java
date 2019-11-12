package io.reconquest.bitbucket.labels.service;

import com.atlassian.activeobjects.external.ActiveObjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reconquest.bitbucket.labels.ao.AOLabel;
import io.reconquest.bitbucket.labels.ao.AOLabelItem;
import io.reconquest.bitbucket.labels.ao.LabelLegacy;
import io.reconquest.bitbucket.labels.dao.LabelDao;
import io.reconquest.bitbucket.labels.dao.LabelLegacyDao;
import net.java.ao.EntityStreamCallback;

public class LabelsService {
  private static Logger log = LoggerFactory.getLogger(LabelsService.class.getSimpleName());
  public static String PLUGIN_KEY = "io.reconquest.bitbucket.labels";

  private ActiveObjects activeObjects;
  private LabelDao labelDao;
  private LabelLegacyDao legacyDao;

  public LabelsService(ActiveObjects activeObjects) {
    this.activeObjects = activeObjects;

    this.labelDao = new LabelDao(activeObjects);
    this.legacyDao = new LabelLegacyDao(activeObjects);
  }

  public void start() {
    legacyDao.create(1, 1, 1, "feature");

    legacyDao.create(1, 1, 2, "bug");
    legacyDao.create(1, 1, 2, "hotfix");

    legacyDao.create(1, 1, 3, "docs");
    legacyDao.create(1, 1, 3, "tests");
    legacyDao.create(1, 1, 3, "legacy");
    legacyDao.create(1, 1, 3, "hotfix"); // the same label

    legacyDao.create(1, 1, 4, "feature"); // the same label
    legacyDao.create(1, 1, 4, "lgtm");

    LegacyMigration migration = new LegacyMigration();
    migration.migrate();
  }

  private class LegacyMigration {
    private String[] colors = {
      "#0033CC", "#428BCA", "#44AD8E", "#A8D695", "#5CB85C", "#69D100", "#004E00",
      "#34495E", "#7F8C8D", "#A295D6", "#5843AD", "#8E44AD", "#FFECDB", "#AD4363",
      "#D10069", "#CC0033", "#FF0000", "#D9534F", "#D1D100", "#F0AD4E", "#AD8D43"
    };

    private int colorsCursor = 0;

    int cursor = 0;

    public void migrate() {
      int total = activeObjects.count(LabelLegacy.class);
      activeObjects.stream(LabelLegacy.class, new EntityStreamCallback<LabelLegacy, Integer>() {
        @Override
        public void onRowRead(LabelLegacy label) {
          cursor++;
          log.warn("[{}/{}] upgrading labels to v5", cursor, total);
          migrateLabel(label);
        }
      });

      int items = activeObjects.count(AOLabelItem.class);
      int labels = activeObjects.count(AOLabel.class);

      log.warn("{} labels migrated to {} label-items and {} label-entities", total, items, labels);
    }

    private void migrateLabel(LabelLegacy legacy) {
      String color = colors[colorsCursor];

      labelDao.create(
          legacy.getProjectId(),
          legacy.getRepositoryId(),
          legacy.getPullRequestId(),
          legacy.getName(),
          color);

      colorsCursor++;
      if (colorsCursor >= colors.length) {
        colorsCursor = 0;
      }
    }
  }
}
