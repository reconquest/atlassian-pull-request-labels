//
// Global state objects.
// These objects are read-only and provide current context only.
//

var API = function (baseURL) {
    this.urls = Object.create({
        root: function() {
            return baseURL + '/rest/io.reconquest.bitbucket.labels/1.0/';
        },

        list: function () {
            return this.root() + 'list';
        },

        byRepository: function(project, repo) {
            return this.root() + project + '/' + repo + '/';
        },

        byPullRequestList: function(project, repo) {
            return this.byRepository(project, repo) + 'pull-requests/';
        },

        byPullRequest: function(project, repo, pr) {
            return this.byPullRequestList(project, repo) + pr;
        },

        search: function(project, repo, filter) {
            return this.byPullRequestList(project, repo) + ':search' +
                '?' + Query(filter);
        },

        update: function (project, repo, labelID) {
            return this.byRepository(project, repo) + 'labels/' + labelID;
        }
    });

    this.getByRepositoryIDs = function(repos) {
        return $.ajax(
            this.urls.list(),
            {
                data: {repository_id: repos},
                method: "POST",
                headers: {
                    "X-Atlassian-Token": "no-check"
                }
            }
        );
    }

    this.getByRepository = function(project, repo) {
        return $.get(this.urls.byRepository(project, repo));
    }

    this.getByPullRequest = function(project, repo, pr) {
        return $.get(this.urls.byPullRequest(project, repo, pr));
    }

    this.getByPullRequestList = function(project, repo) {
        return $.get(this.urls.byPullRequestList(project, repo));
    }

    this.addLabel = function(project, repo, pr, label) {
        return $.ajax(
            this.urls.byPullRequest(project, repo, pr),
            {
                data: {name: label.name},
                method: "POST",
                headers: {
                    "X-Atlassian-Token": "no-check"
                }
            }
        );
    }

    this.removeLabel = function(project, repo, pr, label) {
        return $.ajax(
            this.urls.byPullRequest(project, repo, pr),
            {
                data: {name: label.name},
                method: "DELETE",
                headers: {
                    "X-Atlassian-Token": "no-check"
                }
            }
        );
    }

    this.updateLabel = function(project, repo, label) {
        return $.ajax(
            this.urls.update(project, repo, label.id),
            {
                data: {name: label.name, color: label.color},
                method: "PUT",
                headers: {
                    "X-Atlassian-Token": "no-check"
                }
            }
        );
    }
}
