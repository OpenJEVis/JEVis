-- MySQL dump 10.13  Distrib 5.7.21, for Linux (x86_64)
--
-- Host: 10.1.3.51    Database: jevis
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

--
-- Table structure for table `attribute`
--

DROP TABLE IF EXISTS `attribute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `attribute` (
  `name` varchar(255) NOT NULL,
  `object` bigint(20) NOT NULL,
  `mints` datetime DEFAULT NULL,
  `maxts` datetime DEFAULT NULL,
  `samplecount` int(11) DEFAULT '0',
  `period` varchar(255) DEFAULT NULL,
  `unit` varchar(45) DEFAULT NULL,
  `altsymbol` varchar(255) DEFAULT NULL,
  `inputunit` varchar(1024) DEFAULT NULL,
  `inputrate` varchar(255) DEFAULT NULL,
  `displayrate` varchar(255) DEFAULT NULL,
  `displayunit` varchar(1024) DEFAULT NULL,
  `opt` varchar(10240) DEFAULT NULL,
  PRIMARY KEY (`name`,`object`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `classrelationship`
--

DROP TABLE IF EXISTS `classrelationship`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `classrelationship` (
  `startclass` varchar(255) NOT NULL,
  `endclass` varchar(255) NOT NULL,
  `type` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`startclass`,`endclass`,`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `localization`
--

DROP TABLE IF EXISTS `localization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `localization` (
  `key` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `value` varchar(1024) DEFAULT NULL,
  `language` varchar(45) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary table structure for view `login`
--

DROP TABLE IF EXISTS `login`;
/*!50001 DROP VIEW IF EXISTS `login`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `login` AS SELECT 
 1 AS `object`,
 1 AS `login`,
 1 AS `password`,
 1 AS `enabled`,
 1 AS `sysadmin`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `object`
--

DROP TABLE IF EXISTS `object`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `object` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `link` bigint(20) DEFAULT NULL,
  `deletets` date DEFAULT NULL,
  `public` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8596 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `objectclass`
--

DROP TABLE IF EXISTS `objectclass`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `objectclass` (
  `name` varchar(255) NOT NULL,
  `icon` blob,
  `description` varchar(1024) DEFAULT NULL,
  `isunique` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `relationship`
--

DROP TABLE IF EXISTS `relationship`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `relationship` (
  `startobject` bigint(20) NOT NULL,
  `endobject` bigint(20) NOT NULL,
  `relationtype` int(11) NOT NULL,
  PRIMARY KEY (`startobject`,`endobject`,`relationtype`),
  UNIQUE KEY `UNIQUE_CHECk` (`startobject`,`endobject`,`relationtype`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sample`
--

DROP TABLE IF EXISTS `sample`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sample` (
  `object` bigint(20) NOT NULL,
  `attribute` varchar(255) NOT NULL,
  `timestamp` datetime NOT NULL,
  `value` varchar(2048) DEFAULT NULL,
  `manid` int(11) DEFAULT NULL,
  `insertts` datetime DEFAULT NULL,
  `note` varchar(45) DEFAULT NULL,
  `file` longblob,
  `filename` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`object`,`attribute`,`timestamp`),
  UNIQUE KEY `sampleindex` (`object`,`attribute`,`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `type`
--

DROP TABLE IF EXISTS `type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `type` (
  `name` varchar(255) NOT NULL,
  `jevisclass` varchar(255) NOT NULL,
  `displaytype` varchar(255) DEFAULT NULL,
  `primitivtype` int(11) DEFAULT NULL,
  `guiposition` int(11) DEFAULT NULL,
  `defaultunit` varchar(45) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `validity` varchar(45) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `altsymbol` varchar(255) DEFAULT NULL,
  `inheritedt` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`name`,`jevisclass`),
  UNIQUE KEY `attindex` (`name`,`jevisclass`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `unit`
--

DROP TABLE IF EXISTS `unit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `unit` (
  `name` varchar(45) NOT NULL,
  `siunit` varchar(45) DEFAULT NULL,
  `m` double DEFAULT NULL,
  `b` double DEFAULT NULL,
  PRIMARY KEY (`name`),
  KEY `fk_unit_1` (`siunit`),
  CONSTRAINT `fk_unit_1` FOREIGN KEY (`siunit`) REFERENCES `unit` (`name`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Final view structure for view `login`
--

/*!50001 DROP VIEW IF EXISTS `login`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`jevis`@`%` SQL SECURITY DEFINER */
/*!50001 VIEW `login` AS select `t`.`object` AS `object`,`t`.`login` AS `login`,max(`t`.`password`) AS `password`,max(`t`.`enabled`) AS `enabled`,max(`t`.`sysadmin`) AS `sysadmin` from (select `o`.`id` AS `object`,`o`.`name` AS `login`,coalesce((case when (`s`.`attribute` = 'Password') then `s`.`value` end),0) AS `password`,coalesce((case when (`s`.`attribute` = 'Enabled') then `s`.`value` end),0) AS `enabled`,coalesce((case when (`s`.`attribute` = 'Sys Admin') then `s`.`value` end),0) AS `sysadmin` from (`jevis`.`sample` `s` left join (`jevis`.`attribute` `a` left join `jevis`.`object` `o` on((`o`.`id` = `a`.`object`))) on(((`o`.`id` = `s`.`object`) and (`a`.`name` = `s`.`attribute`) and (`a`.`maxts` = `s`.`timestamp`)))) where (`o`.`type` = 'User')) `t` group by `t`.`object` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-04-13 11:19:44
