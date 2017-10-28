var redirectTimeout = 2000;

$(document).ready(function() {
    var features = null;
    var param = getParameterByName("name");
    $("#name").alphanum({
        allow :    '-.',
        allowSpace : false,
        maxLength : 15
    });
    
    $.ajax({
        type : "GET",
        dataType : "json",
        url : "/groups",
        success : function(data) {
            var groups = data.groups;
            for (var i = 0; i < groups.length; i++) {
                $("#groups").append($("<option>", {
                    value : groups[i].name,
                    text : groups[i].name
                }));
            }
        }
    });

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
        url : "/features",
        success : function(data) {
            features = data.features;
            for (var i = 0; i < features.length; i++) {
                var j = i;
                var isEnabled = Boolean(features[i].enabled);
                var tag = isEnabled ? "is-success" : "is-danger";
                var enabled = "<span class='tag " + tag + "'>" + isEnabled + "</span>";
                var strategy = features[i].strategy === undefined ? "" : features[i].strategy;
                var groups = features[i].groups === undefined ? "" : features[i].groups.join();
                $("#features-table tr:last").after("<tr><th>" + ++j + "</th><td><a href=add_feature.html?name=" + features[i].name + ">" + encodeURIComponent(features[i].name) + "</a></td><td>" + features[i].description + "</td><td>" + strategy + "</td><td>" + groups + "</td><td>" + enabled + "</td></tr>");
            }
        }
    });

    if (param != null) {
        $.ajax({
            type : "GET",
            dataType : "json",
            url : "/features/" + param,
            success : function(data) {
                var name = data.name;
                var description = data.description;
                var strategy = data.strategy;
                var properties = JSON.stringify(data.properties, undefined, 4);
                var groups = data.groups;
                var enabled = Boolean(data.enabled);
                if (enabled) {
                    $("#enabledYes").prop("checked", true);
                } else {
                    $("#enabledNo").prop("checked", true);
                }
                $("#name").val(name);
                $("#name").prop("disabled", true);
                $("#description").val(description);
                if (strategy === undefined) {
                    $("#strategy").val("not_set");
                } else {
                    $("#strategy").val(strategy);
                }
                $("#properties").val(properties);
                $("#groups").val(groups);
                $("#create").text("Update");
                $("#create").prop("onclick", null).off("click");
                $("#create").click(function() {
                    updateFeature();
                });
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

function removeFeature() {
    var result = confirm("Want to delete?");
    if (!result) {
        return;
    }
    var param = getParameterByName('name');
    if (param != null) {
        $.ajax({
            type : "DELETE",
            url : "/features/" + param,
            success : function(data) {
                $("#feature-deleted").show();
                window.setTimeout(function() {
                    var url = "features.html";
                    $(location).attr("href", url);
                }, redirectTimeout);
            }
        });
    }
}

function addFeature() {
    var name = $.trim($("#name").val());
    var description = $("#description").val();
    var strategyValue = $("#strategy").val();
    var strategy = strategyValue === "not_set" ? null : strategyValue;
    var groupsValue = $("#groups").val();
    var groups = groupsValue === "not_set" ? null : groupsValue;
    var propertiesValue = $("#properties").val();
    var properties = propertiesValue === "" ? null : propertiesValue;
    var enabled = $("#enabledYes").is(":checked") ? true : false;
    var data = {
        "name" : name,
        "description" : description,
        "strategy" : strategy,
        "groups" : groups,
        "enabled" : enabled,
        "properties" : "dummy"
    };
    data.properties = JSON.parse(properties);

    if (!name) {
        $("#error-message").show();
        return false;
    }

    $.ajax({
        type : "POST",
        data : JSON.stringify(data),
        url : "/features",
        success : function(data) {
            $("#feature-added").show();
            $("#error-message").hide();
            window.setTimeout(function() {
                var url = "features.html";
                $(location).attr("href", url);
            }, redirectTimeout);
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            $("#error-message").show();
        }
    });
}

function updateFeature() {
    var name = $("#name").val();
    var description = $("#description").val();
    var strategyValue = $("#strategy").val();
    var strategy = strategyValue === "not_set" ? null : strategyValue;
    var groupsValue = $("#groups").val();
    var groups = groupsValue === "not_set" ? null : groupsValue;
    var propertiesValue = $("#properties").val();
    var properties = propertiesValue === "" ? null : propertiesValue;
    var enabled = $("#enabledYes").is(":checked") ? true : false;
    var data = {
        "name" : name,
        "description" : description,
        "strategy" : strategy,
        "groups" : groups,
        "enabled" : enabled,
        "properties" : "dummy"
    };
    data.properties = JSON.parse(properties);

    $.ajax({
        type : "PUT",
        data : JSON.stringify(data),
        url : "/features/" + name,
        success : function(data) {
            $("#feature-updated").show();
            $("#error-message").hide();
            window.setTimeout(function() {
                var url = "features.html";
                $(location).attr("href", url);
            }, redirectTimeout);
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            $("#error-message").show();
        }
    });
}