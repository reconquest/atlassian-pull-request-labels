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

var InvalidLicenseNagbar = new Nagbar(
    'License for Pull Request Labels Add-on is missing or expired. ' +
    'Visit "Manage Apps" page in your Bitbucket instance for more info.'
)

$(document).ready(function () {
    var context = new Context();

    var api = new API(
        AJS.contextPath() != "/" ? AJS.contextPath() : ""
    );

    $.each(views, function (_, view) {
        new view(context, api).mount();
    });
});
