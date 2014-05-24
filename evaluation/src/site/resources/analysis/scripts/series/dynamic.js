define(['data', 'config/theme'], function (data, theme) {

    return function (one, other, column_index) {

        if (column_index === undefined) {
            column_index = 0;
        }

        return {
            get: function (matches) {


                var series = []
                matches.forEach(function (match, index) {

                    var x = one.columnAverage(match.name)[0];
                    var y = other.columnAverage(match.name)[0];

                    series.push(
                        {
                            id: match.name,
                            name: match.name,
                            data: [
                                [x, y]
                            ],
                            color: theme.colours[index],
                            update: function (value) {
                                data[1] = x * value;
                            }
                        }
                    );
                }, this)
                return series;
            }
        }
    }
});