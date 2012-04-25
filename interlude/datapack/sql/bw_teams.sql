DROP TABLE IF EXISTS `bw_teams`;
CREATE TABLE `bw_teams` (
  `teamName` varchar(255) NOT NULL DEFAULT '',
  `teamX` int(11) NOT NULL DEFAULT '0',
  `teamY` int(11) NOT NULL DEFAULT '0',
  `teamZ` int(11) NOT NULL DEFAULT '0',
  `baseX` int(11) NOT NULL DEFAULT '0',
  `baseY` int(11) NOT NULL DEFAULT '0',
  `baseZ` int(11) NOT NULL DEFAULT '0',
  `teamColor` varchar(6) NOT NULL DEFAULT '0',
  PRIMARY KEY (`teamName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `bw_teams` VALUES
('Blue', '148189', '45835', '-3413', '148189', '45835', '-3413', '0000FF'),
('Red', '150698', '45835', '-3413', '150698', '45835', '-3413', 'FF0000'),
('Green', '149496', '47826', '-3413', '149496', '47826', '-3413', '00FF00');