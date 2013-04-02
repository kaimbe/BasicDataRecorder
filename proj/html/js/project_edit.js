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
    var table = tr.parentNode.parentNode;
    var colNum = table.getAttribute("colnum");
    // for each col
    var i;
    for (i = 2; i <= colNum ; i++) {
    	var name = tds[i].textContent.trim();
    	tds[i].innerHTML = "<input type='text' size='10' value='" + name + "'>";
    	tds[i].setAttribute("field", name );
    }
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
    var table = tr.parentNode.parentNode;
    var colNum = table.getAttribute("colnum");
    var rec = new Array();
    rec[0] = recno;
    // for each col
    var i;
    for (i = 1; i < colNum; i++) {
    	rec[i] = inputs[i-1].value;
    }
    
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
    if ( add_but == null ) return;
    var add_tr = add_but.parentNode.parentNode;
    var table = add_tr.parentNode.parentNode;
    var colNum = table.getAttribute("colnum");
    
    var fields = document.querySelectorAll("table.editor td input.add");
    
    function clear_fields() {
    	//for each col
    	var i;
        for (i = 0; i < colNum ; i++) {
        	fields[i].value = "";
        }
    }

    add_but.addEventListener('click', function(evt) {
    	var rec = new Array();
    	// for each col
    	var i;
        for (i = 0; i < (colNum - 1); i++) {
        	rec[i] = fields[i].value;
        }
        
        var json = JSON.stringify( rec );
        console.log(json);
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
                    var new_row_start = "<td>" +  recAdded[0] + "</td><td>" + recAdded[1] + "</td>";
                    var new_row_mid = "";
                    // for each col
                    var i;
                    for (i = 0; i < (colNum - 1); i++) {
                    	new_row_mid += "<td>" + rec[i] + "</td>";
                    }
                    
                    var new_row_end = "<td><button class='edit' recno='" + recAdded + "'>Edit</button>" +
                    "<button class='del' recno='" + recAdded + "'>Delete</button>" +
                    "</td>";
                      
                    tr.innerHTML = new_row_start + new_row_mid + new_row_end;
                    
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

window.addEventListener( 'load', function(evt) {
        delete_button_init();
        add_entry_init();
        edit_button_init();
    }, false );

})();