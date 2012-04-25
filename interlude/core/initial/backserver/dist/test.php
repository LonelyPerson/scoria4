<?php
$ip = "127.0.0.1";
$INIT_KEY = "ShQyhSnz2HAu4AS";
function BackEnd($n, $action, $login = "empty", $password = "empty", $email = "empty", $char = "empty", $security_cookie = "empty", $remote_ip = "0.0.0.0", $INIT_TIME = "43148423", $r_hash = "empty", $x = "0", $y = "0", $z = "0") {
global $backserver, $INIT_KEY;
	fwrite($backserver[$n], "{$INIT_KEY};{$action};$login:$password:$email;$char:$security_cookie:$remote_ip:$INIT_TIME:$r_hash:$x:$y:$z");
	$answer = fread($backserver[$n], 1000);
	return $answer;
}


$backserver[1] = fsockopen($ip,5555);

echo BackEnd(1, "online");

fclose($backserver[1]);

?>