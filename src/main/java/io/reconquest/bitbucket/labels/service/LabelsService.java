package io.reconquest.bitbucket.labels.service;

import com.atlassian.activeobjects.external.ActiveObjects;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.reconquest.bitbucket.labels.StoreZ;
import io.reconquest.bitbucket.labels.ao.Labez;
import io.reconquest.bitbucket.labels.ao.PullRequestShadow;
import io.reconquest.bitbucket.labels.ao.PullRequestShadowToLabel;
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

    activeObjects.deleteWithSQL(PullRequestShadowToLabel.class, "ID > ?", 0);
    activeObjects.deleteWithSQL(PullRequestShadow.class, "ID > ?", 0);
    activeObjects.deleteWithSQL(Labez.class, "ID > ?", 0);

    store.create(100, 200, 301, "301_q", "#000000");
    store.create(100, 200, 302, "302_q", "#000000");
    store.create(100, 200, 301, "301_w", "#000000");
    store.create(100, 200, 302, "302_w", "#000000");

    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.DEBUG);

    // LabezItem[] results = store.find(100, 200, 301);
    Query query = Query.select()
        .from(PullRequestShadow.class)
        .alias(PullRequestShadow.class, "pr")
        .alias(PullRequestShadowToLabel.class, "pr_to_label")
        .alias(Labez.class, "labez")
        .where("pr.PULL_REQUEST_ID = ?", 301);

    PullRequestShadow[] results = this.activeObjects.find(PullRequestShadow.class, query);
    System.err.printf("XXXXXXX LabelsService.java:64 results.length %s \n", results.length);

    for (int i = 0; i < results.length; i++) {
      PullRequestShadow pr = results[i];
      Labez[] labels = pr.getLabels();

      System.err.printf(
          "XXXXXXX LabelsService.java:56 pr.getPullRequestId() %s \n", pr.getPullRequestId());
      System.err.printf("XXXXXXX LabelsService.java:57 labels %d\n", labels.length);

      for (Labez labez : labels) {
        System.err.printf("XXXXXXX LabelsService.java:51 labez.getName() %s \n", labez.getName());
        System.err.printf("XXXXXXX LabelsService.java:51 labez.getColor() %s \n", labez.getColor());
      }
    }

    root.setLevel(Level.INFO);
  }
}
