(function($) {
    $(document).ready(function () {
        var $content = $('#content');
        var $pullRequestsTable = $('.pull-requests-table');
        var $pullRequestsSummaries = $pullRequestsTable.find('tbody td.summary');
        var $sideSection = $('.plugin-section-primary');

        var projectKey = $content.data('projectkey');
        var repoSlug = $content.data('reposlug');

        var pullRequestID = $content.data('pullrequestid');

        var addPullRequestsTableHeader = function () {
            var $header = $('<th/>', {
                // TODO: localization?
                text: "Labels"
            });

            $pullRequestsTable.find('thead th.summary').after($header);
        }

        var loadPullRequestsLabels = function (pullRequestsIDs) {
            console.log(projectKey, repoSlug, pullRequestsIDs);

            return {
                1: [
                    { name: "feature" }
                ],

                2: [
                    { name: "bugfix" },
                    { name: "approved" }
                ],
            };
        }

        var loadRepoLabels = function () {
            console.log(projectKey, repoSlug);

            return [
                { name: "feature" },
                { name: "bugfix" },
                { name: "approved" }
            ];
        }

        var addPullRequestLabel = function (text, onSuccess) {
            // TODO: send
            var label = {
                name: text,
            };

            onSuccess(label)
        }

        var removePullRequestLabel = function (text, onSuccess) {
            onSuccess()
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
                                text: label.name,
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
                    text: label.name,
                    closeable: closeable
                }
            ));

            if (closeable) {
                $label.find('.aui-icon-close').click(function () {
                    $panel.find('.spinner').spin();
                    removePullRequestLabel(label.name, function () {
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

            addPullRequestLabel(newLabel, function (label) {
                addLabelToPanel($panel, label, true);

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
                            text: label.name
                        })
                    )
                }
            );
        }

        var addPullRequestLabelsSidePanel = function (repoLabels) {
            var $panel = $(io.reconquest.bitbucket.labels.View());

            var labels = loadPullRequestsLabels([pullRequestID]);

            if (!(pullRequestID in labels)) {
                return
            }

            var pullRequestLabels = labels[pullRequestID];
            $.each(pullRequestLabels, function (index, label) {
                addLabelToPanel($panel, label, false);
            });

            $panel.find('#labels-edit-button').click(function () {
                editPullRequestLabelsSidePanel($panel, pullRequestLabels);
            });

            initLabelSelect($panel, pullRequestLabels, repoLabels);

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
        }

        if ($pullRequestsTable.length > 0) {
            addPullRequestsTableHeader();

            var pullRequestsIDs = getPullRequestsIDs();
            var pullRequestsLabels = loadPullRequestsLabels(pullRequestsIDs);

            addPullRequestsLabels(pullRequestsLabels);
        }

        if (pullRequestID > 0) {
            var repoLabels = loadRepoLabels(projectKey, repoSlug);

            addPullRequestLabelsSidePanel(repoLabels);
        }
    });
}(AJS.$));
