<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8" />
        <title>Socket Chat Example</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css" rel="stylesheet">
        <link href="css/minecraft.css" rel="stylesheet">
        <style>
            body
            {
                background-color: #282828;
                padding-top: 80px;
            }
        </style>
        <script src="//code.jquery.com/jquery.js"></script>
        <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
        <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
        <!--[if lt IE 9]>
            <script src="//oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
            <script src="//oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
        <![endif]-->
    </head>
    <body>
        <?php
            // Add your own login verfication here.
            $authenticated = true;
            if ($authenticated)
            {
                // Generate SSO Ticket for webchat
                $ticket = substr(str_shuffle("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"), 0, 50);

                // Store ticket to further verify authentication.

                // Echo ticket to Javascript.
                echo "<script> var ssoTicket = '".$ticket."'; </script>";
            }
            else
            {
                // Echo back that we're not authenticated.
                echo "<script> var ssoTicket = 'NOTICKET'; </script>";
            }
        ?>
        <script>
            // Further authentication, fill these accordingly.
            var userip = "127.0.0.1";
            var username = "Username";
        </script>
        <div class="container">
            <div class="row">
                <div class="col-sm-offset-2 col-sm-8">
                    <?php if ($authenticated): ?>
                        <!-- Chat Client -->
                            <div class="well well-sm">
                                <div id="online" class="minecraft">
                                    <span id="onlinelistServer"></span>
                                    <br>
                                    <span id="onlinelistWebchat"></span>
                                </div>
                                <br>
                                <div id="chatlog" class="minecraft minecraft-font"></div>
                                <div class="input-group">
                                    <input type="text" class="form-control darkinput" id="message" autocomplete="off" maxlength="225">
                                    <span class="input-group-btn">
                                        <a href="#" role="button" id="autoscrollButton" class="btn btn-default pull-right" style="margin-right: -1px;border-radius: 0px;border-bottom-right-radius: 4px;border-top: 1px solid #9e9e9e;">Autoscroll ON</a>
                                    </span>
                                </div>
                            </div>
                        <!-- End Chat Client -->
                    <?php endif; ?>
                </div>
            </div>
        </div>
        <script src="//netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>
        <script src="js/chat.js"></script>
    </body>
</html>
