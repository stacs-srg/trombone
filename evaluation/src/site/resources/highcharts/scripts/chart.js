define(['jquery', 'config/theme', 'highcharts' , 'highcharts_more', 'highcharts_exporting'], function ($, theme) {

    var container = $('#chart');
    container.highcharts(theme);
    return{
        current: null,
         renderer: function(query, observation){
             
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