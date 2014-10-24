define(['jquery', 'observations', 'config/scenarios', 'scope', 'series', 'config/theme', 'util'], function ($, observations, scenarios, scope, series, theme, util) {

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
        experiment_duration: [],
        encode: function () {
            var encoded = this.metric + "_";
            encoded += encodeArray(this.churn) + "_";
            encoded += encodeArray(this.workload) + "_";
            encoded += encodeArray(this.maintenance) + "_";
            encoded += encodeArray(this.experiment_duration);
            return encoded;
        },
        decode: function (encoded) {

            if (encoded === undefined || encoded.trim() == '') {
                this.metric = 0;
                this.churn = [];
                this.workload = [];
                this.maintenance = [];
                this.experiment_duration = [];
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
                        case 4:
                            this.experiment_duration = decodeArray(value);
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
            $("#main_title").text(observations[this.metric].title);
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
            this.experiment_duration.forEach(function (selection) {
                $("#experiment_duration_" + selection).prop('checked', true);
            })
            this.experiment_duration.forEach(function (selection) {
                $("#training_duration_" + selection).prop('checked', true);
            })
            this.experiment_duration.forEach(function (selection) {
                $("#clustering_algorithm_" + selection).prop('checked', true);
            })
            this.experiment_duration.forEach(function (selection) {
                $("#feedback_enabled_" + selection).prop('checked', true);
            })

            var matches = this.matches();
            var observation = observations[this.metric]
            scope.matches = matches;
            scope.observation = theme.extend(observation)
            scope.renderer()
            
            var matches_nav = $('#matches_nav');
            var matches_content = $('#matches_content');
            matches_nav.empty();
            matches_content.empty();

            
            matches.forEach(function (match, index) {

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
            util.resizeElementHeight(document.getElementById("chart_list"));
        },
        matches: function () {

            var matches = new Array();
            var churn_keys = Object.keys(scenarios.by_churn());
            var workload_keys = Object.keys(scenarios.by_workload());
            var maintenance_keys = Object.keys(scenarios.by_maintenance());
            var experiment_duration_keys = Object.keys(scenarios.by_experiment_duration());
            this.churn.forEach(function (selection) {

                scenarios.by_churn()[churn_keys[selection]].forEach(function (element) {
                    matches.push(element);
                });
            }, this)

            matches = matches.filter(function (element) {
                var found = false;
                this.workload.forEach(function (selection) {
                    found = found || element.workload == workload_keys[selection];
                });
                return found;
            }, this)
            matches = matches.filter(function (element) {
                var found = false;
                this.maintenance.forEach(function (selection) {
                    found = found || element.maintenance == maintenance_keys[selection];
                });
                return found;
            }, this)
            matches = matches.filter(function (element) {
                var found = false;
                this.experiment_duration.forEach(function (selection) {
                    found = found || element.experiment_duration == experiment_duration_keys[selection];
                });
                return found;
            }, this)
            return matches;
        }
    }
})
;