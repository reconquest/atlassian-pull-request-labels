var Compat = function () {
    var react = function () {
        try {
            return require('react')
        } catch(e) {
            return {version: null};
        }
    }

    this.react = {
        v15: react().version >= "15",
        v16: react().version >= "16"
    }

    this.helpers = {
        avatars: AJS.version > "7.6.3"
    }

    this.icons = {
        tag: AJS.version >= "7.5.3"
    }

    return this;
}
