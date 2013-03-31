(function() {

function delete_item( evt ) {
    // add a confirmation message
    var cancelButton = null;
    var delButton = this;
    if ( delButton.textContent == "Delete" ) {
        delButton.textContent = "Really?";
        cancelButton = document.createElement("button");
        cancelButton.textContent = "Cancel";
        //insert after
        delButton.parentNode.insertBefore(cancelButton, delButton.nextSibling);
        cancelButton.addEventListener('click',
            function( can_evt ) {
                cancelButton.parentNode.removeChild( cancelButton );
                delButton.textContent = "Delete";
            }, false);

        return;
    }
    // must be "Really?"
    delButton.textContent = "Delete"; 
    var recno = parseInt( this.getAttribute("recno"));
    var json = JSON.stringify( recno );
    local_ajax_mod.ajax_request( {
        method : "POST",
        link: window.location.pathname + '/delete',
        doc : json,
        mime : 'application/json',
        ok_fn :  function( req ) {
            try {
                var obj = JSON.parse( req.responseText );
                console.log( obj );
                if ( obj  == "ok" ) {
                    var tr = delButton.parentNode.parentNode;
                    tr.parentNode.removeChild( tr );
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

function delete_button_init() {
    var buts = document.querySelectorAll("table.editor td button.del");
    var i;
    for( i = 0 ; i < buts.length; i++ ) {
        buts[i].addEventListener('click', delete_item, false);
    }
}

window.addEventListener( 'load', function(evt) {
        delete_button_init();
    }, false );

})();