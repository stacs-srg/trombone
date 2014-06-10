define(['jquery', 'util', 'jstat', 'json', 'csv', 'jszip', 'jszip_utils', 'mt'],
    function ($, util) {

        return function (name) {

            var home = util.resultsPath + name;
            var scenario_json = JSON.parse(util.read(home + '/scenario.json'));
            var repetitions_json = JSON.parse(util.read(home + '/repetitions/repetitions.json'));

            function repetitionZipFiles() {

                var zip_files = [];
                var zip_loads = []
                repetitions_json.forEach(function (zip_name) {

                    var differed = $.Deferred(function () {

                        var zip_path = home + '/repetitions/' + zip_name;
                        JSZipUtils.getBinaryContent(zip_path, function (error, data) {
                            if (error) {
                                differed.rejectWith(zip_path, [error]);
                            }
                            zip_files.push(new JSZip(data))
                            differed.resolve();
                        });
                    });

                    zip_loads.push(differed);
                }, this);

                var future_zips = $.Deferred();
                $.when.apply($, zip_loads).done(function () {
                    future_zips.resolve(zip_files);
                }).fail(function (error) {
                    future_zips.reject([error]);
                });

                return future_zips.promise();
            }

            function combineCrossHostData(file_name, dataz) {

                if (dataz.length == 1) {
                    return dataz[0];
                }

                throw Error('unimplemented');
            }

            function combineCrossRepetitionData(file_name, dataz) {

                var combined = [];
                if (file_name.match(/.+counter.csv/)) {

                    for (var i = 0; i < dataz[0].length; i++) {
                        var coll = jStat(dataz).col(i).toArray();

                        var row = [0, 0];
                        var values = [];

                        coll.forEach(function (s) {
                            if (s[0] != undefined) {
                                values.push(s[0][1]);
                            }
                        });

                        row[0] = i * 10;
                        var mean = jStat(values).mean();
                        var confidence_interval = jStat.tci(mean, 0.05, values);
                        if (isNaN(confidence_interval[0])) {
                            confidence_interval[0] = null
                        }
                        if (isNaN(confidence_interval[1])) {
                            confidence_interval[1] = null
                        }
                        console.log(mean, values, confidence_interval);
                        row[1] = mean;
                        row[2] = confidence_interval[0];
                        row[3] = confidence_interval[1];

                        combined.push(row);
                    }
                    return combined;
                }

                if (file_name.match(/.+rate.csv/)) {

                    for (var i = 0; i < dataz[0].length; i++) {
                        var coll = jStat(dataz).col(i).toArray();

                        var row = [0, 0];
                        var values = [];

                        coll.forEach(function (s) {
                            if (s[0] != undefined) {
                                values.push(s[0][2]);
                            }
                        });

                        row[0] = i * 10;
                        var mean = jStat(values).mean();
                        var confidence_interval = jStat.tci(mean, 0.05, values);
                        if (isNaN(confidence_interval[0])) {
                            confidence_interval[0] = null
                        }
                        if (isNaN(confidence_interval[1])) {
                            confidence_interval[1] = null
                        }
                        row[1] = mean;
                        row[2] = confidence_interval[0];
                        row[3] = confidence_interval[1];

                        combined.push(row);
                    }
                    return combined;
                }

                if (dataz.length == 1) {
                    return dataz[0];
                }

                throw Error('unimplemented');
            }

            function toData(zip, file_name) {

                var cross_host_files = zip.filter(function (path) {
                    return path.match(new RegExp('[0-9]\/' + file_name))
                });

                var dataz = [];

                cross_host_files.forEach(function (zipped_file) {

                    var content = zipped_file.asBinary();
                    var csv_as_array = $.csv.toArrays(content,
                        {
                            onParseValue: function (value, state) {

                                var casted_value = $.csv.hooks.castToScalar(value.replace(/,/g, ""));

                                if (isNaN(casted_value)) {
                                    return  null;
                                }

                                return  casted_value;
                            }
                        }
                    );

                    csv_as_array.splice(0, 1);
                    dataz.push(csv_as_array);
                })

                return combineCrossHostData(file_name, dataz);
            }

            return{

                description: scenario_json,

                csv: function (csv_file_name) {
                    return {
                        acrossRepetitions: function () {

                            var future_data = $.Deferred();
                            this.perRepetition().done(function (cross_repetitions) {
                                var combined = combineCrossRepetitionData(csv_file_name, cross_repetitions);
                                future_data.resolve(combined);
                            })
                            return future_data;
                        },
                        perRepetition: function () {

                            var future_data = $.Deferred();
                            repetitionZipFiles().done(function (data) {

                                var cross_repetitions = [];
                                data.forEach(function (zip) {
                                    cross_repetitions.push(toData(zip, csv_file_name));
                                });
                                future_data.resolve(cross_repetitions);
                            });

                            return future_data;
                        }
                    }
                }

            }
        }
    }
);