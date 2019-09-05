package io.reconquest.bitbucket.labels.rest;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Logger;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.FINEST;

import io.reconquest.bitbucket.labels.ao.*;

import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.repository.Repository;

import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.pull.PullRequest;

import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.project.Project;

import net.java.ao.DBParam;
import net.java.ao.Query;

import static com.google.common.base.Preconditions.*;

@Path("/{project_slug}/{repository_slug}/")
@Scanned
public class PullRequestLabels {
  @ComponentImport private final ActiveObjects ao;

  private static Logger log = Logger.getLogger(PullRequestLabels.class.getSimpleName());

  @ComponentImport private final RepositoryService repositoryService;

  @ComponentImport private final PullRequestService pullRequestService;

  @ComponentImport private final ProjectService projectService;

  @Inject
  public PullRequestLabels(
      ActiveObjects ao,
      RepositoryService repositoryService,
      PullRequestService pullRequestService,
      ProjectService projectService) {
    log.setLevel(INFO);

    this.ao = checkNotNull(ao);
    this.repositoryService = checkNotNull(repositoryService);
    this.pullRequestService = checkNotNull(pullRequestService);
    this.projectService = checkNotNull(projectService);
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/pull-requests/{pull_request_id}")
  public Response listByPullRequest(
      @PathParam("project_slug") String projectSlug,
      @PathParam("repository_slug") String repositorySlug,
      @PathParam("pull_request_id") Long pullRequestId) {
    Project project = this.projectService.getByKey(projectSlug);
    if (project == null) {
      return Response.status(404).build();
    }

    Repository repository = this.repositoryService.getBySlug(projectSlug, repositorySlug);
    if (repository == null) {
      return Response.status(404).build();
    }

    PullRequest pullRequest = this.pullRequestService.getById(repository.getId(), pullRequestId);
    if (pullRequest == null) {
      return Response.status(404).build();
    }

    final Label[] labels =
        this.ao.find(
            Label.class,
            Query.select()
                .where(
                    "PROJECT_ID = ? AND REPOSITORY_ID = ? AND PULL_REQUEST_ID = ?",
                    project.getId(),
                    repository.getId(),
                    pullRequest.getId()));

    return Response.ok(new PullRequestLabelsListResponse(this.getNames(labels))).build();
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/pull-requests/")
  public Response listByRepositoryHash(
      @PathParam("project_slug") String projectSlug,
      @PathParam("repository_slug") String repositorySlug) {
    Project project = this.projectService.getByKey(projectSlug);
    if (project == null) {
      return Response.status(404).build();
    }

    Repository repository = this.repositoryService.getBySlug(projectSlug, repositorySlug);
    if (repository == null) {
      return Response.status(404).build();
    }

    final Label[] labels =
        this.ao.find(
            Label.class,
            Query.select()
                .where(
                    "PROJECT_ID = ? AND REPOSITORY_ID = ?", project.getId(), repository.getId()));

    HashMap<Long, ArrayList<String>> map = new HashMap<Long, ArrayList<String>>();
    for (Label label : labels) {
      ArrayList<String> names = map.get(label.getPullRequestId());
      if (names == null) {
        names = new ArrayList<String>();
        map.put(label.getPullRequestId(), names);
      }

      names.add(label.getName());

      // log.log(SEVERE, new Object(names));
    }

    return Response.ok(new PullRequestLabelsMapResponse(map)).build();
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/")
  public Response listByRepository(
      @PathParam("project_slug") String projectSlug,
      @PathParam("repository_slug") String repositorySlug) {
    Project project = this.projectService.getByKey(projectSlug);
    if (project == null) {
      return Response.status(404).build();
    }

    Repository repository = this.repositoryService.getBySlug(projectSlug, repositorySlug);
    if (repository == null) {
      return Response.status(404).build();
    }

    final Label[] labels =
        this.ao.find(
            Label.class,
            Query.select()
                .where(
                    "PROJECT_ID = ? AND REPOSITORY_ID = ?", project.getId(), repository.getId()));

    return Response.ok(new PullRequestLabelsListResponse(this.getNames(labels))).build();
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Consumes("application/x-www-form-urlencoded")
  @Path("/pull-requests/{pull_request_id}")
  public Response add(
      @PathParam("project_slug") String projectSlug,
      @PathParam("repository_slug") String repositorySlug,
      @PathParam("pull_request_id") Long pullRequestId,
      @FormParam("name") String name) {
    Project project = this.projectService.getByKey(projectSlug);
    if (project == null) {
      return Response.status(404).build();
    }

    Repository repository = this.repositoryService.getBySlug(projectSlug, repositorySlug);
    if (repository == null) {
      return Response.status(404).build();
    }

    PullRequest pullRequest = this.pullRequestService.getById(repository.getId(), pullRequestId);
    if (pullRequest == null) {
      return Response.status(404).build();
    }

    final int found =
        this.ao.count(
            Label.class,
            Query.select()
                .where(
                    "PROJECT_ID = ? "
                        + "AND REPOSITORY_ID = ? "
                        + "AND PULL_REQUEST_ID = ? "
                        + "AND NAME LIKE ?",
                    project.getId(),
                    repository.getId(),
                    pullRequest.getId(),
                    name));

    if (found > 0) {
      return Response.ok(new PullRequestLabelsSaveResponse(true)).build();
    }

    final Label label =
        this.ao.create(
            Label.class,
            new DBParam("PROJECT_ID", project.getId()),
            new DBParam("REPOSITORY_ID", repository.getId()),
            new DBParam("PULL_REQUEST_ID", pullRequest.getId()),
            new DBParam("NAME", name));

    this.ao.flush();

    return Response.ok(new PullRequestLabelsSaveResponse(true)).build();
  }

  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  @Consumes("application/x-www-form-urlencoded")
  @Path("/pull-requests/{pull_request_id}")
  public Response delete(
      @PathParam("project_slug") String projectSlug,
      @PathParam("repository_slug") String repositorySlug,
      @PathParam("pull_request_id") Long pullRequestId,
      @FormParam("name") String name) {
    Project project = this.projectService.getByKey(projectSlug);

    Repository repository = this.repositoryService.getBySlug(projectSlug, repositorySlug);

    PullRequest pullRequest = this.pullRequestService.getById(repository.getId(), pullRequestId);

    final Label[] labels =
        this.ao.find(
            Label.class,
            Query.select()
                .where(
                    "PROJECT_ID = ? "
                        + "AND REPOSITORY_ID = ? "
                        + "AND PULL_REQUEST_ID = ? "
                        + "AND NAME = ?",
                    project.getId(),
                    repository.getId(),
                    pullRequest.getId(),
                    name));

    if (labels.length > 0) {
      this.ao.delete(labels);
      this.ao.flush();
    }

    return Response.ok(new PullRequestLabelsSaveResponse(true)).build();
  }

  private String[] getNames(Label[] labels) {
    Set<String> set = new HashSet<String>();
    for (int i = 0; i < labels.length; i++) {
      set.add(labels[i].getName());
    }

    String[] names = set.toArray(new String[set.size()]);

    return names;
  }
}
