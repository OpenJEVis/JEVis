LOCK TABLES `sample` WRITE;

alter table sample modify note varchar(254);

UNLOCK TABLES;
