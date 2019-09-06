(function($) {
    // @requires $
    // @requires _
    $(document).ready(function () {
        var $content = $('#content');
        var $pullRequestsContent = $('#pull-requests-content');
        var $sideSection = $('.plugin-section-primary');

        var projectKey = $content.data('projectkey');
        var repoSlug = $content.data('reposlug');

        var pullRequestID = $content.data('pullrequestid');

        var baseURL = AJS.contextPath();

        if (baseURL == "/") {
            baseURL = ""
        }

        var apiBaseURL = baseURL + '/rest/io.reconquest.bitbucket.labels/1.0/';

        var loadPullRequestLabels = function (pullRequestID) {
            var url = apiBaseURL +
                projectKey + '/' +
                repoSlug + '/pull-requests/' +
                pullRequestID;

            return $.get(url);
        }

        var loadPullRequestsLabels = function () {
            var url = apiBaseURL +
                projectKey + '/' +
                repoSlug + '/pull-requests/';

            return $.get(url);
        }

        var loadRepoLabels = function () {
            var url = apiBaseURL +
                projectKey + '/' +
                repoSlug + '/';

            return $.get(url);
        }

        var addPullRequestsTableHeader = function () {
            var $header = $('<th/>', {
                // TODO: localization?
                "text": "Labels",
                "class": "labels"
            });

            if ($pullRequestsContent.find('th.labels').length == 0) {
                $pullRequestsContent.find('thead th.summary').after($header);
            }
        }

        var addPullRequestLabel = function (text) {
            var url = apiBaseURL +
                projectKey + '/' +
                repoSlug + '/pull-requests/' +
                pullRequestID;

            return $.ajax(
                url,
                {
                    data: {name: text},
                    method: "POST",
                    headers: {
                        "X-Atlassian-Token": "no-check"
                    }
                }
            );
        }

        var removePullRequestLabel = function (text) {
            var url = apiBaseURL +
                projectKey + '/' +
                repoSlug + '/pull-requests/' +
                pullRequestID;

            return $.ajax(
                url,
                {
                    data: {name: text},
                    method: "DELETE",
                    headers: {
                        "X-Atlassian-Token": "no-check"
                    }
                }
            )
        }

        var addPullRequestsLabels = function (pullRequestsLabels) {
            $pullRequestsContent.
                find('tbody td.summary').
                filter('tbody tr:not([data-labeled]) td[data-pull-request-id!=""]').
                each(function(i, td) {
                    var labels = [];

                    var id = $(td).data('pull-request-id');

                    if (id in pullRequestsLabels) {
                        labels = pullRequestsLabels[id];
                    }

                    var $cell = $('<td/>');

                    $.each(labels, function (index, label) {
                        var $label = io.reconquest.bitbucket.labels.Label(
                            {
                                text: label,
                            }
                        );

                        $cell.append($label);
                        $cell.append(" ");
                    });

                    $(td).after($cell);
                    $(td).parent().attr('data-labeled', 'true');
                });
        }

        var addLabelToPanel = function ($panel, label, closeable) {
            var $label = $(io.reconquest.bitbucket.labels.Label(
                {
                    text: label,
                    closeable: closeable
                }
            ));

            if (closeable) {
                $label.find('.aui-icon-close').click(function () {
                    $panel.find('.spinner').spin();
                    $.when(
                        removePullRequestLabel(label)
                    ).done(function () {
                        $panel.find('.spinner').spinStop();
                        $label.remove();
                    });
                });
            }

            $panel.find('.labels-list').append($label);
        }

        var addLabel = function ($panel, $select) {
            var $input = $select.find('input');

            var newLabel = _.unescape($select.val());
            var found = false;

            if (newLabel == "") {
                return;
            }

            $panel.find('.labels-list .aui-label').each(
                function (index, label) {
                    if (newLabel == $(label).data('label-text')) {
                        found = true;
                    }
                }
            );

            if (found) {
                AJS.flag({
                    type: 'error',
                    body: 'Label <b>' + newLabel + '</b> already set.',
                    close: 'auto'
                });

                return;
            }

            $input.attr('disabled', true);
            $panel.find('.spinner').spin();

            $.when(
                addPullRequestLabel(newLabel)
            ).done(function () {
                addLabelToPanel($panel, newLabel, true);

                $input.val('');
                $input.removeAttr('disabled');
                $panel.find('.spinner').spinStop();
            });
        }

        var initLabelSelect = function (
            $panel,
            repoLabels
        ) {
            var $select = $panel.find('#labels-new-select');

            $select.parents('form').submit(function (e) {
                e.preventDefault();
            })

            changing = false;
            // aui/select has duplicated event triggering
            $select.change(function (e) {
                if (changing) {
                    return;
                }

                changing = true;
                $.when(
                    addLabel($panel, $select)
                ).done(function() {
                    changing = false;
                })

                return false;
            });
        }

        var addPullRequestLabelsSidePanel = function (
            pullRequestLabels,
            repoLabels
        ) {
            var $panel = $(io.reconquest.bitbucket.labels.View({labels: repoLabels}));

            console.log($panel.html());

            $.each(pullRequestLabels, function (index, label) {
                addLabelToPanel($panel, label, false);
            });

            $panel.find('#labels-edit-button').click(function () {
                editPullRequestLabelsSidePanel($panel, pullRequestLabels);
            });

            initLabelSelect($panel, repoLabels);

            $sideSection.append($panel);
        }

        var editPullRequestLabelsSidePanel = function ($panel, labels) {
            var $labelsList = $sideSection.find('.labels-list');
            var $select = $sideSection.find('.labels-new');
            var $editButton = $sideSection.find('#labels-edit-button');

            $labelsList.empty();
            $editButton.hide();

            $.each(labels, function (index, label) {
                addLabelToPanel($panel, label, true)
            });

            $select.show();
            $select.find('input').focus();
        }

        var addTooltipForLabels = function() {
            $('.io_reconquest_bitbucket_labels_label').tooltip({
                title: function() {
                    return 'Searching by labels is not yet available. '+
                        'The feature is coming soon.';
                },
            });
        }

        var populatePullRequestsTable = function () {
            // refresh object because it can be modified by atlassian ui
            $pullRequestsContent = $($pullRequestsContent);

            $.when(
                loadPullRequestsLabels()
            ).done(function (pullRequestsResponse) {
                addPullRequestsTableHeader();
                addPullRequestsLabels(pullRequestsResponse.labels);
                addTooltipForLabels();
            });
        }

        if ($pullRequestsContent.length > 0) {
            var MutationObserver =
                window.MutationObserver ||
                window.WebKitMutationObserver;

            var observer = new MutationObserver(function(mutations, observer) {
                var timeout = null;

                $.each(mutations, function (index, mutation) {
                    if (mutation.target.tagName == "TBODY") {
                        if (timeout != null) {
                            clearTimeout(timeout);
                        }

                        timeout = setTimeout(function () {
                            populatePullRequestsTable();
                        }, 100)
                    }
                })
            });

            observer.observe($pullRequestsContent[0], {
                subtree: true,
                childList: true
            });

            populatePullRequestsTable();
        }

        if (pullRequestID > 0) {
            $.when(
                loadPullRequestLabels(pullRequestID),
                loadRepoLabels(projectKey, repoSlug)
            ).done(function (pullRequestLabelsResponse, repoLabels) {
                console.log('done')
                addPullRequestLabelsSidePanel(
                    pullRequestLabelsResponse[0].labels,
                    repoLabels[0].labels
                );
                addTooltipForLabels();
            });
        }
    });
}(AJS.$));
// vim: et
