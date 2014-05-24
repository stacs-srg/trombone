define(['jquery', 'util', 'json', 'csv', 'jszip', 'jszip_utils'],
    function ($, util) {

        return function (name) {

            var home = util.resultsPath + name;
            var scenario_json = $.getJSON(home + '/scenario.json').fail(
                function (error) {
                    console.log('failed to read scenario.json of scenario ' + name, error);
                }
            );
            var repetitions_json = $.getJSON(home + '/repetitions/repetitions.json').fail(
                function (error) {
                    console.log('failed to read repetitions.json of scenario ' + name, error);
                }
            );

            var repetitionZipFiles = function () {

                var zip_files = [];
                var zip_loads = []
                repetitions_json.done(function (repetitions) {
                    repetitions.forEach(function (zip_name) {

                        var deffered = $.Deferred(function () {
                            var zip_path = home + '/repetitions/' + zip_name;
                            JSZipUtils.getBinaryContent(zip_path, function (error, data) {
                                if (error) {
                                    deffered.rejectWith(zip_path, [error]);
                                }
                                zip_files.push(new JSZip(data))
                                deffered.resolve();
                            });
                        });

                        zip_loads.push(deffered);
                    }, this)
                });

                var future_zips = $.Deferred();
                $.when.apply($, zip_loads).done(function () {
                    future_zips.resolve([zip_files]);
                }).fail(function (error) {
                    future_zips.reject([error]);
                });

                return future_zips.promise();
            }

            function toData(zipped_file) {

                var content = zipped_file.asBinary();
                var csv_as_array = $.csv.toArrays(content,
                    { onParseValue: function (value, state) {

                        var casted_value = $.csv.hooks.castToScalar(value.replace(/,/g, ""));
                        var converter = context.converters[state.colNum - 1];
                        if (state.rowNum > 1 && converter !== undefined) {
                            casted_value = converter(casted_value);
                        }
                        
                        if (isNaN(casted_value)) {
                            return  null;
                        }
                        
                        return  casted_value;
                    }}
                );

                if (context.skip_header) {
                    csv_as_array.splice(0, 1);
                }
                return csv_as_array;
            }

            return{

                /** Returns a promise of this scenario's details in JSON format.*/
                description: function () {
                    return  scenario_json;
                },
                data: function (csv_file_name) {

                    var future_data = $.Deferred();

                    repetitionZipFiles().done(function (data) {
                        data.forEach(function (zip) {
                            zip.file()
                        })
                    })

                    return future_data;
                }
            }
        }
    }
);