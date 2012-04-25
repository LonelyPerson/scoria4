-- ---------------------------
-- Table structure for accounts
-- ---------------------------
CREATE TABLE IF NOT EXISTS `accounts` (
  `login` VARCHAR(45) NOT NULL default '',
  `password` VARCHAR(45) ,
  `lastactive` DECIMAL(20),
  `access_level` INT NOT NULL default '0',
  `lastIP` VARCHAR(20),
  `lastServer` int(4) default 1,
  `allowed_ip` varchar(20) NOT NULL default '*',
  `hwid` VARCHAR(32) NOT NULL default '*',
  PRIMARY KEY (`login`)
);
