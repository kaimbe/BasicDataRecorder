(function() {

function edit_project_button( evt )  {
	
    var rec = {};
    var json = JSON.stringify( rec );
    local_ajax_mod.ajax_request( {
        method : "POST",
        link: window.location.pathname + '/edit',
        doc : json,
        mime : 'application/json',
        ok_fn :  function( req ) {
            try {
                var recAdded = JSON.parse( req.responseText );
                window.location = "/proj/user/edit_project/" + recAdded;
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

function edit_project_button_init() {
    var but = document.querySelector("#edit_project");
    but.addEventListener('click', edit_project_button, false);
}

function view_report_button( evt )  {
	
    var rec = {};
    var json = JSON.stringify( rec );
    local_ajax_mod.ajax_request( {
        method : "POST",
        link: window.location.pathname + '/report',
        doc : json,
        mime : 'application/json',
        ok_fn :  function( req ) {
            try {
                var recAdded = JSON.parse( req.responseText );
                window.location = "/proj/user/reports/" + recAdded;
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

function view_report_button_init() {
    var but = document.querySelector("#view_reports");
    but.addEventListener('click', view_report_button, false);
}

window.addEventListener( 'load', function(evt) {
	edit_project_button_init();
	view_report_button_init();
    }, false );

})();