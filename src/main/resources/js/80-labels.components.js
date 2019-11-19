//
// Plugin-specific components which work with BB internal React state.
//
// These components rely on internal React state and they can hijack this
// state to achieve certain tasks.
//

var PullRequestFilter = function (react, filter) {
    this._react = react;

    this.set = function (filter) {
        this._label = filter.label;
    }

    this.get = function() {
        var filter = this._react.state().filter;

        return {
            state: filter.state.value || "OPEN",
            is_reviewer: filter.reviewer_self.value,
            target_ref: filter.target_ref.value,
            author: filter.author.value,
            label: this._label,
        };
    }

    return $.extend(filter, this);
}

var PullRequestList = function(react, builder) {
    this._builder = Options(builder, {
        withParams: function(params) {
            this.params = params;
            return this;
        },

        build: function() {},
    });

    this._react = react;
    this._provider = this._react.state().prProvider;

    this._update = function() {
        // Reset internal BB React state to force reload PR list from the
        // remote server.
        this._provider.currentData = [];
        this._provider.reset();
        this._react.state().pullRequests = [];
        this._react.state().onMorePrsRequested();
    }

    this.mount = function() {
        this._getBuilder = this._provider._getBuilder;

        // _getBuilder is an internal BB provider which is object that
        // should construct URI which will be used to in GET request to the
        // server to obtain list of a pull requests that match given
        // filter.
        //
        // On the server side we mimic authentic response from native
        // pull request list endpoint to deceive native BB React code and
        // render response from our plugin.
        this._provider._getBuilder = function() {
            return this._builder;
        }.bind(this);

        this._update();
    },

    this.unmount = function() {
        this._provider._getBuilder = this._getBuilder;
        this._getBuilder = null;

        this._update();
    }

    return this;
}

//
// Plugin-specific UI components, which extend native BB UI.
//
// These components construct DOM elements and inject them into given DOM
// hierarchy.
//
// These components are not aware about right place to inject DOM elements
// and it's caller responsibility to call 'mount()' method with correct
// root element.
//

var PullRequestTable = function (labelsProvider) {
    this.LabelsHeader = function () {
        return $('<th/>', {
            "text": "Labels",
            "class": "rq-labels-table-header"
        });
    }

    this._header = new this.LabelsHeader();

    this._extractPullRequestID = function(row) {
        var $td = $(row).find('td.summary');

        var repositoryID = $td.data('repository-id')
            || $td.find('div a').data('repository-id');

        var pullRequestID = $td.data('pull-request-id')
            || $td.find('div a').data('pull-request-id');

        if (repositoryID && pullRequestID) {
            return repositoryID + "." + pullRequestID;
        }

        if (pullRequestID) {
            return pullRequestID;
        }

        return "";
    }

    this._render = function ($tbody) {
        $tbody.parent().
            find('thead th.reviewers').
            before(this._header);

        $tbody.parent().
            find('tbody tr').
            each(function(_, row) {
                var id = this._extractPullRequestID(row)
                if (!id) {
                    return;
                }

                labelsProvider.provide(id, function(cell) {
                    $(row).find('td.reviewers').before(cell);
                })
            }.bind(this));
    }

    this._observer = new Observer(
        'tbody',
        this._render.bind(this)
    );

    this.mount = function(table) {
        this._observer.observe(table);
        this._render(table);
    }

    return this;
}

var PullRequestTableFilter = function (filter) {
    this.mount = function (bar) {
        $(bar).
            find('li:last').
            after(
                $('<li/>').append(filter)
            );
    }

    return this;
}
