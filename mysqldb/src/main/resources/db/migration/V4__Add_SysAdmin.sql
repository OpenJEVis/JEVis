-- MySQL dump 10.13  Distrib 5.7.21, for Linux (x86_64)
--
-- Host: localhost    Database: jevis
-- ------------------------------------------------------
-- Server version	5.7.21-0ubuntu0.16.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;



LOCK TABLES `object` WRITE;
/*!40000 ALTER TABLE `object` DISABLE KEYS */;
INSERT INTO `object` VALUES (1,'System','System',NULL,NULL,0),(2,'Administration','Administration Directory',NULL,NULL,0),(3,'Services','Service Directory',NULL,NULL,0),(10,'Groups','Group Directory',NULL,NULL,0),(13,'Users','User Directory',NULL,NULL,0),(16,'Sys Admin','User',NULL,NULL,0),(19,'Sys Admins','Group',NULL,NULL,0),(20,'Organizations','Organization Directory',NULL,NULL,0),(30,'System Documentaton','Document Directory',NULL,NULL,0);
/*!40000 ALTER TABLE `object` ENABLE KEYS */;
UNLOCK TABLES;


LOCK TABLES `sample` WRITE;
/*!40000 ALTER TABLE `sample` DISABLE KEYS */;
INSERT INTO `sample` VALUES (1,'Domain Name','2018-03-20 11:14:15','test.localhost',NULL,'2018-03-20 11:14:16',NULL,NULL,NULL),(1,'Hostname','2018-03-20 11:14:16','localhost',NULL,'2018-03-20 11:14:16',NULL,NULL,NULL),(1,'Language','2018-03-20 13:50:49','DEU',NULL,'2018-03-20 13:50:59',NULL,NULL,NULL),(1,'Language','2018-03-21 13:51:55','pt',NULL,'2018-03-21 13:51:57',NULL,NULL,NULL),(1,'Language','2018-03-21 16:25:31','de',NULL,'2018-03-21 16:25:34',NULL,NULL,NULL),(1,'Language','2018-03-26 11:48:22','en',NULL,'2018-03-26 11:48:24',NULL,NULL,NULL),(1,'Local IP','2018-03-20 11:14:16','127.0.0.1',NULL,'2018-03-20 11:14:16',NULL,NULL,NULL),(1,'Public IP','2018-03-20 13:55:39','192.168.1.100',NULL,'2018-03-20 13:55:39',NULL,NULL,NULL),(1,'TimeZone','2018-03-20 13:50:56','Europe/Berlin',NULL,'2018-03-20 13:50:59',NULL,NULL,NULL),(16,'E-Mail','2017-04-12 15:39:30','',NULL,'2017-04-12 15:39:48','',NULL,NULL),(16,'E-Mail','2018-03-20 11:18:38','admin@localhost',NULL,'2018-03-20 11:18:38',NULL,NULL,NULL),(16,'Enabled','2014-06-13 15:00:27','1',NULL,'2014-06-13 15:00:52','',NULL,NULL),(16,'First Name','2017-04-12 15:39:31','',NULL,'2017-04-12 15:39:49','',NULL,NULL),(16,'First Name','2018-03-20 11:18:38','John',NULL,'2018-03-20 11:18:38',NULL,NULL,NULL),(16,'First Name','2018-03-22 16:12:51','Sys',NULL,'2018-03-22 16:12:51',NULL,NULL,NULL),(16,'First Name','2018-03-22 16:13:04','Sys',NULL,'2018-03-22 16:13:04',NULL,NULL,NULL),(16,'Last Name','2017-04-12 15:39:37','Sys Admin',NULL,'2017-04-12 15:39:49','',NULL,NULL),(16,'Last Name','2018-03-20 11:18:38','Doe',NULL,'2018-03-20 11:18:38',NULL,NULL,NULL),(16,'Last Name','2018-03-22 16:12:51','Admin',NULL,'2018-03-22 16:12:51',NULL,NULL,NULL),(16,'Last Name','2018-03-22 16:13:04','Admin',NULL,'2018-03-22 16:13:04',NULL,NULL,NULL),(16,'Password','2014-01-01 00:00:00','1000:528cb1bc150e1e101f4ba02989f9556e19fab2c1b17fce7f:7ca072deaee4d1410a95ceecea5f2e976322c5f019d3ae1d',NULL,'2014-01-08 00:00:00','Default PW',NULL,NULL),(16,'Phone','2014-06-13 15:00:03','',NULL,'2014-06-13 15:00:52','',NULL,NULL),(16,'Phone','2018-03-20 11:18:38','+49 (0)40 300857 - 0',NULL,'2018-03-20 11:18:38',NULL,NULL,NULL),(16,'Position','2017-04-12 15:39:41','',NULL,'2017-04-12 15:39:50','',NULL,NULL),(16,'Position','2018-03-20 11:18:38','Head of IT',NULL,'2018-03-20 11:18:38',NULL,NULL,NULL),(16,'Sys Admin','2014-01-01 00:00:00','1',0,'2014-01-08 00:00:00','Default Value',NULL,NULL),(16,'Title','2017-04-12 15:39:46','',NULL,'2017-04-12 15:39:50','',NULL,NULL),(16,'Title','2018-03-20 11:18:38','Prof. Dr. Dr. h.c. mult',NULL,'2018-03-20 11:18:38',NULL,NULL,NULL),(16,'Title','2018-03-22 16:13:04','Prof. Dr.',NULL,'2018-03-22 16:13:04',NULL,NULL,NULL);
/*!40000 ALTER TABLE `sample` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `attribute` WRITE;
/*!40000 ALTER TABLE `attribute` DISABLE KEYS */;
INSERT INTO `attribute` VALUES ('Domain Name',1,'2018-03-20 11:14:15','2018-03-20 11:14:15',1,'P15m',NULL,NULL,NULL,NULL,NULL,NULL,NULL),('E-Mail',16,'2017-04-12 15:39:30','2018-03-20 11:18:38',2,'P15m',NULL,NULL,'{\"formula\":\"\",\"label\":\"\",\"prefix\":\"None\"}','PT0S','PT0S','{\"formula\":\"\",\"label\":\"\",\"prefix\":\"None\"}',NULL),('Enabled',16,'2014-06-13 15:00:27','2014-06-13 15:00:27',1,'P15m',NULL,NULL,NULL,NULL,NULL,NULL,NULL),('First Name',16,'2017-04-12 15:39:31','2018-03-22 16:13:04',4,'P15m',NULL,NULL,'{\"formula\":\"\",\"label\":\"\",\"prefix\":\"None\"}','PT0S','PT0S','{\"formula\":\"\",\"label\":\"\",\"prefix\":\"None\"}',NULL),('Gender',16,NULL,NULL,0,'P15m',NULL,NULL,NULL,NULL,NULL,NULL,NULL),('Hostname',1,'2018-03-20 11:14:16','2018-03-20 11:14:16',1,'P15m',NULL,NULL,NULL,NULL,NULL,NULL,NULL),('Language',1,'2018-03-20 13:50:49','2018-03-26 11:48:22',4,'P15m','',NULL,NULL,NULL,NULL,NULL,NULL),('Language',16,NULL,NULL,0,'P15m',NULL,NULL,NULL,NULL,NULL,NULL,NULL),('Last Login',16,NULL,NULL,0,'P15m',NULL,NULL,NULL,NULL,NULL,NULL,NULL),('Last Name',16,'2017-04-12 15:39:37','2018-03-22 16:13:04',4,'P15m',NULL,NULL,'{\"formula\":\"\",\"label\":\"\",\"prefix\":\"None\"}','PT0S','PT0S','{\"formula\":\"\",\"label\":\"\",\"prefix\":\"None\"}',NULL),('Local IP',1,'2018-03-20 11:14:16','2018-03-20 11:14:16',1,'P15m',NULL,NULL,'{\"symbol\":\"\",\"prefix\":\"None\"}','PT0S','PT0S','{\"symbol\":\"\",\"prefix\":\"None\"}',''),('New Attribute',16,NULL,NULL,0,'P15m',NULL,NULL,NULL,NULL,NULL,NULL,NULL),('Password',16,'2014-01-01 00:00:00','2014-01-01 00:00:00',1,NULL,'',NULL,'{\"formula\":\"\",\"label\":\"\",\"prefix\":\"None\"}','PT0S','PT0S','{\"formula\":\"\",\"label\":\"\",\"prefix\":\"None\"}',''),('Phone',16,'2014-06-13 15:00:03','2018-03-20 11:18:38',2,'P15m',NULL,NULL,NULL,NULL,NULL,NULL,NULL),('Position',16,'2017-04-12 15:39:41','2018-03-20 11:18:38',2,'P15m',NULL,NULL,'{\"formula\":\"\",\"label\":\"\",\"prefix\":\"None\"}','PT0S','PT0S','{\"formula\":\"\",\"label\":\"\",\"prefix\":\"None\"}',NULL),('Public IP',1,'2018-03-20 13:55:39','2018-03-20 13:55:39',1,'P15m',NULL,NULL,NULL,NULL,NULL,NULL,NULL),('Sys Admin',16,'2014-01-01 00:00:00','2014-01-01 00:00:00',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),('TimeZone',1,'2018-03-20 13:50:56','2018-03-20 13:50:56',1,'P15m',NULL,NULL,NULL,NULL,NULL,NULL,NULL),('TimeZone',16,NULL,NULL,0,'P15m',NULL,NULL,NULL,NULL,NULL,NULL,NULL),('Title',16,'2017-04-12 15:39:46','2018-03-22 16:13:04',3,'P15m',NULL,NULL,'{\"formula\":\"\",\"label\":\"\",\"prefix\":\"None\"}','PT0S','PT0S','{\"formula\":\"\",\"label\":\"\",\"prefix\":\"None\"}',NULL),('UmlautÄ',16,NULL,NULL,0,'P15m',NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `attribute` ENABLE KEYS */;
UNLOCK TABLES;

LOCK TABLES `relationship` WRITE;
/*!40000 ALTER TABLE `relationship` DISABLE KEYS */;
INSERT INTO `relationship` VALUES (2,1,1),(3,1,1),(10,2,1),(19,10,1),(13,2,1),(16,13,1),(20,1,1),(30,1,1),(19,1,3);

/*!40000 ALTER TABLE `relationship` ENABLE KEYS */;
UNLOCK TABLES;


/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-04-13 13:32:32
