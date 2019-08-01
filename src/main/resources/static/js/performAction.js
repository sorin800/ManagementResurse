$(document).ready(function() {
    $('#performActionButton').click(function() {
        event.preventDefault();
        ajaxPost();
    })

    function ajaxPost() {
        $.ajax({
            type : "GET",
            url : "/performAction",
            contentType : "application/json",
            data : {
                'fileName' : $('#fileName').val(),
                'action' : $('#action').val()
            },
            success : function(data) {
                if (data !== "success" || data !== "unsuccessful") {
                	var sampleArr = base64ToArrayBuffer(data);
                	saveByteArray("Sample Report", sampleArr);

                    var div = document.createElement("div");
                    div.style.width = "500px";
                    div.style.height = "300px";
                    div.style.color = "black";
                    div.innerHTML = data;

                    document.getElementById("mainDiv").appendChild(div);
                } else {
                    alert(data);
                }
                console.log(data);
            },
            error : function(e) {
                alert("Error!");
                console.log(e);
            }
        });


        function base64ToArrayBuffer(base64) {
            var binaryString = window.atob(base64);
            var binaryLen = binaryString.length;
            var bytes = new Uint8Array(binaryLen);
            for (var i = 0; i < binaryLen; i++) {
               var ascii = binaryString.charCodeAt(i);
               bytes[i] = ascii;
            }
            return bytes;
         }
        
        function saveByteArray(reportName, byte) {
            var blob = new Blob([byte], {type: "img/png"});
            var link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            var fileName = reportName;
            link.download = fileName;
            link.click();
        };
        
    }
})