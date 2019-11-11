package io.reconquest.bitbucket.labels;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atlassian.activeobjects.external.ActiveObjects;

import io.reconquest.bitbucket.labels.ao.Label;
import io.reconquest.bitbucket.labels.ao.LabelItem;
import net.java.ao.DBParam;
import net.java.ao.Query;

public class Store {
  private final ActiveObjects ao;
  private static Logger log = Logger.getLogger(Store.class.getSimpleName());

  public Store(ActiveObjects ao) {
    this.ao = ao;
  }

  public LabelItem[] find(int projectId, int repositoryId, long pullRequestId) {
    return this.ao.find(
        LabelItem.class,
        select(
            "item.PROJECT_ID = ? AND item.REPOSITORY_ID = ? AND item.PULL_REQUEST_ID = ?",
            projectId,
            repositoryId,
            pullRequestId));
  }

  private Query select(String clause, Object... params) {
    return Query.select()
        .from(LabelItem.class)
        .alias(LabelItem.class, "item")
        .join(Label.class, "label.ID = item.LABEL_ID")
        .alias(Label.class, "label")
        .where(clause, params);
  }

  public LabelItem[] find(int projectId, int repositoryId) {
    return this.ao.find(
        LabelItem.class,
        select("item.PROJECT_ID = ? AND item.REPOSITORY_ID = ?", projectId, repositoryId));
  }

  public LabelItem[] find(int projectId, int repositoryId, String name) {
    return this.ao.find(
        LabelItem.class,
        select(
            "item.PROJECT_ID = ? AND item.REPOSITORY_ID = ? AND label.NAME LIKE ?",
            projectId,
            repositoryId,
            name));
  }

  public LabelItem[] find(int projectId, int repositoryId, long pullRequestId, String name) {
    return this.ao.find(
        LabelItem.class,
        select(
            "item.PROJECT_ID = ? AND item.REPOSITORY_ID = ? AND item.PULL_REQUEST_ID = ?"
                + " AND label.NAME LIKE ?",
            projectId,
            repositoryId,
            pullRequestId,
            name));
  }

  public LabelItem[] find(Integer[] repositories) {
    String[] ids = new String[repositories.length];
    for (int i = 0; i < repositories.length; i++) {
      ids[i] = String.valueOf(repositories[i]);
    }

    String query = String.join(",", ids);
    return this.ao.find(LabelItem.class, select("item.REPOSITORY_ID IN (" + query + ")"));
  }

  public int countName(int projectId, int repositoryId, long pullRequestId, String name) {
    return this.ao.count(
        LabelItem.class,
        select(
            "item.PROJECT_ID = ? AND item.REPOSITORY_ID = ? AND item.PULL_REQUEST_ID = ?"
                + " AND label.NAME LIKE ?",
            projectId,
            repositoryId,
            pullRequestId,
            name));
  }

  public Label createLabel(int projectId, int repositoryId, String name, String color) {
    try {
      Label label = this.ao.create(
          Label.class,
          new DBParam("PROJECT_ID", projectId),
          new DBParam("REPOSITORY_ID", repositoryId),
          new DBParam("NAME", name),
          new DBParam("COLOR", color),
          new DBParam("HASH", hash(projectId, repositoryId, name)));
      return label;
    } catch (Exception e) { // No way to handle duplicate hash
      Label[] labels = this.ao.find(Label.class, Query.select()
          .from(Label.class)
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
      int projectId, int repositoryId, long pullRequestId, Label label) {
    return this.ao.create(
        LabelItem.class,
        new DBParam("LABEL_ID", label.getID()),
        new DBParam("PROJECT_ID", projectId),
        new DBParam("REPOSITORY_ID", repositoryId),
        new DBParam("PULL_REQUEST_ID", pullRequestId));
  }

  public LabelItem create(
      int projectId, int repositoryId, long pullRequestId, String name, String color) {
    Label label = createLabel(projectId, repositoryId, name, color);
    return createLabelItem(projectId, repositoryId, pullRequestId, label);
  }

  public void update(int projectId, int repositoryId, int labelId, String name, String color)
      throws Exception {
    Label[] labels = this.ao.find(Label.class, Query.select()
        .from(Label.class)
        .where(
            "PROJECT_ID = ? AND REPOSITORY_ID = ? AND ID = ?", projectId, repositoryId, labelId));
    if (labels.length == 0) {
      log.warning("no labels found with such conditions");
      return;
    }

    Label label = labels[0];

    label.setName(name);
    label.setColor(color);
    label.setHash(hash(projectId, repositoryId, name));

    label.save();
  }

  public void flush() {
    ao.flush();
  }

  public void delete(LabelItem[] labels) {
    ao.delete(labels);
  }

  public String hash(Label label) {
    return hash(label.getProjectId(), label.getRepositoryId(), label.getName());
  }

  private String hash(int projectId, int repositoryId, String name) {
    String token = String.valueOf(projectId) + "@" + String.valueOf(repositoryId) + "@" + name;
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encoded = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      return bytesToHex(encoded);
    } catch (NoSuchAlgorithmException e) {
      log.log(Level.SEVERE, "unable to encode label hash", e);
      return "";
    }
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

  // public LabelItem[] find(LabelItem[] items) {
  //  if (items.length == 0) {
  //    return null;
  //  }

  //  return this.ao.find(Label.class, select("ID IN (" + ids(items) + ")"));
  // }

  // public String ids(LabelItem[] items) {
  //  String[] strings = new String[items.length];
  //  for (int i = 0; i < items.length; i++) {
  //    strings[i] = String.valueOf(items[i].getID());
  //  }

  //  return String.join(",", strings);
  // }

}
