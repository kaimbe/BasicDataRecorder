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
    var type = tds[2].textContent.trim();
    tds[2].innerHTML = "<select id='data-type'><option>text</option><option>real</option><option>integer</option></select>";
    tds[2].setAttribute("field", type );
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
    var inputs = tr.querySelectorAll("td input, td select");
   
    var rec = {
        name : inputs[0].value,
        type : inputs[1].value,
        index : recno
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
    var add_but = document.querySelector("#add-entry");
    var name_field = document.querySelector( "#data-name" );
    var type_box = document.querySelector( "#data-type" );
    if ( add_but == null ) return;
    var add_tr = add_but.parentNode.parentNode;
    
    	
    function clear_fields() {
    	name_field.value = "";
    }
    var recno = 0;
    add_but.addEventListener('click', function(evt) {
    	recno++;
        var rec = {
            name : name_field.value,
            type : type_box.value,
            index : recno
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
                    "<td>" + rec.name + "</td>" +
                    "<td>"+ rec.type + "</td>" +
                    "<td><button class='edit' recno='" + recAdded + "'>Edit</button>" +
                    "<button class='del' recno='" + recAdded + "'>Delete</button></td>";
                    var nb = tr.querySelector("button.edit");
                    nb.addEventListener('click', edit_item, false);
                    nb = tr.querySelector("button.del");
                    nb.addEventListener('click', delete_item, false);
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

function create_proj_init(init_evt) {
	try {
	var create_but = document.querySelector("#create-proj");
	
	create_but.addEventListener('click', function(evt) {
		var cmd = {cmd : "create"};
		var json = JSON.stringify( cmd );
		local_ajax_mod.ajax_request( {
            method : "POST",
            link: window.location.pathname + '/create',
            doc : json,
            mime : 'application/json',
            ok_fn :  function( req ) {
                try {
                    var recAdded = JSON.parse( req.responseText );
                    console.log( "creating: " + recAdded );
                    window.location = "/proj/user/project_properties/" + recAdded;
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
	catch (e) {
		console.log(e);
	}
}

window.addEventListener( 'load', function(evt) {
        delete_button_init();
        add_entry_init();
        edit_button_init();
        create_proj_init();
    }, false );

})();