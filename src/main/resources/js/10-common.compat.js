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
