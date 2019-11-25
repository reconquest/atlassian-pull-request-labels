package io.reconquest.bitbucket.labels.service;

import com.atlassian.activeobjects.external.ActiveObjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reconquest.bitbucket.labels.ao.LabelEntity;
import io.reconquest.bitbucket.labels.ao.LabelItem;
import io.reconquest.bitbucket.labels.ao.LabelLegacy;
import io.reconquest.bitbucket.labels.dao.LabelDao;
import net.java.ao.EntityStreamCallback;

public class MigrationLabelsV5 {
  private static Logger log = LoggerFactory.getLogger(MigrationLabelsV5.class.getSimpleName());

  public static String MIGRATION_KEY = "labels-to-v5";
  private String[] colors = {
    "#69D100", "#FFECDB", "#A295D6", "#5CB85C", "#A8D695", "#CC0033", "#8E44AD",
    "#D9534F", "#428BCA", "#AD8D43", "#34495E", "#D10069", "#FF0000", "#F0AD4E",
    "#AD4363", "#7F8C8D", "#0033CC", "#44AD8E", "#5843AD", "#004E00", "#D1D100"
  };

  private ActiveObjects ao;
  private LabelDao labelDao;

  private int colorsCursor = 0;
  private int cursor = 0;

  public MigrationLabelsV5(ActiveObjects ao, LabelDao labelDao) {
    this.ao = ao;
    this.labelDao = labelDao;
  }

  public void process() {
    int total = ao.count(LabelLegacy.class);
    ao.stream(LabelLegacy.class, new EntityStreamCallback<LabelLegacy, Integer>() {
      @Override
      public void onRowRead(LabelLegacy label) {
        cursor++;
        log.warn("[{}/{}] upgrading label", cursor, total);

        processLabel(label);
      }
    });

    int items = ao.count(LabelItem.class);
    int labels = ao.count(LabelEntity.class);

    log.warn("{} labels migrated to {} label-items and {} label-entities", total, items, labels);
  }

  private void processLabel(LabelLegacy legacy) {
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
