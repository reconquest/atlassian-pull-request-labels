//
// Plugin-specific view components.
//
// These components are 'main' classes which constructs all other
// dependencies and modify BB UI state if applicable.
//
// These components are aware about right place to inject other UI
// elements.
//
// They should conform to interface:
// a) constructor should accept exactly two parameters: 'context' and
//    'api'.
// b) 'mount()' method should either mount current state or return null if
//    current state is not applicable for current page.
//

var ViewPullRequestListWithFilter = function (context, api) {
    this._$ = $('#pull-requests-content');
    if (this._$.length == 0) {
        return new ViewNotApplicable();
    }

    this._avatarSize = new AvatarSize('medium');

    this._render = function(labels, mapping) {
        this._react = new React(this._$);

        // Bitbucket <= 5.0
        if (this._react.state() == null) {
            this._react = new React(this._$.find('.pull-requests-table'))
        }

        this._filter = new PullRequestFilter(
            this._react,
            new SelectLabelFilter(labels)
        );

        var that = this;
        this._list = new PullRequestList(this._react, {
            build: function() {
                return api.urls.search(
                    context.getProjectKey(),
                    context.getRepositorySlug(),
                    $.extend(
                        that._filter.get(),
                        {avatar_size: that._avatarSize.px()},
                        this.params // params are set by internal BB code
                    )
                )
            }
        });

        this._filter.change(
            function(event) {
                if (event.added) {
                    this._filter.set({label: event.added.name});
                    this._list.mount();
                }

                if (event.removed) {
                    this._list.unmount();
                }
            }.bind(this)
        );

        this._table = {
            filter: new PullRequestTableFilter(this._filter),
            content: new PullRequestTable(new LabelsCellProviderStatic(mapping))
        }

        this._table.filter.mount(this._$.find('.filter-bar'))
        this._table.content.mount(this._$)
    }

    this.mount = function() {
        $.when(
            api.getByRepository(
                context.getProjectKey(),
                context.getRepositorySlug()
            ),
            api.getByPullRequestList(
                context.getProjectKey(),
                context.getRepositorySlug()
            )
        )
        .done(
            function (getByRepositoryXHR, getByPullRequestListXHR) {
                this._render(
                    getByRepositoryXHR[0].labels,
                    getByPullRequestListXHR[0].labels
                );
            }.bind(this)
        )
        .fail(function (e) {
            if (e.status == 401) {
                InvalidLicenseNagbar.show();
            } else {
                throw e;
            }
        }.bind(this));

        return this;
    }

    return this;
}

var ViewPullRequestDetails = function (context, api) {
    this._$ = $('.plugin-section-primary');
    if (this._$.length == 0) {
        return new ViewNotApplicable();
    }

    this._render = function(labels, mapping) {
        this._labels = labels;

        this._panel = new LabelsPanel({
            allowNew: true,

            query: function (_) {
                return this._labels;
            }.bind(this),

            add: function(candidate) {
                var found = false;
                $.each(this._labels, function (_, label) {
                    if (label.name == candidate.name) {
                        found = true;
                    }
                })

                if (!found) {
                    candidate.color = WellKnownColors.Random();
                }

                return api.addLabel(
                    context.getProjectKey(),
                    context.getRepositorySlug(),
                    context.getPullRequestID(),
                    candidate
                ).done(
                    function(response) {
                        if (!found) {
                            candidate.id = response.id;
                            this._labels.push(candidate);
                        }
                    }.bind(this)
                );
            }.bind(this),

            remove: function(label) {
                return api.removeLabel(
                    context.getProjectKey(),
                    context.getRepositorySlug(),
                    context.getPullRequestID(),
                    label
                );
            }.bind(this),

            update: function (label) {
                return api.updateLabel(
                    context.getProjectKey(),
                    context.getRepositorySlug(),
                    label
                );
            }.bind(this)
        });

        $.each(
            mapping,
            function (_, label) {
                this._panel.label(label)
            }.bind(this)
        );

        this._$.append(this._panel);
    }

    this.mount = function () {
        $.when(
            api.getByRepository(
                context.getProjectKey(),
                context.getRepositorySlug()
            ),
            api.getByPullRequest(
                context.getProjectKey(),
                context.getRepositorySlug(),
                context.getPullRequestID()
            )
        )
        .done(
            function (getByRepositoryXHR, getByPullRequestXHR) {
                this._render(
                    getByRepositoryXHR[0].labels,
                    getByPullRequestXHR[0].labels
                );
            }.bind(this)
        )
        .fail(function (e) {
            if (e.status == 401) {
                InvalidLicenseNagbar.show();
            } else {
                throw e;
            }
        }.bind(this));

        return this;
    }

    return this;
};

var ViewDashboard = function (context, api) {
    this._$ = $('table.dashboard-pull-requests-table');
    if (this._$.length == 0) {
        return new ViewNotApplicable();
    }

    this._provider = new LabelsCellProviderDynamic(this._$, api);

    this.mount = function() {
        this._$.each(
            function(_, container) {
                new PullRequestTable(this._provider).mount($(container));
            }.bind(this)
        );
    }

    return this;
}
