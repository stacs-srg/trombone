define(
    ['jquery', 'util', 'cache', 'jstat', 'jszip'],
    function ($, util, cache, jstat) {

        var DEFAULT_SKIP_HEADER = true;
        var DEFAULT_CONVERTERS = {
            0: util.convert.secondToHour
        }
        var data_cache = cache();

        var getRepetitions = function (scenario_name) {
            return results.folder(scenario_name).file(/\.zip$/);
        }

        var combineCrossHostMeasurements = function (measurements) {

            if (measurements.length == 1) {
                return measurements[0];
            }

            throw "UNIMPLEMENTED";
            //TODO check if there are headers and the headers are the same 
            //TODO identify type and combine to a 2d array
        }

        var toData = function (files) {

            var data = []
            files.forEach(function (file) {
                
                var content = file.asBinary();
                var csv_as_array = $.csv.toArrays(content, { onParseValue: function (value, state) {

                    var casted_value = $.csv.hooks.castToScalar(value.replace(/,/g, ""));
                    if (isNaN(casted_value)) {
                        return  null;
                    }
                    return  casted_value;
                } });

                csv_as_array.splice(0, 1);
                data.push(csv_as_array);
            });
            return data;
        }

        var getMeanPerRepetition = function (repetitions, file_name) {

            var files = [];
            repetitions.forEach(function (rep) {

                var zip = new JSZip(rep.asBinary());
                var cross_host_files = zip.filter(function (path) {
                    return path.match(new RegExp('[0-9]\/' + file_name))
                });

                var combined_cross_host_measurement = combineCrossHostMeasurements(cross_host_files);
                files.push(combined_cross_host_measurement);
            });

            return toData(files);
        }

        return {

            getPerRepetitionData: function (scope, scenario_name, file_name) {
                var results = scope.data;
                console.log(results);
                console.log(getMeanPerRepetition(getRepetitions('scenario_1'), 'available_peer_counter.csv'));
            }
        }
    }
);