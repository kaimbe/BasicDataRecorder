(function() {

	function login( evt ) {
		var un = document.querySelector('#un');
		var pw = document.querySelector('#pw');

		var creds = {
		        username : un.value,
		        password : pw.value
		    };

		var json = JSON.stringify( creds );

	    local_ajax_mod.ajax_request( {
	        method : "POST",
	        link: window.location.pathname,
	        doc : json,
	        mime : 'application/json',
	        ok_fn :  function( req ) {
	            try {
	                var recAdded = JSON.parse( req.responseText );
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

	function new_account( evt ) {

	}

	function login_button_init() {

		var but = document.querySelector("#loginBut");
	    but.addEventListener('click', login, false);
	}

	function add_user_button_init() {
		var but = document.querySelector('#newAccBut');
	    but.addEventListener('click', new_account, false);
	}

window.addEventListener( 'load', function(evt) {

        login_button_init();
        add_user_button_init();
    }, false );

})();