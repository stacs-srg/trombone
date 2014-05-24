define(['data', 'config/theme'], function (data, theme) {

    return function (datas) {

        var values = function (match) {
            var result = new Array();
            datas.forEach(function (d) {
               console.log(d.csv_file)
                result.push([match.name + " "+d.csv_file, d.columnSum(match.name)[0]]);
            })
            
            console.log(result)
            return result;
        }

        return {
            get: function (matches) {

                var series = []
                matches.forEach(function (match, index) {
                    series.push(
                        {
                            id: match.name,
                            name: match.name,
                            data: values(match),
                            zIndex: 1,
                            lineWidth: 1,
                            color: theme.colours[index]
                        }
                    );
                }, this)
                return series;
            }
        }
    }
});