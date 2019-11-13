package io.reconquest.bitbucket.labels.dao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import com.atlassian.activeobjects.external.ActiveObjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reconquest.bitbucket.labels.Label;
import io.reconquest.bitbucket.labels.ao.LabelEntity;
import io.reconquest.bitbucket.labels.ao.LabelItem;
import net.java.ao.DBParam;
import net.java.ao.Query;

public class LabelDao {
  private final ActiveObjects ao;
  private static Logger log = LoggerFactory.getLogger(LabelDao.class.getSimpleName());

  public LabelDao(ActiveObjects ao) {
    this.ao = ao;
  }

  private Label[] find(String clause, Object... params) {
    LabelItem[] aoItems = this.ao.find(LabelItem.class, getJoinQuery(clause, params));

    Set<Integer> setLabelIds = new HashSet<Integer>();
    for (LabelItem item : aoItems) {
      setLabelIds.add(item.getLabelId());
    }

    Integer[] labelIds = setLabelIds.toArray(new Integer[0]);
    if (labelIds.length == 0) {
      return Label.Factory.getLabels(aoItems, null);
    }

    String condition = conditionIn(labelIds);

    LabelEntity[] aoLabels = this.ao.find(
        LabelEntity.class,
        Query.select().from(LabelEntity.class).where("ID in (" + condition + ")"));

    return Label.Factory.getLabels(aoItems, aoLabels);
  }

  public Label[] find(int projectId, int repositoryId, long pullRequestId) {
    return this.find(
        "item.PROJECT_ID = ? AND item.REPOSITORY_ID = ? AND item.PULL_REQUEST_ID = ?",
        projectId,
        repositoryId,
        pullRequestId);
  }

  public Label[] find(int projectId, int repositoryId) {
    return this.find("item.PROJECT_ID = ? AND item.REPOSITORY_ID = ?", projectId, repositoryId);
  }

  public Label[] find(int projectId, int repositoryId, String name) {
    return this.find(
        "item.PROJECT_ID = ? AND item.REPOSITORY_ID = ? AND label.NAME LIKE ?",
        projectId,
        repositoryId,
        name);
  }

  public Label[] find(int projectId, int repositoryId, long pullRequestId, String name) {
    return this.find(
        "item.PROJECT_ID = ? AND item.REPOSITORY_ID = ? AND item.PULL_REQUEST_ID = ?"
            + " AND label.NAME LIKE ?",
        projectId,
        repositoryId,
        pullRequestId,
        name);
  }

  public Label[] find(Integer[] repositories) {
    String[] ids = new String[repositories.length];
    for (int i = 0; i < repositories.length; i++) {
      ids[i] = String.valueOf(repositories[i]);
    }

    String query = String.join(",", ids);
    return this.find("item.REPOSITORY_ID IN (" + query + ")");
  }

  public LabelEntity createLabelEntity(int projectId, int repositoryId, String name, String color) {
    try {
      LabelEntity label = this.ao.create(
          LabelEntity.class,
          new DBParam("PROJECT_ID", projectId),
          new DBParam("REPOSITORY_ID", repositoryId),
          new DBParam("NAME", name),
          new DBParam("COLOR", color),
          new DBParam("HASH", hash(projectId, repositoryId, name)));
      return label;
    } catch (Exception e) { // No way to handle duplicate hash
      LabelEntity[] labels = this.ao.find(LabelEntity.class, Query.select()
          .from(LabelEntity.class)
          .where(
              "PROJECT_ID = ? AND REPOSITORY_ID = ? AND NAME = ?", projectId, repositoryId, name));
      if (labels.length == 0) {
        // throw what we have if we can't find the same label
        throw e;
      }

      return labels[0];
    }
  }

  public LabelItem createLabelItem(
      int projectId, int repositoryId, long pullRequestId, LabelEntity label) {
    try {
      return this.ao.create(
          LabelItem.class,
          new DBParam("LABEL_ID", label.getID()),
          new DBParam("PROJECT_ID", projectId),
          new DBParam("REPOSITORY_ID", repositoryId),
          new DBParam("PULL_REQUEST_ID", pullRequestId),
          new DBParam("HASH", hash(projectId, repositoryId, pullRequestId, label.getID())));
    } catch (Exception e) { // No way to handle duplicate hash
      LabelItem[] items = this.ao.find(LabelItem.class, Query.select()
          .from(LabelItem.class)
          .where(
              "PROJECT_ID = ? AND REPOSITORY_ID = ? AND PULL_REQUEST_ID = ? AND LABEL_ID = ?",
              projectId,
              repositoryId,
              pullRequestId,
              label.getID()));
      if (items.length == 0) {
        // throw what we have if we can't find the same label
        throw e;
      }

      return items[0];
    }
  }

  public int create(
      int projectId, int repositoryId, long pullRequestId, String name, String color) {
    LabelEntity label = createLabelEntity(projectId, repositoryId, name, color);
    LabelItem item = createLabelItem(projectId, repositoryId, pullRequestId, label);
    return item.getID();
  }

  public void update(int projectId, int repositoryId, int labelId, String name, String color)
      throws Exception {
    LabelEntity[] labels = this.ao.find(LabelEntity.class, Query.select()
        .from(LabelEntity.class)
        .where(
            "PROJECT_ID = ? AND REPOSITORY_ID = ? AND ID = ?", projectId, repositoryId, labelId));
    if (labels.length == 0) {
      log.warn("no labels found with such conditions");
      return;
    }

    LabelEntity label = labels[0];

    label.setName(name);
    label.setColor(color);
    label.setHash(hash(projectId, repositoryId, name));

    label.save();
  }

  private Query getJoinQuery(String clause, Object... params) {
    return Query.select()
        .from(LabelItem.class)
        .alias(LabelItem.class, "item")
        .join(LabelEntity.class, "label.ID = item.LABEL_ID")
        .alias(LabelEntity.class, "label")
        .where(clause, params);
  }

  public void flush() {
    ao.flush();
  }

  public void deleteItems(Label[] labels) {
    ao.deleteWithSQL(LabelItem.class, "ID IN (" + conditionIn(getItemIds(labels)) + ")");
  }

  // public String hash(LabelEntity label) {
  //  return hash(label.getProjectId(), label.getRepositoryId(), label.getName());
  // }

  private String hash(Object... objects) {
    String[] strings = new String[objects.length];
    for (int i = 0; i < objects.length; i++) {
      strings[i] = String.valueOf(objects[i]);
    }

    return hash(strings);
  }

  private String hash(String... strings) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encoded = digest.digest(String.join("@", strings).getBytes(StandardCharsets.UTF_8));
      return bytesToHex(encoded);
    } catch (NoSuchAlgorithmException e) {
      log.error("unable to encode label hash", e);
      return "";
    }
  }

  private String hash(int projectId, int repositoryId, String name) {
    return hash(String.valueOf(projectId), String.valueOf(repositoryId), name);
  }

  private String bytesToHex(byte[] hash) {
    StringBuffer hexString = new StringBuffer();
    for (int i = 0; i < hash.length; i++) {
      String hex = Integer.toHexString(0xff & hash[i]);
      if (hex.length() == 1) hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }

  private Integer[] getItemIds(Label[] labels) {
    Integer[] ids = new Integer[labels.length];
    for (int i = 0; i < labels.length; i++) {
      ids[i] = Integer.valueOf(labels[i].getItemId());
    }
    return ids;
  }

  private <T> String conditionIn(T[] items) {
    String[] strings = new String[items.length];
    for (int i = 0; i < items.length; i++) {
      strings[i] = String.valueOf(items[i]);
    }

    return String.join(",", strings);
  }
}
