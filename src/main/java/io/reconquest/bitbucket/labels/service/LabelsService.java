package io.reconquest.bitbucket.labels.service;

import com.atlassian.activeobjects.external.ActiveObjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import ch.qos.logback.classic.Logger;
import io.reconquest.bitbucket.labels.Store;
import io.reconquest.bitbucket.labels.StoreLegacy;
import io.reconquest.bitbucket.labels.ao.LabelLegacy;
import net.java.ao.EntityStreamCallback;

public class LabelsService {
  private static Logger log = LoggerFactory.getLogger(LabelsService.class.getSimpleName());
  public static String PLUGIN_KEY = "io.reconquest.bitbucket.labels";

  private ActiveObjects activeObjects;
  private Store store;
  private StoreLegacy storeLegacy;

  public LabelsService(ActiveObjects activeObjects) {
    this.activeObjects = activeObjects;

    this.store = new Store(activeObjects);
    this.storeLegacy = new StoreLegacy(activeObjects);
  }

  public void start() {
    LegacyMigration migration = new LegacyMigration();
    migration.migrate();
  }

  private class LegacyMigration {
    private String[] migrationColors = {
      "#0033CC", "#428BCA", "#44AD8E", "#A8D695", "#5CB85C", "#69D100", "#004E00",
      "#34495E", "#7F8C8D", "#A295D6", "#5843AD", "#8E44AD", "#FFECDB", "#AD4363",
      "#D10069", "#CC0033", "#FF0000", "#D9534F", "#D1D100", "#F0AD4E", "#AD8D43"
    };

    public void migrate() {
      activeObjects.stream(LabelLegacy.class, new EntityStreamCallback<LabelLegacy, Integer>() {
        @Override
        public void onRowRead(LabelLegacy label) {
          migrateLabel(label);
        }
      });
    }

    private void migrateLabel(LabelLegacy legacy) {
      System.err.printf("XXXXXXX LabelsService.java:54 legacy %s \n", legacy);
      //
    }
  }
}
