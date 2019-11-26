var Context = function () {
    this.state = require('bitbucket/util/state')

    this.getProjectKey = function() {
        return (this.state.getProject() || {key: null}).key
    }

    this.getRepositorySlug = function() {
        return (this.state.getRepository() || {slug: null}).slug
    }

    this.getPullRequestID = function() {
        return (this.state.getPullRequest() || {id: null}).id
    }

    return this;
}
