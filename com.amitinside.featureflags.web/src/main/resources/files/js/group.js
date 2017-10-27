$(document).ready(function() {
    var groups = null;
    var param = getParameterByName("name");

    $.ajax({
        type : "GET",
        dataType : "json",
        url : "/strategies",
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
        url : "/groups",
        success : function(data) {
            groups = data.groups;
            for (var i = 0; i < groups.length; i++) {
                var j = i;
                var isEnabled = Boolean(groups[i].enabled);
                var tag = isEnabled ? "is-success" : "is-danger";
                var enabled = "<span class='tag " + tag + "'>" + isEnabled + "</span>";
                var strategy = groups[i].strategy === undefined ? "" : groups[i].strategy;
                $("#groups-table tr:last").after("<tr><th>" + ++j + "</th><td><a href=add_group.html?name=" + encodeURIComponent(groups[i].name) + ">" + groups[i].name + "</a></td><td>" + groups[i].description + "</td><td>" + strategy + "</td><td>" + enabled + "</td></tr>");
            }
        }
    });

    if (param != null) {
        $.ajax({
            type : "GET",
            dataType : "json",
            url : "/groups/" + param,
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
                $("#error-message").hide();
                $("#create").prop("onclick", null).off("click");
                $("#create").click(function() {
                    updateGroup();
                });
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
            url : "/groups/" + param,
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
    var data = {
        "name" : name,
        "description" : description,
        "strategy" : strategy,
        "enabled" : enabled,
        "properties" : "dummy"
    };
    data.properties = JSON.parse(properties);

    $.ajax({
        type : "POST",
        data : JSON.stringify(data),
        url : "/groups",
        success : function(data) {
            $("#group-added").show();
            $("#error-message").hide();
            window.setTimeout(function() {
                var url = "groups.html";
                $(location).attr("href", url);
            }, 3000);
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            $("#error-message").show();
        }
    });
}

function updateGroup() {
    var name = $("#name").val();
    var description = $("#description").val();
    var strategyValue = $("#strategy").val();
    var strategy = strategyValue === "not_set" ? null : strategyValue;
    var propertiesValue = $("#properties").val();
    var properties = propertiesValue === "" ? null : propertiesValue;
    var enabled = $("#enabledYes").is(":checked") ? true : false;
    var data = {
        "name" : name,
        "description" : description,
        "strategy" : strategy,
        "enabled" : enabled,
        "properties" : "dummy"
    };
    data.properties = JSON.parse(properties);

    $.ajax({
        type : "PUT",
        data : JSON.stringify(data),
        url : "/groups/" + name,
        success : function(data) {
            $("#group-updated").show();
            $("#error-message").hide();
            window.setTimeout(function() {
                var url = "groups.html";
                $(location).attr("href", url);
            }, 3000);
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            $("#error-message").show();
        }
    });
}