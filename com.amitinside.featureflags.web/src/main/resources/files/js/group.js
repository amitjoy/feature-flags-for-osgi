var redirectTimeout = 2000;

$(document).ready(function() {
    var groups = null;
    var param = getParameterByName("name");
    $("#name").alphanum({
        allow :    '-.',
        allowSpace : false,
        maxLength : 15
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
        url : "/groups",
        success : function(data) {
            groups = data.groups;
            for (var i = 0; i < groups.length; i++) {
                var j = i;
                var isEnabled = Boolean(groups[i].enabled);
                var tag = isEnabled ? "checked" : "";
                var enabled = "<label class='switch'><input type='checkbox' " + tag + " disabled> <span class='slider'></span></label>";
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
                    $("#enabled").prop("checked", true);
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
                }, redirectTimeout);
            }
        });
    }
}

function addGroup() {
    var name = $.trim($("#name").val());
    var description = $("#description").val();
    var strategyValue = $("#strategy").val();
    var strategy = strategyValue === "not_set" ? null : strategyValue;
    var propertiesValue = $("#properties").val();
    var properties = propertiesValue === "" ? null : propertiesValue;
    var enabled = $("#enabled").is(":checked") ? true : false;
    var data = {
        "name" : name,
        "description" : description,
        "strategy" : strategy,
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
        url : "/groups",
        success : function(data) {
            $("#group-added").show();
            $("#error-message").hide();
            window.setTimeout(function() {
                var url = "groups.html";
                $(location).attr("href", url);
            }, redirectTimeout);
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
    var enabled = $("#enabled").is(":checked") ? true : false;
    var data = {
        "name" : name,
        "description" : description,
        "strategy" : strategy,
        "enabled" : enabled,
        "properties" : "dummy"
    };
    data.properties = safelyParseJSON(properties);

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
            }, redirectTimeout);
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            $("#error-message").show();
        }
    });
}