define(['jquery', 'config/theme', 'highcharts'], function ($, theme, query, observations) {

    var container = $('#chart');
    container.highcharts(theme);
    return{

        current: null,
        renderer: function () {

            var observation = observations[query.metric]
            observation = theme.extend(observation)

            if (!observation.chart.onePerMatch) {

                if (observation.series_provider !== undefined) {
                    observation.populateSeries(query.matches())
                }

            } else {
                alert("not implemented yet");
            }
            container.highcharts(observation)
            this.current = container.highcharts();
        }
    };
})