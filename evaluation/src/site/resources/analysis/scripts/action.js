define(['query'], function (query) {
    return{
        metricClicked: function (index) {
            query.metric = index;
            query.update();
        },

        filterChecked: function (checkbox, index, property) {

            var selection = query[property];
            var element_index = selection.indexOf(index);
            if (!checkbox.checked) {
                selection.removeIfExists(index);
            } else {
                selection.addIfAbsent(index);
            }
            query.update();
        }
    }
})