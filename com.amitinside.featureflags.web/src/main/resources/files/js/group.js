$(document).ready(function() {
    var groups = null;
    var param = getParameterByName("name");

    $.ajax({
        type : "GET",
        dataType : "json",
        url : "http://localhost:8080/strategies",
        success : function(data) {
            var strategies = data.strategies;
            for (var i = 0; i < strategies.length; i++) {
                $("#strategy").append($("<option>", {
                    value : strategies[i].name,
                    text : strategies[i].name
                }));
            }
        }
    });

    $.ajax({
        type : "GET",
        dataType : "json",
        url : "http://localhost:8080/groups",
        success : function(data) {
            groups = data.groups;
            for (var i = 0; i < groups.length; i++) {
                var j = i;
                var isEnabled = Boolean(groups[i].enabled);
                var tag = isEnabled ? "is-success" : "is-danger";
                var enabled = "<span class='tag " + tag + "'>" + isEnabled + "</span>";
                var strategy = groups[i].strategy === undefined ? "" : groups[i].strategy;
                $("#groups-table tr:last").after("<tr><th>" + ++j + "</th><td><a href=add_group.html?name=" + groups[i].name + ">" + groups[i].name + "</a></td><td>" + groups[i].description + "</td><td>" + strategy + "</td><td>" + enabled "</td></tr>");
            }
        }
    });

    if (param != null) {
        $.ajax({
            type : "GET",
            dataType : "json",
            url : "http://localhost:8080/groups/" + param,
            success : function(data) {
                var name = data.name;
                var description = data.description;
                var strategy = data.strategy;
                var enabled = Boolean(data.enabled);
                if (enabled) {
                    $("#enabledYes").prop("checked", true);
                } else {
                    $("#enabledNo").prop("checked", true);
                }
                var properties = JSON.stringify(data.properties, undefined, 4);
                $("#name").val(name);
                $("#name").prop("disabled", true);
                $("#description").val(description);
                if (strategy === undefined) {
                    $("#strategy").val("not_set");
                } else {
                    $("#strategy").val(strategy);
                }
                $("#properties").val(properties);
                $("#create").text("Update");
                $("#delete").show();
            }
        });
    }
});

function getParameterByName(name, url) {
    if (!url) {
        url = window.location.href;
    }
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"), results = regex.exec(url);
    if (!results) {
        return null;
    }
    if (!results[2]) {
        return "";
    }
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function removeGroup() {
    var result = confirm("Want to delete?");
    if (!result) {
        return;
    }
    var param = getParameterByName('name');
    if (param != null) {
        $.ajax({
            type : "DELETE",
            url : "http://localhost:8080/groups/" + param,
            success : function(data) {
                $("#group-deleted").show();
                window.setTimeout(function() {
                    var url = "groups.html";
                    $(location).attr("href", url);
                }, 3000);
            }
        });
    }
}

function addGroup() {
    var name = $("#name").val();
    var description = $("#description").val();
    var strategyValue = $("#strategy").val();
    var strategy = strategyValue === "not_set" ? null : strategyValue;
    var propertiesValue = $("#properties").val();
    var properties = propertiesValue === "" ? null : propertiesValue;
    var enabled = $("#enabledYes").is(":checked") ? true : false;

    $.ajax({
        type : "POST",
        data : JSON.stringify({
            "name" : name,
            "description" : description,
            "strategy" : strategy,
            "enabled" : enabled,
            "properties" : properties
        }),
        url : "http://localhost:8080/groups",
        success : function(data) {
            $('#group-added').show();
            window.setTimeout(function() {
                var url = "groups.html";
                $(location).attr("href", url);
            }, 3000);
        }
    });
}