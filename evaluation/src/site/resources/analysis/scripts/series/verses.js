define(['jquery', 'data', 'config/theme', 'scope', 'util'], function ($, data, theme, scope, util) {

        return function (one, other, column_index, group_by) {

            if (column_index === undefined) {
                column_index = 0;
            }

            function getSeries(matches, group_by) {
                var series = []
                if (group_by !== undefined) {

                    var data = [];
                    var groups = matches.groupBy(group_by);

                    Object.keys(groups).forEach(function (group, index) {
                        var data = [];
                        groups[group].forEach(function (match, index) {
                            var x = one.columnAverage(match.name)[0];
                            var y = other.columnAverage(match.name)[0];
                            data.push({x: x, y: y, name: match.name})

                        }, this);

                        series.push(
                            {
                                id: group,
                                name: group,
                                data: data,
                                color: theme.colours[index]
                            }
                        );

                    }, this);


                } else {

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
                                color: theme.colours[index]
                            }
                        );
                    }, this)
                }

                return series;
            }

            return {
                get: function (matches) {

                    var churn_tick = $(util.read("templates/group_by.html"));
                    var aa = this;
                    churn_tick.click(function (e) {
                        var val = $("input[name=group]:checked").val();
                        var group_by = val == 'none' ? undefined : val;
                        var series = getSeries(matches, group_by);
                        
                        scope.observation.series = series;
                        scope.renderer();
                    });
                    
                    $('#extras').html(churn_tick);
                    
                    return getSeries(matches, group_by);
                }
            }
        }
    }
);