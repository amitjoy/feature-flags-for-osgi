$(document).ready(function() {
    $.ajax({
        type : "GET",
        dataType : "json",
        url : "/rest/strategies",
        success : function(data) {
            var count = data.length;
            $("#noOfStrategies").html(count);
        }
    });
    $.ajax({
        type : "GET",
        dataType : "json",
        url : "/rest/features",
        success : function(data) {
            var count = data.length;
            $("#noOfFeatures").html(count);
        }
    });
    $.ajax({
        type : "GET",
        dataType : "json",
        url : "/rest/groups",
        success : function(data) {
            var count = data.length;
            $("#noOfGroups").html(count);
        }
    });
});