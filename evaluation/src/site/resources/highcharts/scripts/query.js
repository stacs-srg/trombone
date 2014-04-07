define(['jquery', 'observations', 'config/scenarios', 'chart', 'series', 'config/theme'], function ($, observations, scenarios, chart, series, theme) {

    var encodeArray = function (array) {
        var encoded = "";
        var last_index = array.length - 1;
        array.forEach(function (value, index) {
            encoded += value;
            encoded += index != last_index ? "-" : "";
        });
        return encoded;
    }

    var decodeArray = function (encoded_array) {
        var decoded_array = new Array();
        encoded_array.split("-").forEach(function (element) {
            if (element.trim() != '') {
                decoded_array.push(parseInt(element))
            }
        });
        return decoded_array;
    }

    return{
        metric: 0,
        churn: [],
        workload: [],
        maintenance: [],
        encode: function () {
            var encoded = this.metric + "_";
            encoded += encodeArray(this.churn) + "_";
            encoded += encodeArray(this.workload) + "_";
            encoded += encodeArray(this.maintenance);
            return encoded;
        },
        decode: function (encoded) {

            if (encoded === undefined || encoded.trim() == '') {
                this.metric = 0;
                this.churn = [];
                this.workload = [];
                this.maintenance = [];
            } else {

                encoded.split("_").forEach(function (value, index) {

                    switch (index) {
                        case 0:
                            this.metric = parseInt(value);
                            break;
                        case 1:
                            this.churn = decodeArray(value);
                            break;
                        case 2:
                            this.workload = decodeArray(value);
                            break;
                        case 3:
                            this.maintenance = decodeArray(value);
                            break;
                        default:
                            console.log("unknown encoded query format", index, value)
                            break;
                    }
                }, this);
            }
            return this;
        },
        init: function () {
            this.decode(location.hash.slice(1));
            this.update();
        },
        update: function () {

            window.location.hash = this.encode();
            $("#main_title").text(observations.observations[this.metric].title);
            $("#chart_list .active").removeClass("active");
            $("#metric_" + this.metric).addClass("active");

            this.churn.forEach(function (selection) {
                $("#churn_" + selection).prop('checked', true);
            })
            this.workload.forEach(function (selection) {
                $("#workload_" + selection).prop('checked', true);
            })
            this.maintenance.forEach(function (selection) {
                $("#maintenance_" + selection).prop('checked', true);
            })

            chart.showLoading("Loading...");

            var observation = observations.observations[this.metric];
            observation = theme.extend(observation)

            if (!observation.chart.onePerMatch) {

                if (observation.series_provider !== undefined) {
                    observation.populateSeries(this.matches())
                }

            } else {
                alert("not implemented yet");
            }
            chart = new Highcharts.Chart(observation);
            chart.hideLoading();

            var matches_nav = $('#matches_nav');
            var matches_content = $('#matches_content');
            matches_nav.empty();
            matches_content.empty();

            this.matches().forEach(function (match, index) {

                matches_nav.append('<li><a href="#' +
                    match.name +
                    '" data-toggle="tab"' +
                    ' id="match_link_' +
                    match.name +
                    '" class="small" >' +
                    '<span class="glyphicon glyphicon-stop" style="color: ' +
                    theme.colours[index] +
                    '"/> ' +
                    match.name +
                    "</a></li>");

                var div = $('<div class="tab-pane" id="' + match.name + '"></div>');
                var table = $('<table class="table small"></table>');
                Object.keys(match).forEach(function (key) {
                    table.append('<tr>' +
                        '<td style="width: 15%"><strong>' +
                        key +
                        '</strong></td>' +
                        '<td>' +
                        match[key] +
                        '</td>' +
                        '</tr>')

                })
                div.append(table);
                matches_content.append(div);
            })

            $('#matches_nav a:first').tab('show');
//            $('#filter').collapse('hide');
            $('#matches').collapse('show');

        },
        matches: function () {

            var matches = new Array();
            this.churn.forEach(function (selection) {

                scenarios.by_churn[Object.keys(scenarios.by_churn)[selection]].forEach(function (element) {
                    matches.push(element);
                });
            }, this)

            matches = matches.filter(function (element) {
                var found = false;
                this.workload.forEach(function (selection) {
                    found = found || element.workload == Object.keys(scenarios.by_workload)[selection];
                });
                return found;
            }, this)
            matches = matches.filter(function (element) {
                var found = false;
                this.maintenance.forEach(function (selection) {
                    found = found || element.maintenance == Object.keys(scenarios.by_maintenance)[selection];
                });
                return found;
            }, this)
            return matches;
        }
    }
});