-- ---------------------------
-- Table structure for grandboss_data
-- ---------------------------
CREATE TABLE IF NOT EXISTS grandboss_data (
  `boss_id` INTEGER NOT NULL DEFAULT 0,
  `loc_x` INTEGER NOT NULL DEFAULT 0,
  `loc_y` INTEGER NOT NULL DEFAULT 0,
  `loc_z` INTEGER NOT NULL DEFAULT 0,
  `heading` INTEGER NOT NULL DEFAULT 0,
  `respawn_time` BIGINT NOT NULL DEFAULT 0,
  `currentHP` DECIMAL(8,0) DEFAULT NULL,
  `currentMP` DECIMAL(8,0) DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY(`boss_id`)
);

INSERT IGNORE INTO `grandboss_data` VALUES 

(29001, -21564, 180723, -5787, 0, 0, 2179536, 667, 0),			-- Queen Ant
(29006, 17726, 108915, -6480, 0, 0, 162561, 575, 0),			-- Core
(29014, 55024, 17368, -5412, 10126, 0, 325124, 1660, 0),		-- Orfen
(29019, 185708, 114298, -8221, 32768, 0, 7500000, 18000, 0),	-- Antharas
(29020, 115213, 16623, 10080, 41740, 0, 790857, 3347, 0),		-- Baium
(29022, 55312, 219168, -3223, 0, 0, 858518, 1975, 0),			-- Zaken
(29045, 0, 0, 0, 0, 0, 790857, 1859, 0),						-- Frintezza
(29054, 0, 0, 0, 0, 0, 300000, 2000, 0),						-- Benom
(29062, 0, 0, 0, 0, 0, 400000, 9999, 0),						-- High Priestess van Halter
(29066, 185708, 114298, -8221, 32768, 0, 6100000, 18000, 0),	-- Antharas Weak (79)
(29067, 185708, 114298, -8221, 32768, 0, 6800000, 18000, 0),	-- Antharas Normal (79)
(29068, 185708, 114298, -8221, 32768, 0, 7500000, 18000, 0);	-- Antharas Strong (85)
-- (29028, -105200,-253104,-15264,0, 0, 16660000, 22197, 0),	-- Valakas