$(document).ready(function() {
    $("#about").click(function() {
        showModal();
    });
    $("#about-modal-close").click(function() {
        $("#about-modal").removeClass("is-active");
    });
});

function showModal() {
    $.ajax({
        type : "GET",
        url : "/exampleflag",
        success : function(data) {
            $("#about-modal").addClass("is-active");
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            alert("Feature not enabled");
        }
    });
}

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

function safelyParseJSON(json) {
    var parsed;
    try {
        parsed = JSON.parse(json);
    } catch (e) {
        $("#error-message").show();
        return false;
    }
    return parsed // Could be undefined!
}
