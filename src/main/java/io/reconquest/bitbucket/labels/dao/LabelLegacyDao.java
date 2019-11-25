package io.reconquest.bitbucket.labels.dao;

import com.atlassian.activeobjects.external.ActiveObjects;

import io.reconquest.bitbucket.labels.ao.LabelLegacy;
import net.java.ao.DBParam;
import net.java.ao.Query;

public class LabelLegacyDao {
  private final ActiveObjects ao;

  public LabelLegacyDao(ActiveObjects ao) {
    this.ao = ao;
  }

  public LabelLegacy[] find(int projectId, int repositoryId, long pullRequestId) {
    return this.ao.find(LabelLegacy.class, Query.select()
        .where(
            "PROJECT_ID = ? AND REPOSITORY_ID = ? AND PULL_REQUEST_ID = ?",
            projectId,
            repositoryId,
            pullRequestId));
  }

  public LabelLegacy[] find(int projectId, int repositoryId) {
    return this.ao.find(LabelLegacy.class, Query.select()
        .where("PROJECT_ID = ? AND REPOSITORY_ID = ?", projectId, repositoryId));
  }

  public LabelLegacy[] find(int projectId, int repositoryId, String name) {
    return this.ao.find(LabelLegacy.class, Query.select()
        .where(
            "PROJECT_ID = ? AND REPOSITORY_ID = ? AND NAME LIKE ?", projectId, repositoryId, name));
  }

  public LabelLegacy[] find(int projectId, int repositoryId, long pullRequestId, String name) {
    return this.ao.find(LabelLegacy.class, Query.select()
        .where(
            "PROJECT_ID = ? AND REPOSITORY_ID = ? AND PULL_REQUEST_ID = ? AND NAME LIKE ?",
            projectId,
            repositoryId,
            pullRequestId,
            name));
  }

  public LabelLegacy[] find(Integer[] repositories) {
    String[] ids = new String[repositories.length];
    for (int i = 0; i < repositories.length; i++) {
      ids[i] = String.valueOf(repositories[i]);
    }

    String query = String.join(",", ids);
    return this.ao.find(
        LabelLegacy.class, Query.select().where("REPOSITORY_ID IN (" + query + ")"));
  }

  public int countName(int projectId, int repositoryId, long pullRequestId, String name) {
    return this.ao.count(LabelLegacy.class, Query.select()
        .where(
            "PROJECT_ID = ? AND REPOSITORY_ID = ? AND PULL_REQUEST_ID = ? AND NAME LIKE ?",
            projectId,
            repositoryId,
            pullRequestId,
            name));
  }

  public void create(int projectId, int repositoryId, long pullRequestId, String name) {
    this.ao.create(
        LabelLegacy.class,
        new DBParam("PROJECT_ID", projectId),
        new DBParam("REPOSITORY_ID", repositoryId),
        new DBParam("PULL_REQUEST_ID", pullRequestId),
        new DBParam("NAME", name));
  }

  public void flush() {
    ao.flush();
  }

  public void delete(LabelLegacy[] labels) {
    ao.delete(labels);
  }
}
