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
