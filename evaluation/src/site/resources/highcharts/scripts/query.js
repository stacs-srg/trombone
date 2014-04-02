define(['observations'], function (observations) {

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
        }

    }
});