define(['query', 'util'], function (query, util) {

    var DEFAULT_SKIP_HEADER = true;

    return{
        fetch: function (csv_file, column) {
            return this.fetch(csv_file, column, DEFAULT_SKIP_HEADER);
        },
        fetch: function (csv_file, column, skip_header) {
            
            var csv_array = util.readCSV(csv_file);

        }
    }
})