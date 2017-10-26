$(document).ready(function() {
    $.ajax({
        type : "GET",
        dataType : "json",
        url : "http://localhost:8080/strategies",
        success : function(data) {
            var count = data.strategies.length;
            $('#noOfStrategies').html(count);
        }
    });
    $.ajax({
        type : "GET",
        dataType : "json",
        url : "http://localhost:8080/features",
        success : function(data) {
            var count = data.features.length;
            $('#noOfFeatures').html(count);
        }
    });
    $.ajax({
        type : "GET",
        dataType : "json",
        url : "http://localhost:8080/groups",
        success : function(data) {
            var count = data.groups.length;
            $('#noOfGroups').html(count);
        }
    });
});