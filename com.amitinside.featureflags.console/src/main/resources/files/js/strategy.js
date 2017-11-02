var redirectTimeout = 2000;

$(document).ready(function() {
    var strategies = null;
    var param = getParameterByName("name");
    $("#name").alphanum({
        allow : "-.",
        allowSpace : false,
        maxLength : 20
    });
    $("#prop_key").alphanum({
        allow : "+-.?*^$[]|\{}()!", // allowing chars for REGEX
        allowSpace : false,
        maxLength : 20
    });
    $("#value").alphanum({
        allow : "+-.?*^$[]|\{}()!", // allowing chars for REGEX
        allowSpace : false,
        maxLength : 20
    });

    $.ajax({
        type : "GET",
        dataType : "json",
        url : "/strategy",
        success : function(data) {
            strategies = JSON.parse(data).elements;
            for (var i = 0; i < strategies.length; i++) {
                var j = i;
                $("#strategies-table tr:last").after("<tr><th>" + ++j + "</th><td><a href=add_strategy.html?name=" + encodeURIComponent(strategies[i].name) + ">" + strategies[i].name + "</a></td><td>" + strategies[i].description + "</td><td>" + strategies[i].type + "</td><td>" + strategies[i].key + "</td><td>" + strategies[i].value + "</td></tr>");
            }
        }
    });

    if (param != null) {
        $.ajax({
            type : "GET",
            dataType : "json",
            url : "/rest/strategies/" + param,
            success : function(data) {
                var name = data.name;
                var description = data.description;
                var type = data.type;
                var key = data.key;
                var value = data.value;
                $("#name").val(name);
                $("#name").prop("disabled", true);
                $("#description").val(description);
                $("#type").val(type);
                $("#prop_key").val(key);
                $("#value").val(value);
                $("#create").text("Update");
                $("#create").prop("onclick", null).off("click");
                $("#create").click(function() {
                    updateStrategy();
                });
                $("#delete").show();
            }
        });
    }
});

function removeStrategy() {
    var result = confirm("Want to delete?");
    if (!result) {
        return;
    }
    var param = getParameterByName("name");
    if (param != null) {
        $.ajax({
            type : "DELETE",
            url : "/rest/strategies/" + param,
            success : function(data) {
                $("#strategy-deleted").show();
                window.setTimeout(function() {
                    var url = "strategies.html";
                    $(location).attr("href", url);
                }, redirectTimeout);
            }
        });
    }
}

function addStrategy() {
    var name = $.trim($("#name").val());
    var description = $.trim($("#description").val());
    var type = $("#type").val();
    var key = $.trim($("#prop_key").val());
    var value = $.trim($("#value").val());

    var flag = !name || !key || !value;
    if (flag) {
        $("#error-message").show();
        return false;
    }

    $.ajax({
        type : "POST",
        data : JSON.stringify({
            "name" : name,
            "description" : description,
            "type" : type,
            "key" : key,
            "value" : value
        }),
        url : "/rest/strategies",
        success : function(data) {
            $("#strategy-added").show();
            $("#error-message").hide();
            window.setTimeout(function() {
                var url = "strategies.html";
                $(location).attr("href", url);
            }, redirectTimeout);
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            $("#error-message").show();
        }
    });
}

function updateStrategy() {
    var name = $.trim($("#name").val());
    var description = $.trim($("#description").val());
    var type = $("#type").val();
    var key = $.trim($("#prop_key").val());
    var value = $.trim($("#value").val());

    var flag = !name || !key || !value;
    if (flag) {
        $("#error-message").show();
        return false;
    }

    $.ajax({
        type : "PUT",
        data : JSON.stringify({
            "name" : name,
            "description" : description,
            "type" : type,
            "key" : key,
            "value" : value
        }),
        url : "/rest/strategies/" + name,
        success : function(data) {
            $("#strategy-updated").show();
            $("#error-message").hide();
            window.setTimeout(function() {
                var url = "strategies.html";
                $(location).attr("href", url);
            }, redirectTimeout);
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            $("#error-message").show();
        }
    });
}