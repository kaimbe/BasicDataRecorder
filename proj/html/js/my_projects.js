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

function make_editable( tr ) {
    var tds = tr.querySelectorAll("td");
    var name = tds[1].textContent.trim();
    tds[1].innerHTML = "<input type='text' size='10' value='" + name + "'>";
    tds[1].setAttribute("field", name );
    var owner = tds[2].textContent.trim();
    tds[2].innerHTML = "<input type='text' size='10' value='" + owner + "'>";
    tds[2].setAttribute("field", owner );
}

function revert_editable( tr ) {
    var inputs = tr.querySelectorAll("td input");
    for( var i = 0 ; i < inputs.length; i++ ) {
        inputs[i].parentNode.innerHTML = inputs[i].parentNode.getAttribute("field");
    }
}

function delete_button_init() {
    var buts = document.querySelectorAll("table.editor td button.del");
    var i;
    for( i = 0 ; i < buts.length; i++ ) {
        buts[i].addEventListener('click', delete_item, false);
    }
}

function edit_item( evt )  {
    var cancelButton = null;
    var editButton = this;
    if ( editButton.textContent == "Edit" ) {
        make_editable( editButton.parentNode.parentNode );
        editButton.textContent = "Update";
        cancelButton = document.createElement("button");
        cancelButton.textContent = "Cancel";
        //insert after
        editButton.parentNode.insertBefore(cancelButton, editButton.nextSibling);
        cancelButton.addEventListener('click',
            function( can_evt ) {
                cancelButton.parentNode.removeChild( cancelButton );
                editButton.textContent = "Edit";
                revert_editable( editButton.parentNode.parentNode );
            }, false);
        return;
    }
    editButton.textContent = "Edit";
    var nextButton = editButton.nextSibling;
    if ( nextButton.textContent == "Cancel" ) {
        nextButton.parentNode.removeChild( nextButton );
    }

    var recno = parseInt( editButton.getAttribute("recno"));
    var tr = editButton.parentNode.parentNode;
    var inputs = tr.querySelectorAll("td input");
    
    var rec = {
        name : inputs[0].value,
        owner : inputs[1].value,
        recordID : recno
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
                // XXX check for errors
                for( var i = 0 ; i < inputs.length; i++ ) {
                    inputs[i].parentNode.innerHTML = inputs[i].value;
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
    var buts = document.querySelectorAll("table.editor td button.edit");
    var i;
    for( i = 0 ; i < buts.length; i++ ) {
        buts[i].addEventListener('click', edit_item, false);
    }
}

function add_entry_init( init_evt ) {
    var add_but = document.querySelector("#add-proj");
    var name_field = document.querySelector( "#proj-name" );
    var owner_field = document.querySelector( "#proj-owner" );
    if ( add_but == null ) return;
    var add_tr = add_but.parentNode.parentNode;

    function clear_fields() {
    	name_field.value = "";
    	owner_field.value = "";
    }

    add_but.addEventListener('click', function(evt) {
        var rec = {
            name : name_field.value,
            owner : owner_field.value
        };
        var json = JSON.stringify( rec );
        local_ajax_mod.ajax_request( {
            method : "POST",
            link: window.location.pathname + '/add',
            doc : json,
            mime : 'application/json',
            ok_fn :  function( req ) {
                try {
                    var recAdded = JSON.parse( req.responseText );
                    console.log( "adding: " + recAdded );
                    var tr = document.createElement("tr");
                    tr.innerHTML = "<td>" +  recAdded + "</td>" +
                    "<td>"+ rec.name + "</td>" +
                    "<td>"+ rec.owner + "</td>" +
                    "<td><button class='edit' recno='" + recAdded + "'>Edit</button>" +
                    "<button class='del' recno='" + recAdded + "'>Delete</button>" +
                    "<button class='set' recno='" + recAdded + "'>Setup</button></td>";
                    var nb = tr.querySelector("button.edit");
                    nb.addEventListener('click', edit_item, false);
                    nb = tr.querySelector("button.del");
                    nb.addEventListener('click', delete_item, false);
                    nb = tr.querySelector("button.set");
                    nb.addEventListener('click', setup_proj, false);
                    add_tr.parentNode.insertBefore(tr, add_tr);
                    clear_fields();
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
    }, false );
}

function setup_proj(evt) {
	var but = evt.srcElement;
	var td = but.parentNode;
	var tr = td.parentNode;
	var tds = tr.querySelectorAll("td");
    var name = tds[1].textContent.trim();
	window.location = "project_setup/" + name;
}

function setup_button_init() {
	var buts = document.querySelectorAll("table.editor td button.set");
    var i;
    for( i = 0 ; i < buts.length; i++ ) {
        buts[i].addEventListener('click', setup_proj, false);
    }
}

function go_to_properties(evt) {
	var but = evt.srcElement;
	var td = but.parentNode;
	var tr = td.parentNode;
	var tds = tr.querySelectorAll("td");
    var name = tds[1].textContent.trim();
	window.location = "project_properties/" + name;
}

function go_to_properties_init() {
	var buts = document.querySelectorAll("table.editor td button.prop");
    var i;
    for( i = 0 ; i < buts.length; i++ ) {
        buts[i].addEventListener('click', go_to_properties, false);
    }
}

window.addEventListener( 'load', function(evt) {
        delete_button_init();
        add_entry_init();
        edit_button_init();
        setup_button_init();
        go_to_properties_init();
    }, false );

})();