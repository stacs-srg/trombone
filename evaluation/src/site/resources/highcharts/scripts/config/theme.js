define(['jquery', 'series'], function ($, series) {

    return {
        colours: [
            "#3366cc", "#dc3912", "#ff9900", "#109618", "#990099", "#0099c6", "#dd4477",
            "#66aa00", "#b82e2e", "#316395", "#994499", "#22aa99", "#aaaa11", "#6633cc",
            "#e67300", "#8b0707", "#651067", "#329262", "#5574a6", "#3b3eac", "#b77322",
            "#16d620", "#b91383", "#f4359e", "#9c5935", "#a9c413", "#2a778d", "#668d1c",
            "#bea413", "#0c5922", "#743411"
        ],
        chart: {
            type: 'line',
            zoomType: 'xy',
            onePerMatch: false
        },
        title: {
            text: null
        },
        plotOptions: {
            line: {
                marker: {
                    enabled: false
                }
            },
            arearange: {
                showInLegend: false
            },
            series: {
                animation: false
            }
        },

        xAxis: {
            startOnTick: true,
            endOnTick: true,
            title: {
                text: "Time Through Experiment (Hours)"
            },
            min: 0,
            max: 4
        },
        yAxis: {
            min: 0
        },
        series: [],
        credits: {
            enabled: false
        },
        extend: function (child) {
            var clone = $.extend(true, {}, this);
            $.extend(true, clone, child);
            return clone;
        },
        populateSeries: function (matches) {
            this.series = []
            matches.forEach(function (match, index) {


                var newVar = this.series_provider.get(match, index);

                this.series = this.series.concat(newVar)

//                this.series.push(
//                    {
//                        id: match.name,
//                        name: match.name,
//                        data: series(this.file_name, [0, 1]).data(match.name),
//                        zIndex: 1,
//                        lineWidth: 1,
//                        color: this.colours[index]
//                    }
//                );
//
//                this.series.push(
//                    {
//                        name: match.name + " Confidence Interval",
//                        type: 'arearange',
//                        data: series(this.file_name, [0, 2, 3]).data(match.name),
//                        fillOpacity: 0.3,
//                        zIndex: 0,
//                        lineWidth: 0,
//                        linkedTo: match.name,
//                        color: this.colours[index]
//
//                    }
//                );
            }, this);
        }
    }
});
