package io.reconquest.bitbucket.labels.service;

import com.atlassian.activeobjects.external.ActiveObjects;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.reconquest.bitbucket.labels.StoreZ;
import io.reconquest.bitbucket.labels.ao.Labez;
import io.reconquest.bitbucket.labels.ao.LabezItem;
import net.java.ao.Query;

public class LabelsService {
  public static String PLUGIN_KEY = "io.reconquest.bitbucket.labels";

  private ActiveObjects activeObjects;
  private StoreZ store;

  public LabelsService(ActiveObjects activeObjects) {
    this.activeObjects = activeObjects;

    this.store = new StoreZ(activeObjects);
  }

  public void start() {
    System.err.printf("XXXXXXX LabelsService.java:60 start() invoked \n");
    System.err.printf("XXXXXXX LabelsService.java:60 truncate \n");

    System.err.printf("XXXXXXX LabelsService.java:61 deleting labelZ \n");
    System.err.printf("XXXXXXX LabelsService.java:63 deleting Labez Item \n");
    activeObjects.deleteWithSQL(LabezItem.class, "ID > ?", 0);
    activeObjects.deleteWithSQL(Labez.class, "ID > ?", 0);

    store.create(100, 200, 301, "301_q", "#000000");
    store.create(100, 200, 302, "302_q", "#000000");
    store.create(100, 200, 301, "301_w", "#000000");
    store.create(100, 200, 302, "302_w", "#000000");

    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.DEBUG);

    // LabezItem[] results = store.find(100, 200, 301);
    Query query = Query.select()
        .from(LabezItem.class)
        .alias(LabezItem.class, "item")
        .where("item.PULL_REQUEST_ID = ?", 301);

    LabezItem[] results = this.activeObjects.find(LabezItem.class, query);
    System.err.printf("XXXXXXX LabelsService.java:64 results.length %s \n", results.length);

    for (int i = 0; i < results.length; i++) {
      LabezItem x = results[i];
      System.err.printf(
          "XXXXXXX LabelsService.java:55 x.getPullRequestId() %s \n", x.getPullRequestId());
      System.err.printf("XXXXXXX LabelsService.java:57 x name %s \n", x.getLabez().getName());
      System.err.printf("XXXXXXX LabelsService.java:57 x color %s \n", x.getLabez().getColor());
    }

    root.setLevel(Level.INFO);
  }
}
