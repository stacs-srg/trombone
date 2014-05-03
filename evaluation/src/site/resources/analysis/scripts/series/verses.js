define(['data', 'config/theme'], function (data, theme) {

    return function (one, other, column_index) {

        if (column_index === undefined) {
            column_index = 0;
        }

        return {
            get: function (match, index) {

                var x = one.columnAverage(match.name)[0];
                var y = other.columnAverage(match.name)[0];

                return [
                    {
                        id: match.name,
                        name: match.name,
                        data: [
                            [x, y]
                        ],
                        color: theme.colours[index]
                    }
                ]
            }
        }
    }
});