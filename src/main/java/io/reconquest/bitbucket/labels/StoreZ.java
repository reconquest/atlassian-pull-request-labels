package io.reconquest.bitbucket.labels;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atlassian.activeobjects.external.ActiveObjects;

import io.reconquest.bitbucket.labels.ao.Labez;
import io.reconquest.bitbucket.labels.ao.PullRequestShadow;
import io.reconquest.bitbucket.labels.ao.PullRequestShadowToLabel;
import net.java.ao.DBParam;

public class StoreZ {
  private final ActiveObjects ao;
  private static Logger log = Logger.getLogger(StoreZ.class.getSimpleName());

  public StoreZ(ActiveObjects ao) {
    this.ao = ao;
  }

  // public PullRequestShadow[] find(int projectId, int repositoryId, long pullRequestId) {
  //  return this.ao.find(
  //      PullRequestShadow.class,
  //      select(
  //          "item.PROJECT_ID = ? AND item.REPOSITORY_ID = ? AND item.PULL_REQUEST_ID = ?",
  //          projectId,
  //          repositoryId,
  //          pullRequestId));
  // }

  // private Query select(String clause, Object... params) {
  //  return Query.select()
  //      .from(PullRequestShadow.class)
  //      .alias(PullRequestShadow.class, "item")
  //      .join(Labez.class, "label.ID = item.LABEZ_ID")
  //      .alias(Labez.class, "label")
  //      .where(clause, params);
  // }

  // public PullRequestShadow[] find(int projectId, int repositoryId) {
  //  return this.ao.find(
  //      PullRequestShadow.class,
  //      select("item.PROJECT_ID = ? AND item.REPOSITORY_ID = ?", projectId, repositoryId));
  // }

  // public PullRequestShadow[] find(int projectId, int repositoryId, String name) {
  //  return this.ao.find(
  //      PullRequestShadow.class,
  //      select(
  //          "item.PROJECT_ID = ? AND item.REPOSITORY_ID = ? AND label.NAME LIKE ?",
  //          projectId,
  //          repositoryId,
  //          name));
  // }

  // public PullRequestShadow[] find(int projectId, int repositoryId, long pullRequestId, String
  // name) {
  //  return this.ao.find(
  //      PullRequestShadow.class,
  //      select(
  //          "item.PROJECT_ID = ? AND item.REPOSITORY_ID = ? AND item.PULL_REQUEST_ID = ?"
  //              + " AND label.NAME LIKE ?",
  //          projectId,
  //          repositoryId,
  //          pullRequestId,
  //          name));
  // }

  // public PullRequestShadow[] find(Integer[] repositories) {
  //  String[] ids = new String[repositories.length];
  //  for (int i = 0; i < repositories.length; i++) {
  //    ids[i] = String.valueOf(repositories[i]);
  //  }

  //  String query = String.join(",", ids);
  //  return this.ao.find(PullRequestShadow.class, select("item.REPOSITORY_ID IN (" + query + ")"));
  // }

  // public int countName(int projectId, int repositoryId, long pullRequestId, String name) {
  //  return this.ao.count(
  //      PullRequestShadow.class,
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

  public PullRequestShadow createPullRequestShadow(
      int projectId, int repositoryId, long pullRequestId) {
    return this.ao.create(
        PullRequestShadow.class,
        new DBParam("PROJECT_ID", projectId),
        new DBParam("REPOSITORY_ID", repositoryId),
        new DBParam("PULL_REQUEST_ID", pullRequestId));
  }

  public PullRequestShadowToLabel createPullRequestShadowToLabel(
      PullRequestShadow pullRequestShadow, Labez labez) {
    return this.ao.create(
        PullRequestShadowToLabel.class, new DBParam("LABEL_ID", labez.getID()), new DBParam(
            "PULL_REQUEST_SHADOW_ID", pullRequestShadow.getID()));
  }

  public void create(
      int projectId, int repositoryId, long pullRequestId, String name, String color) {
    Labez label = createLabez(projectId, repositoryId, name, color);
    PullRequestShadow pullRequestShadow =
        createPullRequestShadow(projectId, repositoryId, pullRequestId);

    createPullRequestShadowToLabel(pullRequestShadow, label);
  }

  public void flush() {
    ao.flush();
  }

  public void delete(PullRequestShadow[] labels) {
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

  // public PullRequestShadow[] find(PullRequestShadow[] items) {
  //  if (items.length == 0) {
  //    return null;
  //  }

  //  return this.ao.find(Labez.class, select("ID IN (" + ids(items) + ")"));
  // }

  // public String ids(PullRequestShadow[] items) {
  //  String[] strings = new String[items.length];
  //  for (int i = 0; i < items.length; i++) {
  //    strings[i] = String.valueOf(items[i].getID());
  //  }

  //  return String.join(",", strings);
  // }

}
