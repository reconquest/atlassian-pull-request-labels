package io.reconquest.bitbucket.labels.service;

import com.atlassian.activeobjects.external.ActiveObjects;

import io.reconquest.bitbucket.labels.Store;

public class LabelsService {
  public static String PLUGIN_KEY = "io.reconquest.bitbucket.labels";

  private ActiveObjects activeObjects;
  private Store store;

  public LabelsService(ActiveObjects activeObjects) {
    this.activeObjects = activeObjects;

    this.store = new Store(activeObjects);
  }

  public void start() {
    System.err.printf("XXXXXXX LabelsService.java:60 start() invoked \n");

    // try {
    //  store.create(1, 1, 1, "1_q", "#0000ff");
    //  store.create(1, 1, 1, "1_w", "#00ffff");
    //  store.create(1, 1, 2, "2_q", "#0000ff");
    //  store.create(1, 1, 2, "2_w", "#00ffff");

    //  store.create(1, 1, 2, "2_w", "#00ffff");
    // } catch (Exception e) {
    //  System.err.printf("XXXXXXX LabelsService.java:31 e.getClass() %s \n", e.getClass());
    // }

    // LabelItem[] items = store.find(1, 1);
    // for (int i = 0; i < items.length; i++) {
    //  LabelItem item = items[i];

    //  System.err.printf(
    //      "XXXXXXX id: %s, p_id: %s, r_id: %s, l_id: %s, name: %s, color: %s \n",
    //      item.getID(),
    //      item.getProjectId(),
    //      item.getRepositoryId(),
    //      item.getLabel().getID(),
    //      item.getLabel().getName(),
    //      item.getLabel().getColor());
    // }

    // activeObjects.deleteWithSQL(PullRequestShadowToLabel.class, "ID > ?", 0);
    // activeObjects.deleteWithSQL(PullRequestShadow.class, "ID > ?", 0);
    // activeObjects.deleteWithSQL(Labez.class, "ID > ?", 0);

    // store.create(100, 200, 301, "301_q", "#000000");
    // store.create(100, 200, 302, "302_q", "#000000");
    // store.create(100, 200, 301, "301_w", "#000000");
    // store.create(100, 200, 302, "302_w", "#000000");

    // Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    // root.setLevel(Level.DEBUG);

    //// LabezItem[] results = store.find(100, 200, 301);
    // Query query = Query.select()
    //    .from(PullRequestShadow.class)
    //    .alias(PullRequestShadow.class, "pr")
    //    .alias(PullRequestShadowToLabel.class, "pr_to_label")
    //    .alias(Labez.class, "labez")
    //    .where("pr.PULL_REQUEST_ID = ?", 301);

    // PullRequestShadow[] results = this.activeObjects.find(PullRequestShadow.class, query);
    // System.err.printf("XXXXXXX LabelsService.java:64 results.length %s \n", results.length);

    // for (int i = 0; i < results.length; i++) {
    //  PullRequestShadow pr = results[i];
    //  Labez[] labels = pr.getLabels();

    //  System.err.printf(
    //      "XXXXXXX LabelsService.java:56 pr.getPullRequestId() %s \n", pr.getPullRequestId());
    //  System.err.printf("XXXXXXX LabelsService.java:57 labels %d\n", labels.length);

    //  for (Labez labez : labels) {
    //    System.err.printf("XXXXXXX LabelsService.java:51 labez.getName() %s \n", labez.getName());
    //    System.err.printf("XXXXXXX LabelsService.java:51 labez.getColor() %s \n",
    // labez.getColor());
    //  }
    // }

    // root.setLevel(Level.INFO);
  }
}
