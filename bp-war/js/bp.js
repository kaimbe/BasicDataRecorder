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
    var dv = tds[1].textContent.trim();
    tds[1].innerHTML = "<input type='text' size='12' value='" + dv + "'>";
    tds[1].setAttribute("field", dv );
    var sysv = tds[2].textContent.trim();
    tds[2].innerHTML = "<input type='text' size='5' value='" + sysv + "'>";
    tds[2].setAttribute("field", sysv );
    var diav = tds[3].textContent.trim();
    tds[3].innerHTML = "<input type='text' size='5' value='" + diav + "'>";
    tds[3].setAttribute("field", diav );
    var pv = tds[4].textContent.trim();
    tds[4].innerHTML = "<input type='text' size='5' value='" + pv + "'>";
    tds[4].setAttribute("field", pv );
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

    var date = datetime_parse( inputs[0].value );
    if ( date == null ) {
        alert( "bad date");
        return;
    }
    var rec = {
        epoch1970TimeMS : date.getTime(),
        systolic : parseFloat( inputs[1].value ),
        diastolic : parseFloat( inputs[2].value ),
        pulseRate : parseFloat( inputs[3].value ),
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

function datetime_parse( str ) {
    var mat = str.match(/(\d+)\/(\d+)\/(\d+)\s+(\d+):(\d+)/);
    if ( mat != null ) {
        var year = parseInt(mat[1])+2000;
        var month = parseInt(mat[2]) - 1;
        var day = parseInt(mat[3]);
        var hour = parseInt(mat[4]);
        var minute = parseInt(mat[5]);
        return new Date( year, month, day, hour, minute );
    }
    else {
        return null;
    }
}

function datetime_format( dt ) {
    var pad = function(n){return n<10 ? '0'+n : n};
    var y = pad(dt.getFullYear()-2000);
    var m = pad(dt.getMonth() + 1);
    var d = pad(dt.getDate());
    var h = pad(dt.getHours());
    var min = pad(dt.getMinutes());
    return y + '/' + m + '/' + d + ' ' + h + ':' + min;
}

function add_entry_init( init_evt ) {
    var add_but = document.querySelector("#add-bp");
    var date_field = document.querySelector( "#date" );
    var systolic_field = document.querySelector( "#systolic" );
    var diastolic_field = document.querySelector( "#diastolic" );
    var pulse_field = document.querySelector( "#pulse" );
    if ( add_but == null ) return;
    var add_tr = add_but.parentNode.parentNode;

    function clear_fields() {
        date_field.value = "";
        systolic_field.value = "";
        diastolic_field.value = "";
        pulse_field.value = "";
    }

    add_but.addEventListener('click', function(evt) {
        var date = datetime_parse( date_field.value );
        if ( date == null ) {
            alert( "bad date");
            return;
        }
        var rec = {
            epoch1970TimeMS : date.getTime(),
            systolic : parseFloat( systolic_field.value ),
            diastolic : parseFloat( diastolic_field.value ),
            pulseRate : parseFloat( pulse_field.value )
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
                    var d = new Date(rec.epoch1970TimeMS);
                    var tr = document.createElement("tr");
                    tr.innerHTML = "<td>" +  recAdded + "</td>" +
                    "<td>"+ datetime_format(d) + "</td>" +
                    "<td>"+ rec.systolic.toFixed(1) + "</td>" +
                    "<td>"+ rec.diastolic.toFixed(1) + "</td>" +
                    "<td>"+ rec.pulseRate.toFixed(1) + "</td>" +
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

window.addEventListener( 'load', function(evt) {
        delete_button_init();
        add_entry_init();
        edit_button_init();
    }, false );

})();
