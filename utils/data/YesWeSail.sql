-- MySQL dump 10.13  Distrib 5.7.9, for linux-glibc2.5 (x86_64)
--
-- Host: localhost    Database: yeswesail
-- ------------------------------------------------------
-- Server version	5.6.29

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
-- Table structure for table `AddressInfo`
--

DROP TABLE IF EXISTS `AddressInfo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AddressInfo` (
  `idAddressInfo` int(11) NOT NULL AUTO_INCREMENT,
  `userId` int(11) NOT NULL,
  `type` char(1) NOT NULL DEFAULT 'P',
  `companyName` varchar(45) DEFAULT NULL,
  `taxCode` varchar(15) DEFAULT NULL,
  `address1` varchar(45) DEFAULT NULL,
  `address2` varchar(45) DEFAULT NULL,
  `city` varchar(45) DEFAULT NULL,
  `zip` varchar(6) DEFAULT NULL,
  `province` char(6) DEFAULT NULL,
  `country` varchar(15) DEFAULT NULL,
  PRIMARY KEY (`idAddressInfo`),
  KEY `fk_User_idx` (`userId`),
  CONSTRAINT `fk_Address_Users` FOREIGN KEY (`userId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `AddressInfo`
--

LOCK TABLES `AddressInfo` WRITE;
/*!40000 ALTER TABLE `AddressInfo` DISABLE KEYS */;
/*!40000 ALTER TABLE `AddressInfo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Boats`
--

DROP TABLE IF EXISTS `Boats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Boats` (
  `idBoats` int(11) NOT NULL AUTO_INCREMENT,
  `ownerId` int(11) NOT NULL,
  `engineType` char(1) NOT NULL,
  `plate` varchar(20) NOT NULL,
  `name` varchar(45) NOT NULL,
  `model` varchar(45) NOT NULL,
  `length` int(11) NOT NULL,
  `year` int(11) NOT NULL,
  `cabinsWithBathroom` int(11) DEFAULT NULL,
  `cabinsNoBathroom` int(11) DEFAULT NULL,
  `sharedBathrooms` int(11) DEFAULT NULL,
  `bunks` int(11) DEFAULT NULL,
  PRIMARY KEY (`idBoats`),
  KEY `fk_Users_idx` (`ownerId`),
  CONSTRAINT `fk_Boats_Users` FOREIGN KEY (`ownerId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Boats`
--

LOCK TABLES `Boats` WRITE;
/*!40000 ALTER TABLE `Boats` DISABLE KEYS */;
/*!40000 ALTER TABLE `Boats` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Categories`
--

DROP TABLE IF EXISTS `Categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Categories` (
  `idCategories` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`idCategories`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Categories`
--

LOCK TABLES `Categories` WRITE;
/*!40000 ALTER TABLE `Categories` DISABLE KEYS */;
INSERT INTO `Categories` VALUES (1,'Relax'),(2,'Pesca'),(3,'Sport');
/*!40000 ALTER TABLE `Categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CategoriesLanguages`
--

DROP TABLE IF EXISTS `CategoriesLanguages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CategoriesLanguages` (
  `categoryId` int(11) NOT NULL,
  `languageId` int(11) NOT NULL,
  `description` varchar(45) NOT NULL,
  PRIMARY KEY (`categoryId`,`languageId`),
  KEY `fk_CategoriesLanguages_Languages_idx` (`languageId`),
  CONSTRAINT `fk_CategoriesLanguages_Category` FOREIGN KEY (`categoryId`) REFERENCES `Categories` (`idCategories`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_CategoriesLanguages_Languages` FOREIGN KEY (`languageId`) REFERENCES `Languages` (`idLanguages`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CategoriesLanguages`
--

LOCK TABLES `CategoriesLanguages` WRITE;
/*!40000 ALTER TABLE `CategoriesLanguages` DISABLE KEYS */;
INSERT INTO `CategoriesLanguages` VALUES (1,1,'Relax'),(1,2,'Relax'),(2,1,'Pesca'),(2,2,'Fishing'),(3,1,'Sport'),(3,2,'Sport');
/*!40000 ALTER TABLE `CategoriesLanguages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `EventDescription`
--

DROP TABLE IF EXISTS `EventDescription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventDescription` (
  `idEventDescription` int(11) NOT NULL AUTO_INCREMENT,
  `eventId` int(11) NOT NULL,
  `languageId` int(11) NOT NULL,
  `description` text NOT NULL,
  PRIMARY KEY (`idEventDescription`),
  KEY `fk_EventDescriptaion_Languges_idx` (`languageId`),
  CONSTRAINT `fk_EventDescriptaion_Languges` FOREIGN KEY (`languageId`) REFERENCES `Languages` (`idLanguages`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_EventDescription_Event` FOREIGN KEY (`idEventDescription`) REFERENCES `Events` (`idEvents`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `EventDescription`
--

LOCK TABLES `EventDescription` WRITE;
/*!40000 ALTER TABLE `EventDescription` DISABLE KEYS */;
/*!40000 ALTER TABLE `EventDescription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `EventTickets`
--

DROP TABLE IF EXISTS `EventTickets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventTickets` (
  `idEventTickets` int(11) NOT NULL AUTO_INCREMENT,
  `eventId` int(11) NOT NULL,
  `languageId` int(11) NOT NULL,
  `description` varchar(45) NOT NULL,
  `available` int(11) NOT NULL,
  `booked` int(11) NOT NULL,
  `price` int(11) NOT NULL,
  PRIMARY KEY (`idEventTickets`),
  KEY `fk_EventTickets_Events_idx` (`eventId`),
  KEY `fk_EventTickets_Languages_idx` (`languageId`),
  CONSTRAINT `fk_EventTickets_Events` FOREIGN KEY (`eventId`) REFERENCES `Events` (`idEvents`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_EventTickets_Languages` FOREIGN KEY (`languageId`) REFERENCES `Languages` (`idLanguages`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `EventTickets`
--

LOCK TABLES `EventTickets` WRITE;
/*!40000 ALTER TABLE `EventTickets` DISABLE KEYS */;
/*!40000 ALTER TABLE `EventTickets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `EventTypes`
--

DROP TABLE IF EXISTS `EventTypes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventTypes` (
  `idEventTypes` int(11) NOT NULL AUTO_INCREMENT,
  `languageId` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`idEventTypes`),
  KEY `fk_EventTypes_Language_idx` (`languageId`),
  CONSTRAINT `fk_EventTypes_Language` FOREIGN KEY (`languageId`) REFERENCES `Languages` (`idLanguages`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `EventTypes`
--

LOCK TABLES `EventTypes` WRITE;
/*!40000 ALTER TABLE `EventTypes` DISABLE KEYS */;
INSERT INTO `EventTypes` VALUES (1,1,'Esperienza mare'),(2,1,'Esperienza a terra'),(3,2,'See experience'),(4,2,'Land experience');
/*!40000 ALTER TABLE `EventTypes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Events`
--

DROP TABLE IF EXISTS `Events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Events` (
  `idEvents` int(11) NOT NULL AUTO_INCREMENT,
  `eventType` int(11) NOT NULL,
  `dateStart` datetime NOT NULL,
  `dateEnd` datetime NOT NULL,
  `location` varchar(45) NOT NULL,
  `categoryId` int(11) NOT NULL,
  `imageURL` varchar(120) DEFAULT NULL,
  `shipownerId` int(11) NOT NULL,
  `shipId` int(11) NOT NULL,
  `labels` varchar(90) DEFAULT NULL,
  PRIMARY KEY (`idEvents`),
  KEY `fk_Users_idx` (`shipownerId`),
  KEY `fk_Events_Boats_idx` (`shipId`),
  KEY `fk_Events_Eventtype_idx` (`eventType`),
  KEY `fk_Events_Categories_idx` (`categoryId`),
  CONSTRAINT `fk_Events_Boats` FOREIGN KEY (`shipId`) REFERENCES `Boats` (`idBoats`) ON DELETE NO ACTION,
  CONSTRAINT `fk_Events_Categories` FOREIGN KEY (`categoryId`) REFERENCES `Categories` (`idCategories`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Events_Eventtype` FOREIGN KEY (`eventType`) REFERENCES `EventTypes` (`idEventTypes`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Events_Users` FOREIGN KEY (`shipownerId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Events`
--

LOCK TABLES `Events` WRITE;
/*!40000 ALTER TABLE `Events` DISABLE KEYS */;
/*!40000 ALTER TABLE `Events` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Feedback`
--

DROP TABLE IF EXISTS `Feedback`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Feedback` (
  `idFeedback` int(11) NOT NULL AUTO_INCREMENT,
  `issuerId` int(11) NOT NULL,
  `targetId` int(11) NOT NULL,
  `feedback` text NOT NULL,
  `approved` char(1) NOT NULL DEFAULT 'N',
  PRIMARY KEY (`idFeedback`),
  KEY `fk_Feedback_Users_idx` (`issuerId`),
  KEY `fk_Feedback_Users_1_idx` (`targetId`),
  CONSTRAINT `fk_Feedback_Users_1` FOREIGN KEY (`issuerId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Feedback_Users_2` FOREIGN KEY (`targetId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Feedback`
--

LOCK TABLES `Feedback` WRITE;
/*!40000 ALTER TABLE `Feedback` DISABLE KEYS */;
/*!40000 ALTER TABLE `Feedback` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Languages`
--

DROP TABLE IF EXISTS `Languages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Languages` (
  `idLanguages` int(11) NOT NULL AUTO_INCREMENT,
  `webPrefix` char(2) NOT NULL,
  `name` varchar(20) NOT NULL,
  PRIMARY KEY (`idLanguages`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Languages`
--

LOCK TABLES `Languages` WRITE;
/*!40000 ALTER TABLE `Languages` DISABLE KEYS */;
INSERT INTO `Languages` VALUES (1,'it','Italiano'),(2,'en','English');
/*!40000 ALTER TABLE `Languages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Participants`
--

DROP TABLE IF EXISTS `Participants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Participants` (
  `idParticipants` int(11) NOT NULL AUTO_INCREMENT,
  `eventId` int(11) NOT NULL,
  `userId` int(11) NOT NULL,
  `ticketId` int(11) NOT NULL,
  PRIMARY KEY (`idParticipants`),
  KEY `fk_Participants_Event_idx` (`eventId`),
  KEY `fk_Participants_Users_idx` (`userId`),
  KEY `fk_Participants_EventTickets_idx` (`ticketId`),
  CONSTRAINT `fk_Participants_Event` FOREIGN KEY (`eventId`) REFERENCES `Events` (`idEvents`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Participants_EventTickets` FOREIGN KEY (`ticketId`) REFERENCES `EventTickets` (`idEventTickets`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Participants_Users` FOREIGN KEY (`userId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Participants`
--

LOCK TABLES `Participants` WRITE;
/*!40000 ALTER TABLE `Participants` DISABLE KEYS */;
/*!40000 ALTER TABLE `Participants` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RegistrationConfirm`
--

DROP TABLE IF EXISTS `RegistrationConfirm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RegistrationConfirm` (
  `idRegistrationConfirm` int(11) NOT NULL AUTO_INCREMENT,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `token` varchar(45) NOT NULL,
  `userId` int(11) NOT NULL,
  `status` char(1) NOT NULL,
  PRIMARY KEY (`idRegistrationConfirm`),
  KEY `fk_RegistrationConfirm_Users_idx` (`userId`),
  CONSTRAINT `fk_RegistrationConfirm_Users` FOREIGN KEY (`userId`) REFERENCES `Users` (`idUsers`) ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RegistrationConfirm`
--

LOCK TABLES `RegistrationConfirm` WRITE;
/*!40000 ALTER TABLE `RegistrationConfirm` DISABLE KEYS */;
INSERT INTO `RegistrationConfirm` VALUES (17,'2016-03-22 23:13:15','3151eb62-f21b-4a75-b2e3-a53bdfcfcf44',55,'I');
/*!40000 ALTER TABLE `RegistrationConfirm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Roles`
--

DROP TABLE IF EXISTS `Roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Roles` (
  `idRoles` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL,
  PRIMARY KEY (`idRoles`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Roles`
--

LOCK TABLES `Roles` WRITE;
/*!40000 ALTER TABLE `Roles` DISABLE KEYS */;
INSERT INTO `Roles` VALUES (1,'Viaggiatore'),(2,'Armatore'),(3,'Amministratore');
/*!40000 ALTER TABLE `Roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RolesLanguages`
--

DROP TABLE IF EXISTS `RolesLanguages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RolesLanguages` (
  `roleId` int(11) NOT NULL,
  `languageId` int(11) NOT NULL,
  `description` varchar(45) NOT NULL,
  PRIMARY KEY (`roleId`,`languageId`),
  KEY `fk_RolesLanguages_Languages_idx` (`languageId`),
  CONSTRAINT `fk_RolesLanguages_Languages` FOREIGN KEY (`languageId`) REFERENCES `Languages` (`idLanguages`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_RolesLanguages_Roles` FOREIGN KEY (`roleId`) REFERENCES `Roles` (`idRoles`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RolesLanguages`
--

LOCK TABLES `RolesLanguages` WRITE;
/*!40000 ALTER TABLE `RolesLanguages` DISABLE KEYS */;
INSERT INTO `RolesLanguages` VALUES (1,1,'Viaggiatore'),(1,2,'Traveller'),(2,1,'Armatore'),(2,2,'Ship Owner'),(3,1,'Amministratore'),(3,2,'Admin');
/*!40000 ALTER TABLE `RolesLanguages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `TicketLocks`
--

DROP TABLE IF EXISTS `TicketLocks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TicketLocks` (
  `idTicketLocks` int(11) NOT NULL AUTO_INCREMENT,
  `eventTicketId` int(11) NOT NULL,
  `lockTime` datetime NOT NULL,
  PRIMARY KEY (`idTicketLocks`),
  KEY `fk_TicketLocks_EventTicket_idx` (`eventTicketId`),
  CONSTRAINT `fk_TicketLocks_EventTicket` FOREIGN KEY (`eventTicketId`) REFERENCES `EventTickets` (`idEventTickets`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TicketLocks`
--

LOCK TABLES `TicketLocks` WRITE;
/*!40000 ALTER TABLE `TicketLocks` DISABLE KEYS */;
/*!40000 ALTER TABLE `TicketLocks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Users`
--

DROP TABLE IF EXISTS `Users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Users` (
  `idUsers` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  `surname` varchar(45) DEFAULT NULL,
  `roleId` int(11) NOT NULL,
  `email` varchar(80) NOT NULL,
  `password` varchar(45) DEFAULT NULL,
  `phone1` varchar(15) DEFAULT NULL,
  `phone2` varchar(15) DEFAULT NULL,
  `selfComments` varchar(45) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `facebook` varchar(45) DEFAULT NULL,
  `twitter` varchar(45) DEFAULT NULL,
  `google` varchar(45) DEFAULT NULL,
  `connectedVia` char(1) NOT NULL,
  `isShipOwner` char(1) NOT NULL DEFAULT 'N',
  `languagesSpoken` varchar(60) DEFAULT NULL,
  `experiences` text,
  `status` char(1) NOT NULL DEFAULT 'D',
  `imageURL` varchar(120) DEFAULT NULL,
  PRIMARY KEY (`idUsers`),
  UNIQUE KEY `idx_Users_Email` (`email`),
  KEY `fk_Role_idx` (`roleId`),
  CONSTRAINT `fk_Users_Role` FOREIGN KEY (`roleId`) REFERENCES `Roles` (`idRoles`) ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Users`
--

LOCK TABLES `Users` WRITE;
/*!40000 ALTER TABLE `Users` DISABLE KEYS */;
INSERT INTO `Users` VALUES (55,NULL,NULL,1,'osvaldo.lucchini@gmail.com','lamiapassword',NULL,NULL,NULL,0,NULL,NULL,NULL,'P','F',NULL,NULL,'A',NULL);
/*!40000 ALTER TABLE `Users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `UsersAuth`
--

DROP TABLE IF EXISTS `UsersAuth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UsersAuth` (
  `idUsersAuth` int(11) NOT NULL AUTO_INCREMENT,
  `userId` int(11) NOT NULL,
  `created` datetime NOT NULL,
  `lastRefreshed` datetime NOT NULL,
  `token` varchar(45) NOT NULL,
  PRIMARY KEY (`idUsersAuth`),
  KEY `fk_UsersAuth_Users_idx` (`userId`),
  CONSTRAINT `fk_UsersAuth_Users` FOREIGN KEY (`userId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UsersAuth`
--

LOCK TABLES `UsersAuth` WRITE;
/*!40000 ALTER TABLE `UsersAuth` DISABLE KEYS */;
/*!40000 ALTER TABLE `UsersAuth` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-04-06 19:24:39
