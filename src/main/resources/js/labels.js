(function ($) {
    //
    // Utility classes.
    //

    var Options = function (options, defaults) {
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

    var React_16 = function (element) {
        var element = $(element)[0];
        if (!element._reactRootContainer) {
            return null;
        }

        this.state = function() {
            return element.
                _reactRootContainer.
                    _internalRoot.
                        current.
                            child.
                                stateNode.
                                    state;
        }

        return this;
    }

    var React_15 = function (element) {
        var element = $(element)[0];
        var key = Object.keys(element).find(function (key) {
            return key.startsWith("__reactInternalInstance$");
        });

        if (!key) {
            this.state = function() {
                return null;
            }

            return this;
        } else {
            var pointer = element[key];
            while (pointer._currentElement._owner != null) {
                pointer = pointer._currentElement._owner;
            }

            this.state = function() {
                return pointer._instance.state;
            }

            return this;
        }
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

    var InvalidLicenseMessage =
        'License for Labels Add-on is missing or expired. ' +
        'Visit "Manage Apps" page in your Bitbucket instance for more info.';

    var ViewNotApplicable = function () {
        this.mount = function () {
            return null;
        }

        return this;
    }

    var Colors = {
        FromHex: function (hex) {
            var color = parseInt(hex.slice(1), 16);

            if (isNaN(color)) {
                return null;
            }

            return {
                r: 0xFF & (color >> 16),
                g: 0xFF & (color >> 8),
                b: 0xFF & color
            };
        },

        ToHex: function (rgb) {
            return "#" +
                ((1 << 24) + (rgb.r << 16) + (rgb.g << 8) + rgb.b)
                    .toString(16).slice(1);
        },

        Luminance: function (rgb) {
            return rgb.r*0.299 + rgb.g*0.587 + rgb.b*0.114;
        },

        IsBright: function(rgb) {
            return Colors.Luminance(rgb) > 186;
        }
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

    var Label = function(label, options) {
        var options = Options(options, {
            on: {}
        });

        options.on = Options(options.on, {
            click: function () {},
            close: null
        })

        var config = {
            text: label.name,
            isCloseable: $.isFunction(options.on.close),
            extraClasses: ['rq-label']
        }

        var $node;

        if (aui.labels) {
            $node = $(aui.labels.label(config));
        } else {
            $node = $('<span class="aui-label"/>').text(label.name);

            if ($.isFunction(options.on.close)) {
                this._$.
                    addClass("aui-label-closeable").
                    append($('<span class="aui-icon aui-icon-close"/>'))
            }
        }

        $node.
            find('.aui-icon-close').
            click(function(e) {
                e.stopPropagation();
                options.on.close.bind($(this).parent())();
            }).
            end();

        this.color = function (bg) {
            var fg = Colors.IsBright(Colors.FromHex(bg))
                ? '#000000'
                : '#FFFFFF';

            $node
                .css('background-color', bg)
                .css('color', fg)
                .css('border-color', bg);
        }

        this.uncolor = function () {
            $node
                .css('background-color','')
                .css('color','')
                .css('border-color','');
        }

        this._$ = $.extend($node, this);

        $node.click(options.on.click.bind(this._$));

        if (label.color) {
            this.color(label.color);
        }

        return this._$;
    }

    var LabelsCell = function(labels) {
        this._$ = $('<td class="rq-labels-table-row"/>');

        if (labels && labels.length > 0) {
            this._$.append(IconTag());

            $.each(labels, function (_, label) {
                this._$.append(new Label(label));
            }.bind(this));
        } else {
            this._$.append('&nbsp;'); // prevent table collapse in Firefox
        }

        return this._$;
    }

    var LabelsCellUnlicensed = function(labels) {
        this._$ = $('<td class="rq-labels-table-row"/>');

        this._$.append(new IconInvalidLicense());

        return this._$;
    }

    var Spinner = function(options) {
        var options = Options(options, {
            size: 'small'
        });

        return $('<aui-spinner class="rq-spinner"/>').
            attr("size", options.size).
            hide();
    }

    var Message = function (severity, content, options) {
        var options = Options(options, {
            fade: Options(options && options.fade || {}, {
                delay: 2000,
                duration: 2000
            })
        });

        this._$ = $(aui.message[severity]({
            content: content || ""
        })).hide();

        return $.extend(this._$, {
            fadeout: function () {
                this.
                    delay(options.fade.delay).
                    fadeOut(options.fade.duration);
                return this;
            }
        });
    }

    var MessageError = function (content) {
        return new Message('error', content);
    }

    var Select = function (options) {
        var __noop__ = function() {}

        var options = Options(options, {
            allowNew: false,
            placeholder: '...',
            query: function(_) { return [] },
            itemize: function(_) { return term },
            empty: '<empty>',
            css: Options(options.css, {
                container: '',
                dropdown: ''
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
                var data = [];

                var term = args.term.trim();

                var source = options.query();
                var hit = null;
                var matches = $.grep(source, function (item) {
                    if (item.name == term) {
                        hit = item;
                    }

                    return item.name.startsWith(term)
                })

                // push hit first
                if (hit) {
                    data.push(hit);
                }

                $.each(matches, function(_, item) {
                    if (hit && hit.id == item.id) {
                        return;
                    }

                    data.push(item);
                });

                if (options.allowNew) {
                    if (!hit && term != "") {
                        data.push({
                            id: '',
                            name: term,
                            newly: true
                        });
                    }
                }

                return args.callback({
                    results: data
                });
            },
            formatResult: options.itemize,
            formatSelection: options.itemize,
            formatNoMatches: function() {
                return options.empty;
            },
            dropdownCssClass: options.css.dropdown,
            containerCssClass: options.css.container
        }

        if ($.isFunction(options.on.create) && options.on.create != __noop__) {
            config.formatSelection = function(item) {
                item.name = item.name.trim();
                return options.itemize(options.on.create(item))
            }

            config.createSearchChoice = function (term, data) {
                var term = term.trim();

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
            },

            close: function() {
                this.children().select2('close');
            },

            itemize: function(item) {
                return options.itemize(item);
            }
        });
    }

    var Popup = function (id, body, options) {
        var options = Options(options, {
            alignment: "left top"
        });

        this._$template = $('<aui-inline-dialog>')
            .attr('id', id)
            .attr('alignment', options.alignment);

        this._$anchor = null

        return $.extend(this._$template, {
            close: function () {
                this._$.removeAttr('open');
            }.bind(this),

            open: function (anchor) {
                if (this._$) {
                    this._$.detach();
                }

                if (this._$anchor != null) {
                    this._$anchor.removeAttr('aria-controls');
                }

                this._$ = this._$template
                    .clone()
                    .append(body)
                    .appendTo('body');

                this._$anchor = $(anchor);

                this._$anchor.attr('aria-controls', id);

                this._$.attr('open', true);
            }.bind(this)
        });
    }

    //
    // Plugin-specific UI elements.
    //
    // These components typically extend UI elements from the library and set
    // plugin specific properties.
    //

    var LabelColorPicker = function (options) {
        var options = Options(options, {});

        var colors = [
            '#0033CC', '#428BCA', '#44AD8E', '#A8D695', '#5CB85C', '#69D100', '#004E00',
            '#34495E', '#7F8C8D', '#A295D6', '#5843AD', '#8E44AD', '#FFECDB', '#AD4363',
            '#D10069', '#CC0033', '#FF0000', '#D9534F', '#D1D100', '#F0AD4E', '#AD8D43'
        ];

        var $form = $('<form class="aui"/>').submit(function (e) {
            e.preventDefault();
        });

        var $input = $('<input class="text custom-color" type="text"/>')
            .attr('placeholder', '#RRGGBB');

        $input.on(
            'input',
            function () {
                var hex = $input.val();

                if (Colors.FromHex(hex) == null) {
                    // TODO
                    return;
                }

                this._callbacks.select(hex);
            }.bind(this)
        );

        var $colors = $('<div class="suggested-colors"/>');

        $.each(colors, function (_, color) {
            $colors.append(
                $('<a>')
                    .data('color', color)
                    .css('background-color', color)
                    .click(function () {
                        $input.val($(this).data('color')).trigger('input');
                    })
            );
        });

        var $reset = $('<a href="javascript:void(0)" class="rq-reset"/>')
            .text("Remove color");

        var $cancel = $('<a href="javascript:void(0)" class="rq-cancel"/>')
            .text("Cancel");

        $cancel.click(
            function () {
                this._callbacks.cancel();
                this.unbind();
            }.bind(this)
        );

        $reset.click(
            function () {
                this._callbacks.reset();
                this.unbind();
            }.bind(this)
        );

        $form.append($colors);
        $form.append($input);
        $form.append($reset);
        $form.append($cancel);

        var popup = Popup('rq-label-color-picker', $form);

        this.bind = function (anchor, callbacks) {
            this._callbacks = Options(callbacks, {
                init: function () {},
                select: function () {},
                cancel: function () {},
                reset: function () {}
            });

            var color = this._callbacks.init();
            if (color) {
                $input.val(color);
            } else {
                $input.val('')
            }

            popup.open(anchor);
        }

        this.unbind = function () {
            popup.close();
        }

        return this;
    }

    var AvatarSize_Native = function (size) {
        this.px = function() {
            return bitbucket.internal.widget.avatar.avatar.avatarSizeInPx({
                size: size || 'medium'
            });
        }

        return this;
    }

    var AvatarSize_64 = function() {
        this.px = function () {
            return 64;
        }

        return this;
    }

    // Bitbucket >= 6.0
    var IconTag_Native = function() {
        return Icon('tag', {classes: 'rq-labels-icon-tag'});
    }

    // Bitbucket < 6.0
    var IconTag_DevTools = function() {
        return Icon('devtools-tag', {classes: 'rq-labels-icon-tag'});
    }

    var IconInvalidLicense = function() {
        return Icon(
            'warning',
            {classes: 'rq-labels-icon-invalid-license'}
        ).attr('title', InvalidLicenseMessage);
    }

    var ButtonIconEdit = function() {
        return ButtonIcon('edit', {classes: 'rq-labels-button-edit'});
    }

    var SelectLabel = function(options) {
        return new Select(Options(options, {
            itemize: function(item) {
                if (item == null) {
                    return null;
                }

                return $('<span/>').append(
                    new IconTag(),
                    new Label(item),
                    item.newly ? '(new)' : ''
                )
            },
            css: {
                dropdown: 'rq-labels-select-dropdown',
                container: 'rq-labels-select'
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
            allowNew: false,
            query: function(_) { return [] },
            add: function(_) {},
            remove: function(_) {},
            licensed: true
        });

        this._colorPicker = new LabelColorPicker();

        this._labels = {};

        this._query = function (term) {
            return options.query(term)
        }

        this.create = function (candidate) {
            var exists = false;
            $.each(
                this._labels,
                function (_, label) {
                    if (label.name == candidate.name) {
                        exists = true;
                    }
                }.bind(this)
            );

            if (exists) {
                this._select.close();
                this._select.empty();

                var $label = this._select.itemize({
                    name: candidate.name
                });

                var $content = $('<span/>').
                    append($label).
                    append("already set.");

                this._$messages.exist.
                    html($content).
                    show().
                    fadeout();

                return;
            }

            this._select.disable();
            this._spinner.show();

            $.when(
                options.add(candidate)
            ).done(function () {
                this.label(candidate);
                this._spinner.hide();
                this._select.empty()
                this._select.enable();
            }.bind(this));

            return candidate;
        }

        var editing = false;
        this.edit = function() {
            editing = true;
            this._$.addClass('rq-editable');
        },

        this.label = function (label) {
            var panel = this;

            this._labels[label.id ? label.id : label.name] = label;

            this._$labels.append(
                new Label(label, {
                    on: {
                        click: function () {
                            if (!editing) {
                                return;
                            }

                            panel._colorPicker.bind(
                                this,
                                {
                                    init: function () {
                                        return label.color;
                                    },

                                    select: function(bg) {
                                        label.color = bg;
                                        this.color(bg);
                                        // TODO: send API request
                                    }.bind(this),

                                    cancel: function() {
                                        this.color(label.color);
                                    }.bind(this),

                                    reset: function() {
                                        label.color = null;
                                        this.uncolor();
                                        // TODO: send API request
                                    }.bind(this),
                                }
                            );
                        },
                        close: function() {
                            panel._colorPicker.unbind();
                            panel._spinner.show();

                            $.when(
                                options.remove(label)
                            ).done(function () {
                                this.remove();
                                delete panel._labels[label.id];
                                panel._spinner.hide();
                            }.bind(this));
                        }
                    }
                })
            );
        }

        this._spinner = new Spinner();
        this._$labels = $('<div class="rq-labels-list"/>');
        this._select  = new SelectLabel({
            allowNew: options.allowNew,
            empty: 'Enter New Label',
            placeholder: 'Add Label',
            query: this._query.bind(this),
            on: {
                create: this.create.bind(this)
            }
        });

        this._header = $('<h3/>').append(
            this._spinner,
            'Labels'
        )

        this._warning = null;
        if (options.licensed) {
            this._header.append(
                new ButtonIconEdit().
                click(
                    function() { this.edit() }.bind(this)
                )
            )
        } else {
            this._header.append(new IconInvalidLicense());

            this._warning = $('<i/>').text(InvalidLicenseMessage);
        }

        this._$form = $('<form class="rq-labels-edit-form"/>').
            submit(function() { return false }).
            append(this._select);

        this._$messages = $('<div class="rq-labels-messages"/>')

        this._$messages.exist = new MessageError()
        this._$messages.append(this._$messages.exist);

        this._$ = $('<div class="rq-labels-side-panel"/>').append(
            this._header,
            this._$labels,
            this._$form,
            this._$messages
        );

        if (this._warning) {
            this._$.append(this._warning);
        }

        return $.extend(this._$, this);
    }

    // Providers of Labels Cells
    //
    // Should implement following interface:
    //
    // provide(id, callback) generates a cell for specified PR id
    //     and invokes callback while passing generated cell as an argument

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

    var LabelsCellProviderStaticUnlicensed = function (labels) {
        this._cells = {};

        this.provide = function (id, callback) {
            this._cells[id] = this._cells[id] || new LabelsCellUnlicensed()

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
        this._licensed = true;
        this._callbacks = [];

        this._findIdentifiers = function () {
            $(selector).find('td div.title a').each(function(_, item) {
                this._pullRequests[$(item).data('pull-request-id')] = 1;
                this._repositories[$(item).data('repository-id')] = 1;
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
                        function (pr, labels) {
                            if (!this._cells[pr]) {
                                this._cells[pr] = this._newLabelCell(labels);
                            }
                        }.bind(this)
                    );

                    this._invokeCallbacks();
                    this._updating = false;
                }.bind(this)
            )
            .fail(function (e) {
                if (e.status == 401) {
                    this._licensed = false;

                    this._invokeCallbacks();
                } else {
                    throw e;
                }
            }.bind(this))
        }

        this._newLabelCell = function (labels) {
            if (this._licensed) {
                return new LabelsCell(labels);
            } else {
                return new LabelsCellUnlicensed(labels);
            }
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

            var id = $td.data('pull-request-id')
            if (id) {
                return id;
            }

            id = $td.find('div a').data('pull-request-id')
            if (id) {
                return id;
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

        this._renderUnlicensed = function () {
            this._table = new PullRequestTable(
                new LabelsCellProviderStaticUnlicensed()
            )

            this._table.mount(this._$)
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
                    this._renderUnlicensed();
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

        this._initPanel = function (licensed) {
            this._panel = new LabelsPanel({
                allowNew: true,
                licensed: licensed,

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
                        this._labels.push(candidate);
                    }

                    return api.addLabel(
                        context.getProjectKey(),
                        context.getRepositorySlug(),
                        context.getPullRequestID(),
                        candidate
                    );
                }.bind(this),

                remove: function(label) {
                    return api.removeLabel(
                        context.getProjectKey(),
                        context.getRepositorySlug(),
                        context.getPullRequestID(),
                        label
                    );
                }.bind(this)
            });
        }

        this._render = function(labels, mapping) {
            this._initPanel(true);

            this._labels = labels;

            $.each(
                mapping,
                function (_, label) {
                    this._panel.label(label)
                }.bind(this)
            );

            this._$.append(this._panel);
        }

        this._renderUnlicensed = function() {
            this._initPanel(false);
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
            .done(function (getByRepositoryXHR, getByPullRequestXHR) {
                this._render(
                    getByRepositoryXHR[0].labels,
                    getByPullRequestXHR[0].labels
                );
            }.bind(this))
            .fail(function (e) {
                if (e.status == 401) {
                    this._renderUnlicensed();
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
            this._$.each(function(_, container) {
                var table = new PullRequestTable(this._provider);
                table.mount($(container));
            }.bind(this));
        }

        return this;
    }

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
    }

    var Context = function () {
        this.state = require('bitbucket/util/state')

        this.getProjectKey = function() {
            return this.state.getProject().key
        }

        this.getRepositorySlug = function() {
            return this.state.getRepository().slug
        }

        this.getPullRequestID = function() {
            return this.state.getPullRequest().id
        }

        return this;
    }

    var Compat = function () {
        this.react = {
            v15: require('react').version >= "15",
            v16: require('react').version >= "16"
        }

        this.helpers = {
            avatars: AJS.version > "7.6.3"
        }

        this.icons = {
            tag: AJS.version >= "7.5.3"
        }

        return this;
    }

    //
    // Compatibility layer switches.
    //

    var React = React_16;
    var AvatarSize = AvatarSize_Native;
    var IconTag = IconTag_Native;

    $(document).ready(function () {
        var compat = new Compat();

        if (!compat.react.v16) {
            React = React_15;
        }

        if (!compat.helpers.avatars) {
            AvatarSize = AvatarSize_64;
        }

        if (!compat.icons.tag) {
            IconTag = IconTag_DevTools;
        }
    });

    //
    // Entry point.
    // Construct all known views and try to apply each one.
    // Views components are aware when they should be mounted.
    //

    var views = [
        ViewPullRequestDetails,
        ViewPullRequestListWithFilter,
        ViewDashboard
    ];

    $(document).ready(function () {
        var context = new Context();

        var api = new API(
            AJS.contextPath() != "/"? AJS.contextPath() : ""
        );

        $.each(views, function (_, view) {
            new view(context, api).mount();
        });
    });
}(AJS.$));
