DROP TABLE IF EXISTS `buff_profile`;
CREATE TABLE `buff_profile` (
  `char_id` int(10) unsigned NOT NULL default '0',
  `profile` varchar(16) default NULL,
  `buffs` varchar(512) default NULL,
  PRIMARY KEY  (`char_id`,`profile`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;