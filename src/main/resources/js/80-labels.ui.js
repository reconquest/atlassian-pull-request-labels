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
                $input.attr('data-aui-notification-error', '');
                return;
            } else {
                $input.removeAttr('data-aui-notification-error');
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
            this._callbacks.select(null);
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

var ButtonIconEdit = function() {
    return ButtonIcon('edit', {classes: 'rq-labels-button-edit'});
}

var SelectLabel = function(options) {
    return new Select(Options(options, {
        itemize: function(item) {
            if (item == null) {
                return null;
            }

            if (item.disabled) {
                return $('<div class="already-set"/>').append(
                    new IconTag(),
                    item.name + ' is already assigned.'
                );
            }

            var $label = new Label(item);

            var $item = $('<div/>').append(
                new IconTag(),
                $label,
                item.id ? '' : '(new)'
            );

            $item.on('mouseout mousewheel DOMMouseScroll', function () {
                $item.css('position', '');
                if ($item._timer) {
                    clearTimeout($item._timer);
                    $item._timer = null;
                }
            });

            $item.on('mouseover', function () {
                if ($item._timer) {
                    return;
                }

                $item._timer = setTimeout(function () {
                    var height = $item.parent().height();
                    var width = $item.parent().width();
                    var offset = $item.offset();

                    $item.parent().height(height).width(width);
                    $item
                        .css('position', 'fixed')
                        .css('z-index', '10')
                        .offset(offset);

                    $item._timer = null;
                }, 200);
            });

            return $item;
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
        update: function(_) {}
    });

    this._colorPicker = new LabelColorPicker();

    this._labels = {};

    this._query = function (term) {
        var labels = options.query(term);

        $.each(
            labels,
            function (i, label) {
                labels[i].disabled = label.id in this._labels;
            }.bind(this)
        );

        return labels;
    }

    this.create = function (candidate) {
        this._select.disable();
        this._spinner.show();

        $.when(
            options.add(candidate)
        ).done(
            function (response) {
                candidate.id = response.id;

                this.label(candidate);
                this._spinner.hide();
                this._select.empty()
                this._select.enable();
            }.bind(this)
        );

        this._$help.show();

        return candidate;
    }

    var editing = false;
    this.edit = function() {
        editing = true;
        this._$.addClass('rq-editable');
        if (!$.isEmptyObject(this._labels)) {
            this._$help.show();
        }
    }

    this.label = function (label) {
        var panel = this;

        this._labels[label.id] = label;

        var $label = new Label(label, {
            on: {
                click: function () {
                    if (!editing) {
                        return;
                    }

                    var select = function(color) {
                        panel._spinner.show();

                        label.color = color;

                        $.when(
                            options.update(label)
                        ).done(
                            function () {
                                panel._spinner.hide();
                                this.color(color);
                            }.bind(this)
                        );
                    }.bind(this);

                    var cancel = function (color) {
                        return function() { select(color); }
                    }(label.color);

                    panel._colorPicker.bind(
                        this,
                        {
                            init: function () {
                                return label.color;
                            },

                            select: select,
                            cancel: cancel
                        }
                    );
                },
                close: function() {
                    panel._colorPicker.unbind();
                    panel._spinner.show();

                    $.when(
                        options.remove(label)
                    ).done(function () {
                        delete panel._labels[label.id];
                        this.remove();
                        panel._spinner.hide();
                    }.bind(this));
                }
            }
        });

        this._$labels.append($label);
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

    this._header.append(
        new ButtonIconEdit().
        click(
            function() { this.edit() }.bind(this)
        )
    )

    this._$form = $('<form class="rq-labels-edit-form"/>').
        submit(function() { return false }).
        append(this._select);

    this._$help = $('<div class="rq-labels-side-panel-tip">').text(
        'Tip: You can change label color by clicking on it.'
    );

    this._$messages = $('<div class="rq-labels-messages"/>')

    this._$messages.exist = new MessageError()
    this._$messages.append(this._$messages.exist);

    this._$ = $('<div class="rq-labels-side-panel"/>').append(
        this._header,
        this._$labels,
        this._$form,
        this._$help,
        this._$messages
    );

    return $.extend(this._$, this);
}
