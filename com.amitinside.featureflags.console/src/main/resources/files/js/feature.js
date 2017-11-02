var redirectTimeout = 2000;

$(document).ready(function() {
    var features = null;
    var param = getParameterByName("name");
    $("#name").alphanum({
        allow : '-.',
        allowSpace : false,
        maxLength : 20
    });

    $.ajax({
        type : "GET",
        dataType : "json",
        url : "/rest/groups",
        success : function(data) {
            for (var i = 0; i < data.length; i++) {
                $("#groups").append($("<option>", {
                    value : data[i].name,
                    text : data[i].name
                }));
            }
        }
    });

    $.ajax({
        type : "GET",
        dataType : "json",
        url : "/rest/strategies",
        success : function(data) {
            for (var i = 0; i < data.length; i++) {
                $("#strategy").append($("<option>", {
                    value : data[i].name,
                    text : data[i].name
                }));
            }
        }
    });

    $.ajax({
        type : "GET",
        dataType : "json",
        url : "/rest/features",
        success : function(data) {
            features = data;
            for (var i = 0; i < data.length; i++) {
                var j = i;
                var isEnabled = Boolean(data[i].enabled);
                var tag = isEnabled ? "checked" : "";
                var enabled = "<label class='switch'><input type='checkbox' " + tag + " disabled> <span class='slider'></span></label>";
                var strategy = data[i].strategy === undefined ? "" : data[i].strategy;
                var groups = data[i].groups === undefined ? "" : data[i].groups.join();
                $("#features-table tr:last").after("<tr><th>" + ++j + "</th><td><a href=add_feature.html?name=" + data[i].name + ">" + encodeURIComponent(data[i].name) + "</a></td><td>" + data[i].description + "</td><td>" + strategy + "</td><td>" + groups + "</td><td>" + enabled + "</td></tr>");
            }
        }
    });

    if (param != null) {
        $.ajax({
            type : "GET",
            dataType : "json",
            url : "/rest/features/" + param,
            success : function(data) {
                var name = data.name;
                var description = data.description;
                var strategy = data.strategy;
                var properties = JSON.stringify(data.properties, undefined, 4);
                var groups = data.groups;
                var enabled = Boolean(data.enabled);
                if (enabled) {
                    $("#enabled").prop("checked", true);
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

function removeFeature() {
    var result = confirm("Want to delete?");
    if (!result) {
        return;
    }
    var param = getParameterByName('name');
    if (param != null) {
        $.ajax({
            type : "DELETE",
            url : "/rest/features/" + param,
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
    var groups = groupsValue[0] === "not_set" ? null : groupsValue;
    var propertiesValue = $("#properties").val();
    var properties = propertiesValue === "" ? null : propertiesValue;
    var enabled = $("#enabled").is(":checked") ? true : false;
    var data = {
        "name" : name,
        "description" : description,
        "strategy" : strategy,
        "groups" : groups,
        "enabled" : enabled,
        "properties" : "dummy"
    };
    data.properties = safelyParseJSON(properties);

    if (!name) {
        $("#error-message").show();
        return false;
    }

    $.ajax({
        type : "POST",
        data : JSON.stringify(data),
        url : "/rest/features",
        contentType: "application/json",
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
    var groups = groupsValue[0] === "not_set" ? null : groupsValue;
    var propertiesValue = $("#properties").val();
    var properties = propertiesValue === "" ? null : propertiesValue;
    var enabled = $("#enabled").is(":checked") ? true : false;
    var data = {
        "name" : name,
        "description" : description,
        "strategy" : strategy,
        "groups" : groups,
        "enabled" : enabled,
        "properties" : "dummy"
    };
    data.properties = safelyParseJSON(properties);

    $.ajax({
        type : "PUT",
        data : JSON.stringify(data),
        url : "/rest/features/" + name,
        contentType: "application/json",
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