$(document).ready(function() {
    $.ajax({
        type : "GET",
        dataType : "json",
        url : "/strategies",
        success : function(data) {
            var count = data.strategies.length;
            $("#noOfStrategies").html(count);
        }
    });
    $.ajax({
        type : "GET",
        dataType : "json",
        url : "/features",
        success : function(data) {
            var count = data.features.length;
            $("#noOfFeatures").html(count);
        }
    });
    $.ajax({
        type : "GET",
        dataType : "json",
        url : "/groups",
        success : function(data) {
            var count = data.groups.length;
            $("#noOfGroups").html(count);
        }
    });
});