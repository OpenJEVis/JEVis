LOCK TABLES `sample` WRITE;

alter table sample modify value varchar(8192);

UNLOCK TABLES;
