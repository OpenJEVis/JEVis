LOCK TABLES `attribute` WRITE;
CREATE INDEX `idx_attribute_object`  ON `jevis`.`attribute` (object) COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;
UNLOCK TABLES;

LOCK TABLES `relationship` WRITE;
CREATE INDEX `idx_relationship_startobject`  ON `jevis`.`relationship` (startobject) COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;
CREATE INDEX `idx_relationship_endobject`  ON `jevis`.`relationship` (endobject) COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;
UNLOCK TABLES;

LOCK TABLES `object` WRITE;
CREATE INDEX `idx_object_type`  ON `jevis`.`object` (type) COMMENT '' ALGORITHM DEFAULT LOCK DEFAULT;
UNLOCK TABLES;
