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