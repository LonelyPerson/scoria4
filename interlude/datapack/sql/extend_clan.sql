-- ----------------------------
-- Table structure for `extend_clan`
-- ----------------------------
DROP TABLE IF EXISTS `extend_clan`;
CREATE TABLE `extend_clan` (
  `ClanId` decimal(11,0) DEFAULT NULL,
  `Msg` varchar(255) DEFAULT NULL,
  `MsgId` int(11) NOT NULL AUTO_INCREMENT,
  `Char_Name` varchar(35) NOT NULL,
  `Char_obj` decimal(11,0) NOT NULL DEFAULT '0',
  PRIMARY KEY (`MsgId`),
  KEY `MsgId` (`MsgId`)
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=latin1;