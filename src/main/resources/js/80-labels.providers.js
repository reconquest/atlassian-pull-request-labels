// Providers of Labels Cells
//
// Should implement following interface:
//
// provide(id, callback) generates a cell for specified PR id
//     and invokes callback while passing generated cell as an argument

var LabelsCellProvider = function (project, repository, api) {
    this._cells = {};

    this.provide = function (id) {
        if (!(id in this._cells)) {
            return $.when(
                api.getByPullRequest(project, repository, id)
            ).then(
                function (response) {
                    return this._cells[id] = new LabelsCell(response.labels);
                }.bind(this)
            )
        } else {
            return new Promise(function(resolve) {
                resolve(this._cells[id]);
            }.bind(this));
        }
    }

    return this;
}

var LabelsCellProviderStatic = function (labels) {
    this._cells = {};

    $.each(
        labels,
        function (pr, labels) {
            this._cells[pr] = new LabelsCell(labels);
        }.bind(this)
    );

    this.provide = function (id, callback) {
        this._cells[id] = this._cells[id] || new LabelsCell()

        callback(this._cells[id])
    }

    return this;
}

var LabelsCellProviderDynamic = function (selector, api) {
    // Main idea of Dynamic Provider is to collect all PR/Repository ID on
    // the page, retrieve data from backend for given repos, store it in
    // memory and use for all PRs
    //
    // If id in provide() is unknown (not in the list of collected PRs),
    // then we need to collect ids again and retrieve data from backend.
    this._repositories = {};
    this._pullRequests = {};

    this._cells = {};

    this._updating = false;
    this._callbacks = [];

    this._findIdentifiers = function () {
        $(selector).find('td div.title a').each(function(_, item) {
            var repositoryID = $(item).data('repository-id');
            var pullRequestID = $(item).data('pull-request-id');
            this._repositories[repositoryID] = 1;
            this._pullRequests[repositoryID + "." + pullRequestID] = 1;
        }.bind(this))
    }

    this._invokeCallbacks = function () {
        $.each(this._callbacks, function(_, callback) {
            callback()
        })

        this._callbacks.length = 0;
    }

    this._updateLabels = function () {
        this._updating = true;

        this._findIdentifiers();

        $.when(
            api.getByRepositoryIDs(Object.keys(this._repositories))
        )
        .done(
            function (response) {
                $.each(
                    response.labels,
                    function (repositoryID, pullRequests) {
                        $.each(pullRequests, function (pullRequestID, labels) {
                            var id = repositoryID + "." + pullRequestID;
                            if (!this._cells[id]) {
                                this._cells[id] = this._newLabelCell(labels);
                            }
                        }.bind(this))
                    }.bind(this)
                );

                this._invokeCallbacks();
                this._updating = false;
            }.bind(this)
        )
        .fail(function (e) {
            if (e.status == 401) {
                InvalidLicenseNagbar.show();
            } else {
                throw e;
            }
        }.bind(this))
    }

    this._newLabelCell = function (labels) {
        return new LabelsCell(labels);
    }

    this._callback = function(id, fn) {
        return fn(this._cells[id] = this._cells[id] || this._newLabelCell())
    }

    this.provide = function (id, callback) {
        if (this._updating || !this._pullRequests[id]) {
            this._callbacks.push(this._callback.bind(this, id, callback))

            if (!this._updating) {
                this._updateLabels();
                if (!this._pullRequests[id]) {
                    this._callback(id, callback);
                }
            }
        }
    }

    return this;
}

