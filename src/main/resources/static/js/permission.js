
$(document).ready(function() {
	$('#givePermission').click(function() {
		event.preventDefault();
		ajaxPost();
	})

	function ajaxPost() {
		$.ajax({
			type : "GET",
			url : "/givePermission",
			contentType : "application/json",
			data : {
				'user' : $('#form1').val(),
				'filePath' : $('#form2').val(),
				'permission' : $('#form3').val()
			},
			success : function(data) {
				alert(data);
			},
			error : function(e) {
				alert("Error!");
				console.log(e);
			}
		});

	}
})