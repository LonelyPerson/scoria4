CREATE TABLE IF NOT EXISTS `ctf_teams` (
  `teamId` int(4) NOT NULL default '0',
  `teamName` varchar(255) NOT NULL default '',
  `teamX` int(11) NOT NULL default '0',
  `teamY` int(11) NOT NULL default '0',
  `teamZ` int(11) NOT NULL default '0',
  `teamColor` VARCHAR(11) NOT NULL default '0',
  `flagX` int(11) NOT NULL default '0',
  `flagY` int(11) NOT NULL default '0',
  `flagZ` int(11) NOT NULL default '0',
  PRIMARY KEY (`teamId`)
) DEFAULT CHARSET=utf8;

INSERT INTO ctf_teams VALUES ('0', 'team1', '148380', '46723', '-3430', '00ff00', '148250', '46723', '-3430');
INSERT INTO ctf_teams VALUES ('1', 'team2', '150550', '46723', '-3430', 'ff0000', '150700', '46723', '-3430');