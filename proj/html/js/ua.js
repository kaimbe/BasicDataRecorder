(function() {

function delete_user( evt ) {
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
    var json = JSON.stringify([ this.getAttribute("user") ]);
    var tr_to_delete = delButton.parentNode.parentNode;
    local_ajax_mod.ajax_request( {
        method : "POST",
        link: window.location.pathname + '/delete_user',
        doc : json,
        mime : 'application/json',
        ok_fn :  function( req ) {
            try {
                var obj = JSON.parse( req.responseText );
                console.log( obj );
                if ( obj == 1 ) { // delete the row
                    tr_to_delete.parentNode.removeChild( tr_to_delete );
                }
                else {
                   alert("delete failed");
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
    var buts = document.querySelectorAll("div#user-list td button.delete-user");
    var i;
    for( i = 0 ; i < buts.length; i++ ) {
        buts[i].addEventListener('click', delete_user, false);
    }
}

function update_roles( evt ) {
    var user = this.getAttribute("user");
    // find enclosing tr, assumes dom structure, can change
    var tr = this.parentNode.parentNode;
    var cbs = tr.querySelectorAll( "td input" );
    var user_roles = [];
    user_roles.push( user );
    var i;
    for( i=0; i < cbs.length; i++ ) {
        if ( cbs[i].checked ) {
            user_roles.push(cbs[i].value);
        }
    }

    var json = JSON.stringify( user_roles );
    local_ajax_mod.ajax_request( {
        method : "POST",
        link: window.location.pathname + '/update_roles',
        doc : json,
        mime : 'application/json',
        ok_fn :  function( req ) {
            try {
                var obj = JSON.parse( req.responseText );
                console.log( obj );
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

function update_button_init() {
    var buts = document.querySelectorAll("div#user-list td button.update-roles");
    var i;
    for( i = 0 ; i < buts.length; i++ ) {
        buts[i].addEventListener('click', update_roles, false);
    }
}

function add_user_init() {
    var but = document.querySelector("#add-user");
    var username_field = document.querySelector("#un");
    var password_field = document.querySelector("#pw");
    if ( but != null ) {
        but.addEventListener('click', function(evt) {
            var add_user = {
                user: username_field.value,
                password : password_field.value };
            var json = JSON.stringify( add_user );
            local_ajax_mod.ajax_request( {
                method : "POST",
                link: window.location.pathname + '/add_user',
                doc : json,
                mime : 'application/json',
                ok_fn :  function( req ) {
                    try {
                        var obj = JSON.parse( req.responseText );
                        console.log( obj );
                        // follow redirect
                        if ( "redirect" in obj ) {
                            window.location = obj.redirect;
                            return;
                        }
                        username_field.value = 'error';
                        password_field.value = '';
                        alert("add user error");
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
        }, false);
    }
}

window.addEventListener( 'load', function(evt) {
        delete_button_init();
        update_button_init();
        add_user_init();
    }, false );

})();
