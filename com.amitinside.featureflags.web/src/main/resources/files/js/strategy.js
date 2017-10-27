$(document).ready(function() {
    var strategies = null;
    var param = getParameterByName("name");

    $.ajax({
        type : "GET",
        dataType : "json",
        url : "/strategies",
        success : function(data) {
            strategies = data.strategies;
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
            url : "/strategies/" + param,
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

function removeStrategy() {
    var result = confirm("Want to delete?");
    if (!result) {
        return;
    }
    var param = getParameterByName("name");
    if (param != null) {
        $.ajax({
            type : "DELETE",
            url : "/strategies/" + param,
            success : function(data) {
                $("#strategy-deleted").show();
                window.setTimeout(function() {
                    var url = "strategies.html";
                    $(location).attr("href", url);
                }, 3000);
            }
        });
    }
}

function addStrategy() {
    var name = $("#name").val();
    var description = $("#description").val();
    var type = $("#type").val();
    var key = $("#prop_key").val();
    var value = $("#value").val();

    $.ajax({
        type : "POST",
        data : JSON.stringify({
            "name" : name,
            "description" : description,
            "type" : type,
            "key" : key,
            "value" : value
        }),
        url : "/strategies",
        success : function(data) {
            $("#strategy-added").show();
            $("#error-message").hide();
            window.setTimeout(function() {
                var url = "strategies.html";
                $(location).attr("href", url);
            }, 3000);
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            $("#error-message").show();
        }
    });
}

function updateStrategy() {
    var name = $("#name").val();
    var description = $("#description").val();
    var type = $("#type").val();
    var key = $("#prop_key").val();
    var value = $("#value").val();

    $.ajax({
        type : "PUT",
        data : JSON.stringify({
            "name" : name,
            "description" : description,
            "type" : type,
            "key" : key,
            "value" : value
        }),
        url : "/strategies/" + name,
        success : function(data) {
            $("#strategy-updated").show();
            $("#error-message").hide();
            window.setTimeout(function() {
                var url = "strategies.html";
                $(location).attr("href", url);
            }, 3000);
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            $("#error-message").show();
        }
    });
}