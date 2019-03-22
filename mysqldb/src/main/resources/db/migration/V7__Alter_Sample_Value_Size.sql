LOCK TABLES `sample` WRITE;

alter table sample modify value varchar(40000);

UNLOCK TABLES;
