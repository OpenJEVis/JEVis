LOCK TABLES `sample` WRITE;

alter table sample modify value varchar(21000);

UNLOCK TABLES;
