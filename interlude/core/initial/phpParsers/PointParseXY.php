<?php
$text = '
<a>
              <point x="73694" y="-156000"/>
              <point x="81286" y="-149400"/>
</a>
';
$zamok_id = '21530';
$xml = new SimpleXMLElement($text);
$i = 0;
foreach($xml->children() as $value) {
echo "({$zamok_id}, {$i}, {$value[x]}, {$value[y]}),\n";
$i++;
}
?>