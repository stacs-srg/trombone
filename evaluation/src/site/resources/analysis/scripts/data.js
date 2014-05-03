define(['jquery', 'util', 'cache', 'jstat'], function ($, util, cache, jstat) {

    var DEFAULT_SKIP_HEADER = true;
    var DEFAULT_CONVERTERS = {
        0: util.convert.secondToHour
    }
    var data_cache = cache();
    var read = function (scenario_name, context) {
        var path = util.analysisPath(scenario_name, context.csv_file);
        var key = path.hashCode();

        if (!data_cache.isCached(key)) {

            var content = util.read(path);
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

            data_cache.cache(key, csv_as_array);
        }
        return data_cache.get(key);
    }


    return function (csv_file, columns, converters) {

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
                return read(scenario_name, this).map(
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
                return max_minus_min !=0 ? (value - min) / max_minus_min : 0;
            }

        }
    }
})