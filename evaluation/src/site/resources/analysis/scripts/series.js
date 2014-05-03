define(['util'], function (util) {

    return function (csv_file, columns) {
        return {
            csv_file: csv_file,
            columns: columns,
            converters: {
                0: util.convert.secondToHour
            },
            skip_header: true,
            addConverter: function (column_index, convertor) {
                this.converters[column_index] = convertor;
                return this;
            },
            data: function (scenario_name) {
                return util.readCSV(
                    util.analysisPath(scenario_name, this.csv_file),
                    this.columns,
                    this.converters,
                    this.skip_header
                );
            }
        }
    }
});