define([], function () {

    return function () {
        return {
            data: {},
            cache: function (key, value) {
                this.data[key] = value;
                return key;
            },
            get: function (key) {
                return this.data[key];
            },
            isCached: function (key) {
                return this.data[key] !== undefined;
            }
        }
    }
})