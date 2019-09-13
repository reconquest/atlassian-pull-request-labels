(function($, baseURL) {
    //
    // Utility classes.
    //

    var Options = function(options, defaults) {
        return $.extend(defaults, options);
    }

    var Query = function (data) {
        var components = [];

        $.each(data, function(key, value) {
            if (value !== null) {
                components.push(key + "=" + encodeURIComponent(value));
            }
        });

        return components.join("&");
    }

    var React = function (element) {
        this.state = function() {
            return $(element)[0].
                _reactRootContainer.
                _internalRoot.
                    current.
                    child.
                        stateNode.
                        state;
        }

        return this;
    }

    var Observer = function (selector, fn) {
        var MutationObserver =
            window.MutationObserver ||
            window.WebKitMutationObserver;

        this._observer = new MutationObserver(
            function(mutations, observer) {
                var timeout = null;

                $.each(mutations, function (index, mutation) {
                    var $target = $(mutation.target);

                    if ($target.filter(selector).length > 0) {
                        if (timeout != null) {
                            clearTimeout(timeout);
                        }

                        timeout = setTimeout(fn($target), 10)
                    }
                });
            }
        );

        this.observe = function (target) {
            this._observer.observe(
                $(target)[0],
                {subtree: true, childList: true}
            );
        }

        return this;
    }

    var ViewNotApplicable = function () {
        this.mount = function () {
            return null;
        }

        return this;
    }

    //
    // UI elements library.
    //
    // Components that can be reused in different project defined there.
    // These components can be insterted into DOM hierarchy directly since they
    // inherit jQuery object.
    //

    var Icon = function(icon, options) {
        var options = new Options(options, {
            size: 'small',
            classes: []
        });

        return $(aui.icons.icon({
            useIconFont: true,
            size: options.size,
            icon: icon,
            extraClasses: options.classes,
        }));
    }

    var ButtonIcon = function(icon, options) {
        var options = Options(options, {
            classes: []
        });

        return $(aui.buttons.button({
            text: '',
            type: 'subtle',
            iconType: 'aui',
            iconClass: 'aui-icon-small aui-iconfont-' + icon,
            extraClasses: options.classes
        }));
    }

    var Label = function(text, close) {
        var config = {
            text: text,
            isCloseable: $.isFunction(close),
            extraClasses: ['rq-label']
        }

        return $(aui.labels.label(config)).
            find('.aui-icon-close').
            click(function() { close.bind($(this).parent())(); }).
            end();
    }

    var Spinner = function(options) {
        var options = Options(options, {
            size: 'small'
        });

        return $('<aui-spinner class="rq-spinner"/>').
            attr("size", options.size).
            hide();
    }

    var Select = function (options) {
        var __noop__ = function() {}

        var options = Options(options, {
            placeholder: '...',
            query: function(term) { return [] },
            itemize: function(term) { return term },
            empty: '<empty>',
            css: Options(options.css, {
                dropdown: '',
            }),
            on: Options(options.on, {
                create: __noop__,
                clear: __noop__
            }),
        });

        var config = {
            placeholder: options.placeholder,
            allowClear: true,
            query: function (args) {
                data = $.grep(options.query(args.term), function (item) {
                    return item.name.startsWith(args.term)
                });

                return args.callback({
                    results: data
                })
            },
            formatResult: options.itemize,
            formatSelection: options.itemize,
            formatNoMatches: function() {
                return options.empty;
            },
            dropdownCssClass: options.css.dropdown
        }

        if ($.isFunction(options.on.create) && options.on.create != __noop__) {
            config.formatSelection = function(item) {
                return options.itemize(options.on.create(item))
            }

            config.createSearchChoice = function (term, data) {
                if (data.length > 0) {
                    return null;
                }

                return {
                    id: '',
                    name: term,
                    newly: true
                };
            }
        }

        this._$ = $('<span><input/>').
            children().
                auiSelect2(config).
                    // We need to overload click event to be able to clear
                    // underlying Select object, because 'allowClear'
                    // configuration option for Select2 does not work here for
                    // whatever reason.
                    on('select2-opening', function(e) {
                        var value = $(this).select2('data');

                        if (value) {
                            var event = $.Event('change');
                            event.removed = value;

                            $(this).select2('val', '');
                            $(this).trigger(event);
                            options.on.clear();
                            e.preventDefault();
                        }
                    }).
            end();

        return $.extend(this._$, {
            disable: function() {
                this.children().select2('readonly', true);
            },

            enable: function() {
                this.children().select2('readonly', false);
            },

            empty: function() {
                this.children().select2('val', '');
            },

            change: function(fn) {
                this.children().on('change', fn);
            }
        });
    }

    //
    // Plugin-specific UI elements.
    //
    // These components typically extend UI elements from the library and set
    // plugin specific properties.
    //

    var IconTag = function() {
        return Icon('tag', {classes: 'rq-labels-icon-tag'});
    }

    var ButtonIconEdit = function() {
        return ButtonIcon('edit', {classes: 'rq-labels-button-edit'});
    }

    var SelectLabel = function(options) {
        return new Select(Options(options, {
            itemize: function(item) {
                return $('<span/>').append(
                    new IconTag(),
                    new Label(item.name),
                    item.newly ? '(new)' : ''
                )
            },
            css: {
                dropdown: 'rq-labels-select'
            }
        }));
    }

    var SelectLabelFilter = function (labels) {
        return new SelectLabel({
            empty: 'No labels found',
            placeholder: 'Label',
            query: function () {
                return labels;
            }
        });
    }

    var LabelsPanel = function (options) {
        var options = Options(options, {
            query: function(term) { return [] },
            add: function(label) {},
            remove: function(label) {}
        });

        this._context = context;
        this._api = api;
        this._labels = {};

        this._query = function (term) {
            return options.query(term).filter(function (candidate) {
                var accept = true;
                $.each(this._labels, function (_, label) {
                    if (label.name == candidate.name) {
                        accept = false;
                    }
                }.bind(this))

                return accept;
            }.bind(this));
        }

        this.create = function (label) {
            this._select.disable();
            this._spinner.show();

            $.when(
                options.add(label)
            ).done(function () {
                this.label(label);
                this._spinner.hide();
                this._select.empty()
                this._select.enable();
            }.bind(this));

            return label;
        }

        this.edit = function() {
            this._$.addClass('rq-editable');
        },

        this.label = function (label) {
            var panel = this;

            this._labels[label.id] = label;

            this._$labels.append(
                new Label(label.name, function() {
                    panel._spinner.show();

                    $.when(
                        options.remove(label)
                    ).done(function () {
                        this.remove();
                        delete panel._labels[label.id];
                        panel._spinner.hide();
                    }.bind(this));
                })
            );
        }

        this._$ = $('<div class="rq-labels-side-panel"/>').append(
                            /**/ $('<h3/>').append(
            this._spinner = /**/     new Spinner(),
                            /**/     'Labels',
                            /**/     new ButtonIconEdit().
                            /**/         click(
                            /**/             function() { this.edit() }.
                            /**/                bind(this)
                            /**/         )
                            /**/ ),
            this._$labels = /**/ $('<div class="rq-labels-list"/>'),
                            /**/ $('<form class="rq-labels-edit-form"/>').
                            /**/     submit(function() { return false }).
                            /**/     append(
            this._select  = /**/         new SelectLabel({
                            /**/            empty: 'Enter New Label',
                            /**/            placeholder: 'Add Label',
                            /**/            query: this._query.bind(this),
                            /**/            on: {
                            /**/                create: this.create.bind(this)
                            /**/            }
                            /**/        })
                            /**/     )
        );

        return $.extend(this._$, this);
    }

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
            // pull request list endpoint to deceive native BB Reach code and
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

    var PullRequestTable = function (labels) {
        this.LabelsHeader = function () {
            return $('<th/>', {
                "text": "Labels",
                "class": "rq-labels-table-header"
            });
        }

        this.LabelsCell = function(labels) {
            this._$ = $('<td class="rq-labels-table-row"/>');

            if (labels && labels.length > 0) {
                this._$.append(IconTag());

                $.each(labels, function (_, label) {
                    this._$.append(new Label(label.name));
                }.bind(this));
            }

            return this._$;
        }

        this._header = new this.LabelsHeader();

        this._cells = [];

        $.each(
            labels,
            function (pr, labels) {
                this._cells[pr] = new this.LabelsCell(labels);
            }.bind(this)
        );

        this._render = function ($tbody) {
            $tbody.parent().
                find('thead th.summary').
                    after(this._header).end().
                find('tbody td.summary').
                    filter('td[data-pull-request-id!=""]').
                    each(function(_, td) {
                        var id = $(td).data('pull-request-id');

                        $(td).after(
                            this._cells[id] = this._cells[id] ||
                                new this.LabelsCell()
                        );
                    }.bind(this));
        }

        this._observer = new Observer('tbody', this._render.bind(this));

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

        // As requested by authentic BB code.
        this._avatarSize = bitbucket.internal.widget.avatar.avatar.avatarSizeInPx({
            size: 'medium'
        });

        this._render = function(labels, mapping) {
            this._react = new React(this._$);

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
                            {avatar_size: this._avatarSize},
                            this.params // params are set by internal BB code
                        )
                    )
                }
            });

            this._filter.change(function(event) {
                if (event.added) {
                    this._filter.set({label: event.added.name});
                    this._list.mount();
                }

                if (event.removed) {
                    this._list.unmount();
                }
            }.bind(this));

            this._table = {
                filter: new PullRequestTableFilter(this._filter),
                content: PullRequestTable(mapping)
            }

            this._table.filter.mount(this._$.find('.filter-bar'))
            this._table.content.mount(this._$)
        }

        this.mount = function() {
            $.when(
                api.getByRepo(
                    context.getProjectKey(),
                    context.getRepositorySlug()
                ),
                api.getByPullRequestList(
                    context.getProjectKey(),
                    context.getRepositorySlug()
                )
            ).done(function (getByRepoXHR, getByPullRequestListXHR) {
                this._render(
                    getByRepoXHR[0].labels,
                    getByPullRequestListXHR[0].labels
                );
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

        this._panel = new LabelsPanel({
            query: function (_) {
                return this._labels;
            }.bind(this),

            add: api.label.bind(
                api,
                context.getProjectKey(),
                context.getRepositorySlug(),
                context.getPullRequestID()
            ),

            remove: api.unlabel.bind(
                api,
                context.getProjectKey(),
                context.getRepositorySlug(),
                context.getPullRequestID()
            )
        });

        this._render = function(labels, mapping) {
            this._labels = labels;

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
                api.getByRepo(
                    context.getProjectKey(),
                    context.getRepositorySlug()
                ),
                api.getByPullRequest(
                    context.getProjectKey(),
                    context.getRepositorySlug(),
                    context.getPullRequestID()
                )
            ).done(function (getByRepoXHR, getByPullRequestXHR) {
                this._render(
                    getByRepoXHR[0].labels,
                    getByPullRequestXHR[0].labels
                );
            }.bind(this));

            return this;
        }

        return this;
    }

    //
    // Global state objects.
    // These objects are read-only and provide current context only.
    //

    var api = Object.create({
        urls: {
            root: function() {
                return baseURL + '/rest/io.reconquest.bitbucket.labels/1.0/';
            },

            byRepo: function(project, repo) {
                return this.root() + project + '/' + repo + '/';
            },

            byPullRequestList: function(project, repo) {
                return this.byRepo(project, repo) + '/pull-requests/';
            },

            byPullRequest: function(project, repo, pr) {
                return this.byPullRequestList(project, repo) + pr;
            },

            search: function(project, repo, filter) {
                return this.byPullRequestList(project, repo) + ':search' +
                    '?' + Query(filter);
            },
        },

        getByRepo: function(project, repo) {
            return $.get(this.urls.byRepo(project, repo));
        },

        getByPullRequest: function(project, repo, pr) {
            return $.get(this.urls.byPullRequest(project, repo, pr));
        },

        getByPullRequestList: function(project, repo) {
            return $.get(this.urls.byPullRequestList(project, repo));
        },

        label: function(project, repo, pr, label) {
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
        },

        unlabel: function(project, repo, pr, label) {
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
        },
    });

    var context = Object.create({
        state: require('bitbucket/util/state'),

        getProjectKey: function() {
            return this.state.getProject().key
        },

        getRepositorySlug: function() {
            return this.state.getRepository().slug
        },

        getPullRequestID: function() {
            return this.state.getPullRequest().id
        }
    });

    var views = [
        ViewPullRequestDetails,
        ViewPullRequestListWithFilter
    ];

    //
    // Entry point.
    // Construct all known views and try to apply each one.
    // Views components are aware when they should be mounted.
    //

    $(document).ready(function () {
        $.each(views, function (_, view) {
            new view(context, api).mount();
        });
    });
}(AJS.$, AJS.contextPath() != "/" ? AJS.contextPath() : ""));
