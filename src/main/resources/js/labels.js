(function($) {
    var $pullRequestsTable = $('.pull-requests-table');
    var $pullRequestsSummaries = $pullRequestsTable.find('tbody td.summary');

    var addPullRequestsTableHeader = function () {
        var $header = $('<th/>', {
            // TODO: localization?
            text: "Labels"
        });

        $pullRequestsTable.find('thead th.summary').after($header);
    }

    var loadPullRequestsLabels = function (pullRequests) {
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
                    var $div = $('<div/>', {
                        text: label.name
                    });

                    $div.addClass("pull-request-label");

                    $cell.append($div);
                })

                $pullRequestsSummaries.
                    filter('tbody td[data-pull-request-id="' + id + '"]').
                    after($cell);
            }
        )
    }

    if ($pullRequestsTable.length > 0) {
        addPullRequestsTableHeader();

        var pullRequestsIDs = getPullRequestsIDs();
        var pullRequestsLabels = loadPullRequestsLabels(pullRequestsIDs);

        addPullRequestsLabels(pullRequestsLabels);
    }
}(AJS.$));
