define(['jquery', 'util', 'cache', 'jstat', 'jszip', 'jszip_utils'], function ($, util, cache, jstat) {

    var DEFAULT_SKIP_HEADER = true;
    var DEFAULT_CONVERTERS = {
        0: util.convert.secondToHour
    }
    var data_cache = cache();
    var repetitionss = {};

    function toData(content, context, filter) {
        var csv_as_array = $.csv.toArrays(content, { onParseValue: function (value, state) {

            var casted_value = $.csv.hooks.castToScalar(value.replace(/,/g, ""));
            var converter = context.converters[state.colNum - 1];
            if (state.rowNum > 1 && converter !== undefined) {
                casted_value = converter(casted_value);
            }
            if (isNaN(casted_value)) {
                return  0;
            }
            return  casted_value;
        } });

        if (context.skip_header) {
            csv_as_array.splice(0, 1);
        }

        if (filter !== undefined) {

            csv_as_array = filter(csv_as_array)
        }
        return csv_as_array;
    }

    var read = function (scenario_name, context, filter) {

        var path = util.analysisPath(scenario_name, context.csv_file);
        var key = path.hashCode();

//        if (!data_cache.isCached(key)) {
//
//            var content = util.read(path);
//            var csv_as_array = toData(content, context, filter);
//
//
//            data_cache.cache(key, csv_as_array);
//        }
//        return data_cache.get(key);

        var content = util.read(path);
        return toData(content, context, filter);
    }


    return function (csv_file, columns, converters, filter) {

        if (converters === undefined) {
            converters = DEFAULT_CONVERTERS;
        }

        return {
            csv_file: csv_file,
            columns: columns,
            converters: converters,
            skip_header: DEFAULT_SKIP_HEADER,
            setConverter: function (column_index, converter) {
                this.converters[column_index] = converter;
                return this;
            },
            get: function (scenario_name) {
                return read(scenario_name, this, filter).map(
                    function (value, index) {
                        var row = new Array();
                        this.columns.forEach(function (column) {
                            row.push(value[column]);
                        })
                        return row;
                    }, this);
            },
            columnAverage: function (scenario_name) {
                return jStat(this.get(scenario_name)).mean();
            },
            columnSum: function (scenario_name) {
                return jStat(this.get(scenario_name)).sum();
            },
            columnMax: function (scenario_name) {
                return jStat(this.get(scenario_name)).max();
            },
            columnMin: function (scenario_name) {
                return jStat(this.get(scenario_name)).min();
            },
            columnNormalizedAverage: function (scenario_name, column_index) {

                var value = this.columnAverage(scenario_name)[column_index]
//                var min = this.columnMin(scenario_name)[column_index];
//                var max = this.columnMax(scenario_name)[column_index];
                var min = 0;
                var max = 800;
                var max_minus_min = (max - min);
                return max_minus_min != 0 ? (value - min) / max_minus_min : 0;
            },
            repetitions: function (scenario_name, average) {

                var promise = util.repetitionZipFiles(scenario_name);
                var data = [];
                var self = this;

                var future_interval = $.Deferred();

                promise.done(function (zip_files) {
                    var means = [];
                    zip_files.forEach(function (zip) {
                        var content = zip.file(this.csv_file).asBinary();
                        var items = toData(content, this, filter);
                        data.push(items)
                        means.push(jStat(items).mean())

                    }, self);

                    var merged = [];
                    merged = merged.concat.apply(merged, jStat(means).col(1));
                    //95% confidence interval => alpha = 0.05
                    var confidence_interval = jStat.tci(average, 0.05, merged);
                    if (isNaN(confidence_interval[0])) {
                        confidence_interval[0] = null
                    }

                    if (isNaN(confidence_interval[1])) {
                        confidence_interval[1] = null
                    }
                    future_interval.resolve(confidence_interval);
                });

                return future_interval.promise();
            },
            repetitionMeans: function (scenario_name) {
                var promise = util.repetitionZipFiles(scenario_name);
                var data = [];
                var self = this;

                var future_interval = $.Deferred();

                promise.done(function (zip_files) {
                    var means = [];
                    zip_files.forEach(function (zip) {
                        var content = zip.file(this.csv_file).asBinary();
                        var items = toData(content, this, filter);
                        data.push(items)
                        means.push(jStat(items).mean())

                    }, self);

                    var merged = [];
                    merged = merged.concat.apply(merged, jStat(means).col(1));
                    future_interval.resolve(merged);
                });

                return future_interval.promise();
            }
        }
    }
})