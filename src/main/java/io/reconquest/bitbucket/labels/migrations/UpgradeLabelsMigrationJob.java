package io.reconquest.bitbucket.labels.migrations;

import javax.inject.Inject;
import javax.inject.Named;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.hook.script.HookScriptService;
import com.atlassian.bitbucket.project.ProjectService;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reconquest.bitbucket.labels.Store;

@Named("UpgradeLabelsMigrationJob")
public class UpgradeLabelsMigrationJob {
  private static Logger log =
      LoggerFactory.getLogger(UpgradeLabelsMigrationJob.class.getSimpleName());

  private HookScriptService hookScriptService;
  private SecurityService securityService;
  private RepositoryHookService repoHookService;
  private ProjectService projectService;
  private RepositoryService repositoryService;
  private PluginSettings pluginSettings;
  private Store store;

  @Inject
  public UpgradeLabelsMigrationJob(
      @ComponentImport ActiveObjects activeObjects,
      @ComponentImport RepositoryService repositoryService,
      @ComponentImport HookScriptService hookScriptService,
      @ComponentImport RepositoryHookService repoHookService,
      @ComponentImport ProjectService projectService,
      @ComponentImport PluginSettingsFactory pluginSettingsFactory,
      @ComponentImport SecurityService securityService) {
    this.hookScriptService = hookScriptService;
    this.repoHookService = repoHookService;
    this.projectService = projectService;
    this.securityService = securityService;
    this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
    this.repositoryService = repositoryService;

    this.store = new Store(activeObjects);
  }
}
