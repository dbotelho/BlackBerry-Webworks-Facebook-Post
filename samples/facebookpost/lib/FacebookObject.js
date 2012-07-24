/**
 * @author Daniel Botelho (http://www.dbotelho.com)
 */
function FacebookObject() {
	this.json_object;
	this.access_token;
	this.onSuccess;
	this.onError;
	var current_object = this;
	this.query;
	var YOUR_APP_ID = "<YOUR_APP_ID>";
	var YOUR_REDIRECT_URI = escape("http://www.facebook.com/connect/login_success.html");
	var YOUR_APP_SECRET = "<YOUR_APP_SECRET>";
	this.state = null;
	
	this.canPost = function(){
		return (YOUR_APP_ID != "<YOUR_APP_ID>") && (YOUR_APP_SECRET != "<YOUR_APP_SECRET>");
	};

	this.post = function(message, _onSuccess, _onError) {
		current_object.onSuccess = _onSuccess;
		current_object.onError = _onError;
		current_object.query = [ 'message=' + message ];
		current_object.state = new Date().getTime();

		try {
			// live
			if (current_object.access_token) {
				var query_string = 'access_token='
						+ current_object.access_token + '&'
						+ current_object.query.join('&');
				com.dbotelho.facebook.post({
					url : "https://graph.facebook.com/me/feed",
					type : 'POST',
					data : query_string,
					success : current_object.onSuccess,
					error : function() {
						current_object.access_token = null;
						current_object.post(message, _onSuccess, _onError);
					}
				});
			} else {
				var url = 'https://m.facebook.com/dialog/oauth?'
						+ 'client_id=' + YOUR_APP_ID + '&redirect_uri='
						+ YOUR_REDIRECT_URI
						+ '&scope=read_stream,publish_stream'
						+ '&response_type=token' + '&state='
						+ current_object.state+'&display=touch';
				com.dbotelho.facebook.connect(url, urlCallback);
			}

		} catch (e) {
			alert(e);
			if (typeof current_object.onError == 'function') {
				current_object.onError();
			}
		}
	};

	function getUrlVars(newUrl) {
		var vars = [], hash;
		var hashes = newUrl.slice(newUrl.indexOf('?') + 1).split('&');
		for ( var i = 0; i < hashes.length; i++) {
			hash = hashes[i].split('=');
			vars.push(hash[0]);
			vars[hash[0]] = hash[1];
		}
		return vars;
	}
	function urlCallback(newUrl) {
		// extract token from url
		current_object.access_token = getUrlVars(newUrl)["access_token"];
		try {
			if (current_object.access_token) {
				com.dbotelho.facebook.close;
				try {
					var query_string = 'access_token='
							+ current_object.access_token + '&'
							+ current_object.query.join('&');
					com.dbotelho.facebook.post({
						url : "https://graph.facebook.com/me/feed",
						type : 'POST',
						data : query_string,
						success : current_object.onSuccess,
						error : current_object.onError
					});
				} catch (e) {
					alert(e);
					if (typeof current_object.onError == 'function') {
						current_object.onError();
					}
				}

			}
		} catch (e) {
			alert(e);
			if (typeof current_object.onError == 'function') {
				current_object.onError();
			}
		}

	}

}