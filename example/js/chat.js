// Global variables.
var authorized = false;
var websocket = new WebSocket('ws://127.0.0.1:1337'); // IP_Address:Port

var onlinelistServer = [];
var onlinelistWebchat = [];
var autoscroll = 1;

message_history = [];
message_counter = 0;
history_counter = -1;

// On document ready.
$(document).ready(function ()
{
	// Keyboard actions
	$("#message").keyup(function (e)
	{
		code = (e.keyCode ? e.keyCode : e.which);

		// Enter
		if (code == 13)
		{
			var message = $(this).val();
			var scriptCheck = message.toLowerCase();

			if (message)
			{
				sendMessage(htmlEncode(message));

				message_history[message_counter++] = message;
				history_counter = message_counter;
			}
		}
		// Up arrow
		else if (code == 38)
		{
			e.preventDefault();
			if (history_counter > 0)
			{
				$(this).val(message_history[--history_counter]);
			}
		}
		// Down arrow
		else if (code == 40)
		{
			e.preventDefault();
			if (history_counter >= 0)
			{
				$(this).val(message_history[++history_counter]);
			}
		}
	});

	// Autoscroll Button
	$("#autoscrollButton").on("click", function (e)
	{
		e.preventDefault();
		if (autoscroll == 1)
		{
			autoscroll = 0;
			$(this).addClass("active");
			$(this).text("Autoscroll OFF");
		}
		else
		{
			autoscroll = 1;
			$(this).removeClass("active");
			$(this).text("Autoscroll ON");
		}
	});
});

websocket.onopen = function ()
{
    websocket.send('sso.authorize=' + ssoTicket);
};

websocket.onmessage = function (message)
{
    console.log(message);

    if (message.data == 'sso.validated' && !authorized)
	{
        console.log('Authorization complete.');
        authorized = true;
        return;
    }

    if (message.data == 'bad.sso')
	{
        alert('Bad SSO-Ticket, refresh the page.');
        return;
    }

    if (message.data == 'network.unreachable')
	{
        alert('Could not access the server.');
        return;
    }

    if (message.data == 'needs.profile')
	{
        alert('You need to login to the actual server at least one time before you can use the webchat.');
        return;
    }

    if (message.data == 'player.muted')
	{
        alert('You cannot chat because you are muted!');
        return;
    }

    if (message.data == 'player.banned')
	{
        alert('You cannot chat because you are banned!');
        return;
    }

	// Create array by splitting at the first '='
    var bits = message.data.split(/=(.+)/);
    var header = bits[0];
    var body = bits[1];

    var line = false;
    var onlinePlayerS = false;
    var onlinePlayerW = false;
    var offlinePlayerS = false;
    var offlinePlayerW = false;

    switch (header)
	{
        case 'online.list':
        	onlinePlayerS = body;
        break;
        case 'online.list.webchat':
        	onlinePlayerW = body;
        break;
        case 'chat.history':
        	line = "&f" + body;
        break;
        case 'player.join':
        	line = "&e" + body + " joined the game.";
        	onlinePlayerS = body;
        break;
        case 'player.leave':
        	line = "&e" + body + " left the game.";
        	offlinePlayerS = body;
        break;
        case 'player.join.webchat':
        	line = "&e" + body + " joined the webchat.";
        	onlinePlayerW = body;
        break;
        case 'player.leave.webchat':
        	line = "&e" + body + " left the webchat.";
        	offlinePlayerW = body;
        break;
        case 'chat.receive':
        	line = "&f" + body;
        break;
    }

    // Add online players to list
	if (onlinePlayerS)
	{
		onlinelistServer[onlinelistServer.length] = onlinePlayerS;
	}

    // Add online webchat players to list
	if (onlinePlayerW)
	{
		onlinelistWebchat[onlinelistWebchat.length] = onlinePlayerW;
	}

    // Remove online players to list
	if (offlinePlayerS)
	{
		var index = onlinelistServer.indexOf(offlinePlayerS);

		if (index!=-1)
		{
			onlinelistServer.splice(index, 1);
		}
	}

    // Remove online webchat players to list
	if (offlinePlayerW)
	{
		var index = onlinelistWebchat.indexOf(offlinePlayerW);

		if (index!=-1)
		{
			onlinelistWebchat.splice(index, 1);
		}
	}

	// Display this line in the chatlog
	if (line)
	{
		line = parseColorsAndLinks(htmlEncode(line));
		$("#chatlog").append("<div class=\"message\">" + line + "</div>");
	}

	// Scroll to the bottom automatically.
	if (autoscroll)
		$("#chatlog").scrollTop(10000000);

	updateOnlineLists();

    return;
};

websocket.onclose = function ()
{
    alert('Could not access the server. Refresh the page.');
};

function sendMessage(msg)
{
    if (authorized)
	{
    	// Send message
        websocket.send('chat.send=' + msg);

		// Clear the text box.
		$("#message").val("");
    }
}

function updateOnlineLists()
{
	$("#onlinelistServer").html(parseColorsAndLinks(htmlEncode("&6Server Players: &f"+onlinelistServer.join(", "))));
	$("#onlinelistWebchat").html(parseColorsAndLinks(htmlEncode("&6Webchat Players: &f"+onlinelistWebchat.join(", "))));
}

// Parse colors and font styles.
function parseColorsAndLinks(string)
{
	// Add the colors.
	string = string.replace(/&amp;a/g, "</span><span style='color: #55ff55'>", string);
	string = string.replace(/&amp;b/g, "</span><span style='color: #55ffff'>", string);
	string = string.replace(/&amp;c/g, "</span><span style='color: #ff5555'>", string);
	string = string.replace(/&amp;d/g, "</span><span style='color: #ff55ff'>", string);
	string = string.replace(/&amp;e/g, "</span><span style='color: #ffff55'>", string);
	string = string.replace(/&amp;f/g, "</span><span style='color: #FFFFFF'>", string);
	string = string.replace(/&amp;1/g, "</span><span style='color: #0000aa'>", string);
	string = string.replace(/&amp;2/g, "</span><span style='color: #00aa00'>", string);
	string = string.replace(/&amp;3/g, "</span><span style='color: #00aaaa'>", string);
	string = string.replace(/&amp;4/g, "</span><span style='color: #aa0000'>", string);
	string = string.replace(/&amp;5/g, "</span><span style='color: #aa00aa'>", string);
	string = string.replace(/&amp;6/g, "</span><span style='color: #ffaa00'>", string);
	string = string.replace(/&amp;7/g, "</span><span style='color: #aaaaaa'>", string);
	string = string.replace(/&amp;8/g, "</span><span style='color: #555555'>", string);
	string = string.replace(/&amp;9/g, "</span><span style='color: #5555FF'>", string);
	string = string.replace(/&amp;0/g, "</span><span style='color: #000000'>", string);

	// Add the styles.
	string = string.replace(/&lt;/g, "&#60;", string);	// So &l doesn't over take less thans.
	string = string.replace(/&amp;k/g, "</span><span style='color: #202020;'>", string); // Just hidden because scramble-y letters.
	string = string.replace(/&amp;l/g, "</span><span style='font-weight: bold;'>", string);
	string = string.replace(/&amp;m/g, "</span><span style='text-decoration: line-through;'>", string);
	string = string.replace(/&amp;n/g, "</span><span style='text-decoration: underline;'>", string);
	string = string.replace(/&amp;o/g, "</span><span style='font-style: italic;'>", string);
	string = string.replace(/&amp;r/g, "</span><span>", string);

	// REGEX for URL Linking.
	var exp = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig;
	string = string.replace(exp,"<a href='$1' target='_blank'>$1</a>");

	// Return.
	return string;
}

function htmlEncode(value)
{
	// Create a in-memory div, set it's inner text (which jQuery automatically encodes).
	// Then grab the encoded contents back out. The div never exists on the page.
	return $('<div/>').text(value).html();
}
