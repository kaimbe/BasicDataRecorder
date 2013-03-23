// local ajax module
var local_ajax_mod = (function() {

function ajax_request( ajax ) {
    var method = "GET"; // default
    if ( "method" in ajax ) {
        method = ajax.method;
    }
    var url;
    if ( "link" in ajax ) {
        url = ajax.link;
    }
    else {
        throw new Error("FIX ME, missing link");
    }
    var doc = null;
    if ( "doc" in ajax ) {
        doc = ajax.doc;
    }
    var to_time = 0;
    if ( "to_time" in ajax ) {
        to_time = ajax.to_time;
    }
    var ok_fn = null;
    if ( "ok_fn" in ajax ) {
        ok_fn = ajax.ok_fn;
    }
    var to_fn = null;
    if ( "to_fn" in ajax ) {
        to_fn = ajax.to_fn;
    }
    var err_fn = null;
    if ( "err_fn" in ajax ) {
        err_fn = ajax.to_fn;
    }
    var req = new XMLHttpRequest();
    var timed_out = false;
    var timer = null;
    req.open(method, url, true );
    req.onreadystatechange = function() {
        if  ( req.readyState == 4) {
            if ( timed_out ) {
                return;
            }
            if ( timer != null ) {
                clearTimeout( timer );
            }
            if ( req.status == 200 ) {
                if ( ok_fn != null ){ ok_fn( req ); }
            }
            else {
                if ( err_fn != null ) { err_fn( req ); }
            }
        }
    }
    if ( to_time > 0 ) {
        timer = setTimeout( function() {
            timed_out = true;
            req.abort();
            to_fn( null );
        }, to_time );
    }

    if ( "mime" in ajax ) {
        req.setRequestHeader('Content-Type', ajax.mime);
    }
    req.send( doc );
    return req;
}

// exported
return {
    ajax_request: ajax_request
};
}());
