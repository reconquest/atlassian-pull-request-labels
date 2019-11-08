package io.reconquest.bitbucket.labels;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atlassian.activeobjects.external.ActiveObjects;

import io.reconquest.bitbucket.labels.ao.Labez;
import io.reconquest.bitbucket.labels.ao.LabezItem;
import net.java.ao.DBParam;

public class StoreZ {
  private final ActiveObjects ao;
  private static Logger log = Logger.getLogger(StoreZ.class.getSimpleName());

  public StoreZ(ActiveObjects ao) {
    this.ao = ao;
  }

  // public LabezItem[] find(int projectId, int repositoryId, long pullRequestId) {
  //  return this.ao.find(
  //      LabezItem.class,
  //      select(
  //          "item.PROJECT_ID = ? AND item.REPOSITORY_ID = ? AND item.PULL_REQUEST_ID = ?",
  //          projectId,
  //          repositoryId,
  //          pullRequestId));
  // }

  // private Query select(String clause, Object... params) {
  //  return Query.select()
  //      .from(LabezItem.class)
  //      .alias(LabezItem.class, "item")
  //      .join(Labez.class, "label.ID = item.LABEZ_ID")
  //      .alias(Labez.class, "label")
  //      .where(clause, params);
  // }

  // public LabezItem[] find(int projectId, int repositoryId) {
  //  return this.ao.find(
  //      LabezItem.class,
  //      select("item.PROJECT_ID = ? AND item.REPOSITORY_ID = ?", projectId, repositoryId));
  // }

  // public LabezItem[] find(int projectId, int repositoryId, String name) {
  //  return this.ao.find(
  //      LabezItem.class,
  //      select(
  //          "item.PROJECT_ID = ? AND item.REPOSITORY_ID = ? AND label.NAME LIKE ?",
  //          projectId,
  //          repositoryId,
  //          name));
  // }

  // public LabezItem[] find(int projectId, int repositoryId, long pullRequestId, String name) {
  //  return this.ao.find(
  //      LabezItem.class,
  //      select(
  //          "item.PROJECT_ID = ? AND item.REPOSITORY_ID = ? AND item.PULL_REQUEST_ID = ?"
  //              + " AND label.NAME LIKE ?",
  //          projectId,
  //          repositoryId,
  //          pullRequestId,
  //          name));
  // }

  // public LabezItem[] find(Integer[] repositories) {
  //  String[] ids = new String[repositories.length];
  //  for (int i = 0; i < repositories.length; i++) {
  //    ids[i] = String.valueOf(repositories[i]);
  //  }

  //  String query = String.join(",", ids);
  //  return this.ao.find(LabezItem.class, select("item.REPOSITORY_ID IN (" + query + ")"));
  // }

  // public int countName(int projectId, int repositoryId, long pullRequestId, String name) {
  //  return this.ao.count(
  //      LabezItem.class,
  //      select(
  //          "item.PROJECT_ID = ? AND item.REPOSITORY_ID = ? AND item.PULL_REQUEST_ID = ?"
  //              + " AND label.NAME LIKE ?",
  //          projectId,
  //          repositoryId,
  //          pullRequestId,
  //          name));
  // }

  public Labez createLabez(int projectId, int repositoryId, String name, String color) {
    return this.ao.create(
        Labez.class,
        new DBParam("PROJECT_ID", projectId),
        new DBParam("REPOSITORY_ID", repositoryId),
        new DBParam("NAME", name),
        new DBParam("COLOR", color),
        new DBParam("HASH", hash(projectId, repositoryId, name)));
  }

  public LabezItem createLabezItem(
      int projectId, int repositoryId, long pullRequestId, Labez label) {
    System.err.printf("XXXXXXX StoreZ.java:104 creating labelz_id \n");
    return this.ao.create(
        LabezItem.class,
        new DBParam("LABEZ_ID", label.getID()),
        new DBParam("PROJECT_ID", projectId),
        new DBParam("REPOSITORY_ID", repositoryId),
        new DBParam("PULL_REQUEST_ID", pullRequestId));
  }

  public LabezItem create(
      int projectId, int repositoryId, long pullRequestId, String name, String color) {
    Labez label = createLabez(projectId, repositoryId, name, color);
    return createLabezItem(projectId, repositoryId, pullRequestId, label);
  }

  public void flush() {
    ao.flush();
  }

  public void delete(LabezItem[] labels) {
    ao.delete(labels);
  }

  public String hash(Labez label) {
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

  // public LabezItem[] find(LabezItem[] items) {
  //  if (items.length == 0) {
  //    return null;
  //  }

  //  return this.ao.find(Labez.class, select("ID IN (" + ids(items) + ")"));
  // }

  // public String ids(LabezItem[] items) {
  //  String[] strings = new String[items.length];
  //  for (int i = 0; i < items.length; i++) {
  //    strings[i] = String.valueOf(items[i].getID());
  //  }

  //  return String.join(",", strings);
  // }

}
