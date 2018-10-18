(function($) {
    $(document).ready(function () {
        var $content = $('#content');
        var $pullRequestsTable = $('.pull-requests-table');
        var $pullRequestsSummaries = $pullRequestsTable.find('tbody td.summary');
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
                repoSlug + '/pull-requests';

            return $.get(url);
        }

        var loadRepoLabels = function () {
            // TODO: add API call to retrieve unique set of labels
            return loadPullRequestsLabels().then(function (response) {
                var result = {};
                $.each(response.labels, function (pullRequestID, labels) {
                    $.each(labels, function (index, label) {
                        result[label] = true;
                    });
                });

                return Object.keys(result);
            })
        }

        var addPullRequestsTableHeader = function () {
            var $header = $('<th/>', {
                // TODO: localization?
                text: "Labels"
            });

            $pullRequestsTable.find('thead th.summary').after($header);
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

        var getPullRequestsIDs = function () {
            var pullRequestsIDs = $pullRequestsSummaries.
                map(
                    function(index, element) {
                        return $(element).data('pull-request-id');
                    }
                ).
                get();

            return pullRequestsIDs;
        }

        var addPullRequestsLabels = function (pullRequestsLabels) {
            $.each(
                pullRequestsLabels,
                function (id, labels) {
                    var $cell = $('<td/>');

                    $.each(labels, function (index, label) {
                        var $label = io.reconquest.bitbucket.labels.Label(
                            {
                                text: label,
                            }
                        );

                        $cell.append($label);
                        $cell.append(" ");
                    })

                    $pullRequestsSummaries.
                        filter('tbody td[data-pull-request-id="' + id + '"]').
                        after($cell);
                }
            )
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

            var newLabel = $select.val();
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

            $select.parents('form').submit(function (event) {
                event.preventDefault();
            })

            $select.change(function () {
                addLabel($panel, $select);
            });

            $.each(
                repoLabels,
                function (index, label) {
                    $select.append(
                        io.reconquest.bitbucket.labels.ViewLabelsSelectOption({
                            text: label
                        })
                    )
                }
            );
        }

        var addPullRequestLabelsSidePanel = function (
            pullRequestLabels,
            repoLabels
        ) {
            var $panel = $(io.reconquest.bitbucket.labels.View());

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

        if ($pullRequestsTable.length > 0) {
            addPullRequestsTableHeader();

            var pullRequestsIDs = getPullRequestsIDs();
            $.when(
                loadPullRequestsLabels()
            ).done(function (pullRequestsResponse) {
                addPullRequestsLabels(pullRequestsResponse.labels);
            });
        }

        if (pullRequestID > 0) {
            $.when(
                loadPullRequestLabels(pullRequestID),
                loadRepoLabels(projectKey, repoSlug)
            ).done(function (pullRequestLabelsResponse, repoLabels) {
                addPullRequestLabelsSidePanel(
                    pullRequestLabelsResponse[0].labels,
                    repoLabels
                );
            });
        }
    });
}(AJS.$));
