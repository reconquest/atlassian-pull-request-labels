var Context = function() {
  this.state = require("bitbucket/util/state");

  this.getProjectID = function() {
    return (this.state.getProject() || { id: null }).id;
  };

  this.getRepositoryID = function() {
    return (this.state.getRepository() || { id: null }).id;
  };

  this.getPullRequestID = function() {
    try {
      return (this.state.getPullRequest() || { id: null }).id;
    } catch (e) {
      var matches = window.location.pathname.match(
        /\/pull-requests\/(\d+)\/(overview|diff|commits)/
      );
      if (matches.length > 0) {
        return +matches[1];
      } else {
        return null;
      }
    }
  };

  return this;
};
