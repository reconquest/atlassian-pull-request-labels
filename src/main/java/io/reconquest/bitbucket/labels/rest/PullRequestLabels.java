package io.reconquest.bitbucket.labels.rest;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.bitbucket.auth.AuthenticationContext;
import com.atlassian.bitbucket.avatar.AvatarRequest;
import com.atlassian.bitbucket.avatar.AvatarService;
import com.atlassian.bitbucket.project.Project;
import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestParticipantRequest;
import com.atlassian.bitbucket.pull.PullRequestRole;
import com.atlassian.bitbucket.pull.PullRequestSearchRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.pull.PullRequestState;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.rest.pull.RestPullRequest;
import com.atlassian.bitbucket.rest.pull.RestPullRequestParticipant;
import com.atlassian.bitbucket.rest.util.RestPage;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageImpl;
import com.atlassian.bitbucket.util.PageRequest;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.upm.api.license.PluginLicenseManager;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.reconquest.bitbucket.labels.Store;
import io.reconquest.bitbucket.labels.ao.LabelItem;
import io.reconquest.bitbucket.labels.rest.response.PullRequestLabelResponse;
import io.reconquest.bitbucket.labels.rest.response.PullRequestLabelsListResponse;
import io.reconquest.bitbucket.labels.rest.response.PullRequestLabelsMapResponse;
import io.reconquest.bitbucket.labels.rest.response.PullRequestLabelsSaveResponse;

@Path("/")
@Scanned
public class PullRequestLabels {
  @ComponentImport private final PluginLicenseManager pluginLicenseManager;

  // private static Logger log = new
  // LoggerFactory.getLogger(PullRequestLabels.class.getSimpleName());

  @ComponentImport private final RepositoryService repositoryService;

  @ComponentImport private final PullRequestService pullRequestService;

  @ComponentImport private final ProjectService projectService;

  @ComponentImport private final AvatarService avatarService;

  @ComponentImport private final AuthenticationContext authContext;

  private final Store store;

  @Inject
  public PullRequestLabels(
      @ComponentImport ActiveObjects ao,
      PluginLicenseManager pluginLicenseManager,
      RepositoryService repositoryService,
      PullRequestService pullRequestService,
      ProjectService projectService,
      AvatarService avatarService,
      AuthenticationContext authContext) {

    Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.DEBUG);

    this.pluginLicenseManager = pluginLicenseManager;
    this.repositoryService = checkNotNull(repositoryService);
    this.pullRequestService = checkNotNull(pullRequestService);
    this.projectService = checkNotNull(projectService);
    this.avatarService = checkNotNull(avatarService);
    this.authContext = checkNotNull(authContext);

    this.store = new Store(checkNotNull(ao));
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/{project_slug}/{repository_slug}/pull-requests/{pull_request_id}")
  public Response listByPullRequest(
      @PathParam("project_slug") String projectSlug,
      @PathParam("repository_slug") String repositorySlug,
      @PathParam("pull_request_id") Long pullRequestId) {
    if (!this.isLicenseValid()) {
      return Response.status(401).build();
    }

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

    return Response.ok(new PullRequestLabelsListResponse(this.getLabelsResponse(
            store.find(project.getId(), repository.getId(), pullRequest.getId()))))
        .build();
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/{project_slug}/{repository_slug}/pull-requests/")
  public Response listByRepositoryHash(
      @PathParam("project_slug") String projectSlug,
      @PathParam("repository_slug") String repositorySlug) {
    if (!this.isLicenseValid()) {
      return Response.status(401).build();
    }

    Project project = this.projectService.getByKey(projectSlug);
    if (project == null) {
      return Response.status(404).build();
    }

    Repository repository = this.repositoryService.getBySlug(projectSlug, repositorySlug);
    if (repository == null) {
      return Response.status(404).build();
    }

    final LabelItem[] labels = store.find(project.getId(), repository.getId());

    HashMap<Long, ArrayList<PullRequestLabelResponse>> map =
        new HashMap<Long, ArrayList<PullRequestLabelResponse>>();

    // for (Label label : labels) {
    //  ArrayList<PullRequestLabelResponse> pullRequestLabels = map.get(label.getPullRequestId());
    //  if (pullRequestLabels == null) {
    //    pullRequestLabels = new ArrayList<PullRequestLabelResponse>();
    //    map.put(label.getPullRequestId(), pullRequestLabels);
    //  }

    //  pullRequestLabels.add(
    //      new PullRequestLabelResponse(label.getID(), label.getName(), label.getColor()));
    // }

    return Response.ok(new PullRequestLabelsMapResponse(map)).build();
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/{project_slug}/{repository_slug}/pull-requests/:search")
  public Response searchPullRequestsByLabel(
      @PathParam("avatar_size") Integer avatarSize,
      @PathParam("project_slug") String projectSlug,
      @PathParam("repository_slug") String repositorySlug,
      @QueryParam("state") String state,
      @QueryParam("label") String labelName,
      @QueryParam("target_ref") String targetRef,
      @QueryParam("author") String author,
      @QueryParam("is_reviewer") Boolean isReviewer,
      @QueryParam("start") Integer start,
      @QueryParam("limit") Integer limit) {
    if (!this.isLicenseValid()) {
      return Response.status(401).build();
    }

    Project project = this.projectService.getByKey(projectSlug);
    if (project == null) {
      return Response.status(404).build();
    }

    Repository repository = this.repositoryService.getBySlug(projectSlug, repositorySlug);
    if (repository == null) {
      return Response.status(404).build();
    }

    if (labelName == null) {
      return Response.status(400).build();
    }

    if (avatarSize == null) {
      avatarSize = 64;
    }

    // TODO: support search by multiple labels
    final LabelItem[] items = store.find(project.getId(), repository.getId(), labelName);

    HashMap<Long, HashSet<String>> map = new HashMap<Long, HashSet<String>>();
    for (LabelItem item : items) {
      HashSet<String> names = map.get(item.getPullRequestId());
      if (names == null) {
        names = new HashSet<String>();
        map.put(item.getPullRequestId(), names);
      }

      names.add(item.getLabel().getName());
    }

    PullRequestSearchRequest.Builder builder = new PullRequestSearchRequest.Builder();

    builder.toRepositoryId(repository.getId());

    if (state != null) {
      switch (state.toUpperCase()) {
        case "OPEN":
          builder.state(PullRequestState.OPEN);
          break;
        case "DECLINED":
          builder.state(PullRequestState.DECLINED);
          break;
        case "MERGED":
          builder.state(PullRequestState.MERGED);
          break;
        case "ALL":
          break;
        default:
          return Response.status(400).build();
      }
    }

    if (targetRef != null) {
      builder.toRefId(targetRef);
    }

    ArrayList<PullRequestParticipantRequest> participants =
        new ArrayList<PullRequestParticipantRequest>();

    if (isReviewer != null && isReviewer) {
      participants.add(
          (new PullRequestParticipantRequest.Builder(this.authContext.getCurrentUser().getName()))
              .role(PullRequestRole.REVIEWER)
              .build());
    }

    if (author != null) {
      participants.add(
          (new PullRequestParticipantRequest.Builder(author)).role(PullRequestRole.AUTHOR).build());
    }

    if (start == null) {
      start = 0;
    }

    if (limit == null) {
      limit = 25;
    }

    if (participants.size() > 0) {
      builder.participants(participants);
    }

    ArrayList<RestPullRequest> filteredPullRequests = new ArrayList<RestPullRequest>();

    Integer searchOffset = start;
    Integer searchStart = 0;
    Boolean isLastPage = false;

    while (filteredPullRequests.size() < limit) {
      Boolean hasMore = false;
      PageRequest pageRequest = new PageRequestImpl(searchStart, limit);

      Page<PullRequest> page = this.pullRequestService.search(builder.build(), pageRequest);

      for (PullRequest pullRequest : page.getValues()) {
        if (filteredPullRequests.size() >= limit) {
          hasMore = true;
          break;
        }

        HashSet<String> pullRequestLabels = map.get(pullRequest.getId());
        if (pullRequestLabels == null) {
          continue;
        }

        if (pullRequestLabels.contains(labelName)) {
          if (searchOffset > 0) {
            searchOffset--;
            continue;
          }

          RestPullRequest restPullRequest = new RestPullRequest(pullRequest);

          RestPullRequestParticipant pullRequestAuthor =
              (RestPullRequestParticipant) restPullRequest.get(RestPullRequest.AUTHOR);

          pullRequestAuthor.getUser().setAvatarUrl(this.avatarService.getUrlForPerson(
              pullRequest.getAuthor().getUser(), new AvatarRequest(false, avatarSize)));

          for (RestPullRequestParticipant pullRequestParticipant : restPullRequest.getReviewers()) {
            pullRequestParticipant.getUser().setAvatarUrl(this.avatarService.getUrlForPerson(
                pullRequest.getAuthor().getUser(), new AvatarRequest(false, avatarSize)));
          }

          filteredPullRequests.add(restPullRequest);
          filteredPullRequests.add(restPullRequest);
        }
      }

      if (page.getIsLastPage()) {
        isLastPage = !hasMore;
        break;
      }

      searchStart += limit;
    }

    Page<RestPullRequest> filteredPage = new PageImpl<RestPullRequest>(
        new PageRequestImpl(start, limit), filteredPullRequests, isLastPage);

    return Response.ok(new RestPage<RestPullRequest>(filteredPage)).build();
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/{project_slug}/{repository_slug}/")
  public Response listByRepository(
      @PathParam("project_slug") String projectSlug,
      @PathParam("repository_slug") String repositorySlug) {
    if (!this.isLicenseValid()) {
      return Response.status(401).build();
    }

    Project project = this.projectService.getByKey(projectSlug);
    if (project == null) {
      return Response.status(404).build();
    }

    Repository repository = this.repositoryService.getBySlug(projectSlug, repositorySlug);
    if (repository == null) {
      return Response.status(404).build();
    }

    final LabelItem[] items = store.find(project.getId(), repository.getId());

    return Response.ok(new PullRequestLabelsListResponse(this.getLabelsResponse(items))).build();
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/list")
  public Response list(@FormParam("repository_id") List<Integer> repositories) {
    if (!this.isLicenseValid()) {
      return Response.status(401).build();
    }

    for (int i = 0; i < repositories.toArray().length; i++) {
      Integer id = repositories.get(i);

      // It also ensures that the user can access to repository
      Repository repository = this.repositoryService.getById(id);
      if (repository == null) {
        return Response.status(404).build();
      }
    }

    final LabelItem[] items = store.find(repositories.toArray(new Integer[0]));

    HashMap<Long, ArrayList<PullRequestLabelResponse>> map =
        new HashMap<Long, ArrayList<PullRequestLabelResponse>>();

    for (LabelItem item : items) {
      ArrayList<PullRequestLabelResponse> pullRequestLabels = map.get(item.getPullRequestId());
      if (pullRequestLabels == null) {
        pullRequestLabels = new ArrayList<PullRequestLabelResponse>();
        map.put(item.getPullRequestId(), pullRequestLabels);
      }

      pullRequestLabels.add(new PullRequestLabelResponse(
          item.getID(), item.getLabel().getName(), item.getLabel().getColor()));
    }

    return Response.ok(new PullRequestLabelsMapResponse(map)).build();
  }

  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Consumes("application/x-www-form-urlencoded")
  @Path("/{project_slug}/{repository_slug}/pull-requests/{pull_request_id}")
  public Response add(
      @PathParam("project_slug") String projectSlug,
      @PathParam("repository_slug") String repositorySlug,
      @PathParam("pull_request_id") Long pullRequestId,
      @FormParam("name") String name,
      @FormParam("color") String color) {
    if (!this.isLicenseValid()) {
      return Response.status(401).build();
    }

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

    // here we ignore color because it doesn't really matter in terms of
    // duplicates
    //
    // we also need to ignore them in order to save back compatibility
    final int found =
        store.countName(project.getId(), repository.getId(), pullRequest.getId(), name);

    if (found > 0) {
      return Response.ok(new PullRequestLabelsSaveResponse(true)).build();
    }

    if (color == null) {
      color = "#0000ff";
    }

    store.create(project.getId(), repository.getId(), pullRequest.getId(), name, color);
    store.flush();

    return Response.ok(new PullRequestLabelsSaveResponse(true)).build();
  }

  @DELETE
  @Produces({MediaType.APPLICATION_JSON})
  @Consumes("application/x-www-form-urlencoded")
  @Path("/{project_slug}/{repository_slug}/pull-requests/{pull_request_id}")
  public Response delete(
      @PathParam("project_slug") String projectSlug,
      @PathParam("repository_slug") String repositorySlug,
      @PathParam("pull_request_id") Long pullRequestId,
      @FormParam("name") String name) {
    if (!this.isLicenseValid()) {
      return Response.status(401).build();
    }

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

    final LabelItem[] items =
        store.find(project.getId(), repository.getId(), pullRequest.getId(), name);

    if (items.length > 0) {
      store.delete(items);
      store.flush();
    }

    return Response.ok(new PullRequestLabelsSaveResponse(true)).build();
  }

  private PullRequestLabelResponse[] getLabelsResponse(LabelItem[] labels) {
    HashMap<String, PullRequestLabelResponse> set = new HashMap<String, PullRequestLabelResponse>();

    for (int i = 0; i < labels.length; i++) {
      set.put(labels[i].getLabel().getName(), new PullRequestLabelResponse(
          labels[i].getID(), labels[i].getLabel().getName(), labels[i].getLabel().getColor()));
    }

    PullRequestLabelResponse[] response =
        set.values().toArray(new PullRequestLabelResponse[set.size()]);

    return response;
  }

  public boolean isLicenseDefined() {
    return true;
    // Option<PluginLicense> licenseOption = pluginLicenseManager.getLicense();
    // return licenseOption.isDefined();
  }

  public boolean isLicenseValid() {
    return true;
    // Option<PluginLicense> licenseOption = pluginLicenseManager.getLicense();
    // if (!licenseOption.isDefined()) {
    //  return false;
    // }

    // PluginLicense pluginLicense = licenseOption.get();
    // return pluginLicense.isValid();
  }
}
