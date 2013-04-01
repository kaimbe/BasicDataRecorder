(function() {

function edit_item( evt )  {
	var editButton = this;
    var div = editButton.parentNode;
    var inputs = div.querySelectorAll("textarea");
    
    var rec = {
    	description : inputs[0].value,
    	users : inputs[1].value
    };
    
    
    var json = JSON.stringify( rec );
    local_ajax_mod.ajax_request( {
        method : "POST",
        link: window.location.pathname + '/update',
        doc : json,
        mime : 'application/json',
        ok_fn :  function( req ) {
            try {
                var recAdded = JSON.parse( req.responseText );
                if (recAdded === "ok") {
                	window.location = "/proj/user/my_projects";
                }
                else {
                	console.log(recAdded);
                }
            }
            catch( e ) {
                console.log( e );
            }
        },
        err_fn :  function( req ) {
            alert( 'failed: ' + req );
            console.log( req );
        }
    } );
}

function edit_button_init() {
    var but = document.querySelector("#update_properties");
    but.addEventListener('click', edit_item, false);
}

window.addEventListener( 'load', function(evt) {
        edit_button_init();
    }, false );

})();