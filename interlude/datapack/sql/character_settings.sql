DROP TABLE IF EXISTS `character_settings`;

CREATE TABLE `character_settings`(
  `charId` int(11) NOT NULL,
  `param_name` varchar(64) NOT NULL,
  `param_value` varchar(255),
  PRIMARY KEY (`charId`, `param_name`)
);