var Context = function () {
    this.state = require('bitbucket/util/state')

    this.getProjectID = function() {
        return (this.state.getProject() || {id: null}).id
    }

    this.getRepositoryID = function() {
        return (this.state.getRepository() || {id: null}).id
    }

    this.getPullRequestID = function() {
        return (this.state.getPullRequest() || {id: null}).id
    }

    return this;
}
