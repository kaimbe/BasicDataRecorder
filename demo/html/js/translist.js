(function() {

function make_list( l ) {
    var ul = document.createElement( "ul" );
    l.forEach( function(e) {
        var li = document.createElement('li');
        li.innerHTML = e;
        ul.appendChild( li );
    } );
    return ul;
}

function send_list() {
    var items = document.querySelectorAll('div#items li span');
    var texts = [];
    var i;
    for( i = 0 ; i < items.length; i++ ) {
        texts.push( items[i].textContent );
    }
    var json = JSON.stringify( texts );
    local_ajax_mod.ajax_request( {
        method : "POST",
        link: window.location.pathname,
        doc : json,
        mime : 'application/json',
        ok_fn :  function( req ) {
            try {
                var list = JSON.parse( req.responseText );
                console.log( list );
                var res = document.getElementById('result');
                res.innerHTML = '';
                res.appendChild( make_list( list) );
            }
            catch( e ) {
                console.log( e );
            }
        }
    } );
}

function delete_item( evt ) {
    // assume <div> <li> <button> </li> </div> structure
    var li = this.parentNode;
    var parent = li.parentNode;
    parent.removeChild( li );
    send_list();
}

function delete_events_init() {
    var i;
    var lis = document.querySelectorAll("div#items li button");
    for( i = 0 ; i < lis.length; i++ ) {
        lis[i].addEventListener('click', delete_item, false);
    }
}

function add_item( evt ) {
    var content = document.getElementById('addcontent');
    var items = document.getElementById('items');
    var li = document.createElement('li');
    var span = document.createElement('span');
    span.innerHTML = content.value;
    var button = document.createElement('button');
    button.innerHTML = 'delete';
    button.setAttribute('class', 'delete');
    button.addEventListener('click', delete_item, false);
    li.appendChild( span );
    li.appendChild( button );
    items.appendChild( li );
    send_list();
}

window.addEventListener( 'load', function(evt) {
        var add_button = document.getElementById('additem');
        if ( add_button != null && typeof(add_button) !== 'undefined' ) {
            add_button.addEventListener ( 'click', add_item, false );
        }
        delete_events_init();
    }, false );

})();
