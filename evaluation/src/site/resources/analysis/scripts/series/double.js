define(['data', 'config/theme'], function (data, theme) {

    return function (csv_file, converters) {
        return {
            get: function (matches) {

                var series = []
                matches.forEach(function (match, index) {
                    series.push(
                        {
                            id: match.name,
                            name: match.name,
                            data: data(csv_file, [0, 1], converters).get(match.name),
                            zIndex: 1,
                            lineWidth: 1,
                            color: theme.colours[index]
                        },
                        {
                            name: match.name + " Confidence Interval",
                            type: 'arearange',
                            data: data(csv_file, [0, 2, 3], converters).get(match.name),
                            fillOpacity: 0.3,
                            zIndex: 0,
                            lineWidth: 0,
                            linkedTo: match.name,
                            color: theme.colours[index]
                        }
                    );
                }, this)
                return series;
            }
        }
    }
});