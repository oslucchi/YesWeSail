CREATE DATABASE  IF NOT EXISTS `yeswesail` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `yeswesail`;
-- MySQL dump 10.13  Distrib 5.5.50, for debian-linux-gnu (x86_64)
--
-- Host: 188.213.171.213    Database: yeswesail
-- ------------------------------------------------------
-- Server version	5.7.13

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
  `taxCode` varchar(16) DEFAULT NULL,
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
  `insurance` varchar(45) DEFAULT NULL,
  `securityCertification` varchar(45) DEFAULT NULL,
  `RTFLicense` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idBoats`),
  KEY `fk_Users_idx` (`ownerId`),
  CONSTRAINT `fk_Boats_Users` FOREIGN KEY (`ownerId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Categories`
--

DROP TABLE IF EXISTS `Categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Categories` (
  `idCategories` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `url` varchar(56) NOT NULL,
  PRIMARY KEY (`idCategories`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
-- Table structure for table `DocumentTypes`
--

DROP TABLE IF EXISTS `DocumentTypes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DocumentTypes` (
  `idDocumentTypes` int(11) NOT NULL AUTO_INCREMENT,
  `languageId` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`idDocumentTypes`),
  KEY `fk_DocumentTypes_Languages_idx` (`languageId`),
  CONSTRAINT `fk_DocumentTypes_Languages` FOREIGN KEY (`languageId`) REFERENCES `Languages` (`idLanguages`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Documents`
--

DROP TABLE IF EXISTS `Documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Documents` (
  `idDocuments` int(11) NOT NULL AUTO_INCREMENT,
  `userId` int(11) NOT NULL,
  `documentTypesId` int(11) NOT NULL,
  `number` varchar(45) NOT NULL,
  PRIMARY KEY (`idDocuments`),
  KEY `fk_Documents_Users_idx` (`userId`),
  KEY `fk_Documents_DocumentTYpes_idx` (`documentTypesId`),
  CONSTRAINT `fk_Documents_DocumentTYpes` FOREIGN KEY (`documentTypesId`) REFERENCES `DocumentTypes` (`idDocumentTypes`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Documents_Users` FOREIGN KEY (`userId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DynamicPages`
--

DROP TABLE IF EXISTS `DynamicPages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DynamicPages` (
  `idDynamicPages` int(11) NOT NULL AUTO_INCREMENT,
  `URLReference` varchar(45) NOT NULL,
  `createdOn` datetime NOT NULL,
  `status` char(1) NOT NULL,
  `innerHTML` blob NOT NULL,
  `languageId` tinyint(4) NOT NULL,
  PRIMARY KEY (`idDynamicPages`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventDescription`
--

DROP TABLE IF EXISTS `EventDescription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventDescription` (
  `idEventDescription` int(11) NOT NULL AUTO_INCREMENT,
  `languageId` int(11) NOT NULL,
  `eventId` int(11) NOT NULL,
  `anchorZone` tinyint(4) NOT NULL,
  `description` text NOT NULL,
  PRIMARY KEY (`idEventDescription`),
  KEY `fk_EventDescriptaion_Languges_idx` (`languageId`),
  KEY `fk_EventDescription_Event_idx` (`eventId`),
  CONSTRAINT `fk_EventDescriptaion_Languges` FOREIGN KEY (`languageId`) REFERENCES `Languages` (`idLanguages`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_EventDescription_Event` FOREIGN KEY (`eventId`) REFERENCES `Events` (`idEvents`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1318 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventRoute`
--

DROP TABLE IF EXISTS `EventRoute`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventRoute` (
  `idEventRoute` int(11) NOT NULL AUTO_INCREMENT,
  `eventId` int(11) NOT NULL,
  `lat` varchar(24) NOT NULL,
  `lng` varchar(24) NOT NULL,
  `description` varchar(512) DEFAULT NULL,
  `seq` smallint(6) NOT NULL,
  PRIMARY KEY (`idEventRoute`),
  KEY `fk_EventRoute_Events_idx` (`eventId`),
  CONSTRAINT `fk_EventRoute_Events` FOREIGN KEY (`eventId`) REFERENCES `Events` (`idEvents`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=387 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventTickets`
--

DROP TABLE IF EXISTS `EventTickets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventTickets` (
  `idEventTickets` int(11) NOT NULL AUTO_INCREMENT,
  `eventId` int(11) NOT NULL,
  `ticketType` tinyint(4) NOT NULL,
  `available` int(11) NOT NULL,
  `booked` int(11) NOT NULL,
  `price` int(11) NOT NULL,
  `cabinRef` tinyint(4) DEFAULT NULL,
  `bookedTo` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`idEventTickets`),
  KEY `fk_EventTickets_Events_idx` (`eventId`),
  KEY `fk_EventTickets_TicketTypes_idx` (`ticketType`),
  CONSTRAINT `fk_EventTickets_Events` FOREIGN KEY (`eventId`) REFERENCES `Events` (`idEvents`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_EventTickets_TicketTypes` FOREIGN KEY (`ticketType`) REFERENCES `TicketTypes` (`idTicketTypes`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=299 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventTicketsDescription`
--

DROP TABLE IF EXISTS `EventTicketsDescription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventTicketsDescription` (
  `idEventTicketsDescription` int(11) NOT NULL,
  `languageId` int(11) NOT NULL,
  `ticketType` tinyint(4) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY (`idEventTicketsDescription`),
  KEY `fk_EventTicketsDescription_Languages_idx` (`languageId`),
  KEY `fk_EventTicketsDescription_TicketTypes_idx` (`ticketType`),
  CONSTRAINT `fk_EventTicketsDescription_Languages` FOREIGN KEY (`languageId`) REFERENCES `Languages` (`idLanguages`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_EventTicketsDescription_TicketTypes` FOREIGN KEY (`ticketType`) REFERENCES `TicketTypes` (`idTicketTypes`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventTicketsSold`
--

DROP TABLE IF EXISTS `EventTicketsSold`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventTicketsSold` (
  `idEventTicketsSold` int(11) NOT NULL AUTO_INCREMENT,
  `eventTicketId` int(11) NOT NULL,
  `userId` int(11) NOT NULL,
  `transactionId` varchar(96) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`idEventTicketsSold`),
  KEY `fk_EventTicketsSold_Events_idx` (`eventTicketId`),
  KEY `fk_EventTicketsSold_Users_idx` (`userId`),
  CONSTRAINT `fk_EventTicketsSold_EventsTickets` FOREIGN KEY (`eventTicketId`) REFERENCES `EventTickets` (`idEventTickets`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_EventTicketsSold_Users` FOREIGN KEY (`userId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventTypes`
--

DROP TABLE IF EXISTS `EventTypes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventTypes` (
  `idEventTypes` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(45) NOT NULL,
  `languageId` tinyint(4) NOT NULL,
  PRIMARY KEY (`idEventTypes`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `EventTypesLanguages`
--

DROP TABLE IF EXISTS `EventTypesLanguages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventTypesLanguages` (
  `idEventTypesLanguages` int(11) NOT NULL AUTO_INCREMENT,
  `eventTypeId` int(11) NOT NULL,
  `languageId` int(11) NOT NULL,
  `description` varchar(45) NOT NULL,
  PRIMARY KEY (`idEventTypesLanguages`),
  KEY `fk_EventTypesLanguages_EventTYpes_idx` (`eventTypeId`),
  CONSTRAINT `fk_EventTypesLanguages_EventTYpes` FOREIGN KEY (`eventTypeId`) REFERENCES `EventTypes` (`idEventTypes`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

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
  `imageURL` varchar(256) DEFAULT NULL,
  `shipOwnerId` int(11) NOT NULL,
  `boatId` int(11) NOT NULL,
  `labels` varchar(90) DEFAULT NULL,
  `status` char(1) NOT NULL,
  `earlyBooking` tinyint(4) DEFAULT NULL,
  `lastMinute` tinyint(4) DEFAULT NULL,
  `hotEvent` tinyint(4) DEFAULT NULL,
  `eventRef` varchar(16) DEFAULT NULL,
  `aggregateKey` varchar(64) DEFAULT NULL,
  `createdBy` int(11) NOT NULL,
  `createdOn` datetime NOT NULL,
  PRIMARY KEY (`idEvents`),
  KEY `fk_Users_idx` (`shipOwnerId`),
  KEY `fk_Events_Boats_idx` (`boatId`),
  KEY `fk_Events_Eventtype_idx` (`eventType`),
  KEY `fk_Events_Categories_idx` (`categoryId`),
  KEY `fk_Events_Users_create_idx` (`createdBy`),
  CONSTRAINT `fk_Events_Boats` FOREIGN KEY (`boatId`) REFERENCES `Boats` (`idBoats`) ON DELETE NO ACTION,
  CONSTRAINT `fk_Events_Categories` FOREIGN KEY (`categoryId`) REFERENCES `Categories` (`idCategories`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Events_Eventtype` FOREIGN KEY (`eventType`) REFERENCES `EventTypes` (`idEventTypes`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Events_Users` FOREIGN KEY (`shipOwnerId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Events_Users_create` FOREIGN KEY (`createdBy`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
-- Table structure for table `PendingActions`
--

DROP TABLE IF EXISTS `PendingActions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PendingActions` (
  `idPendingActions` int(11) NOT NULL AUTO_INCREMENT,
  `actionType` varchar(512) NOT NULL,
  `userId` int(11) NOT NULL,
  `link` varchar(256) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` char(1) NOT NULL,
  `updated` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`idPendingActions`),
  KEY `fk_PendingActions_Users_idx` (`userId`),
  CONSTRAINT `fk_PendingActions_Users` FOREIGN KEY (`userId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

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
  `passwordChange` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idRegistrationConfirm`),
  KEY `fk_RegistrationConfirm_Users_idx` (`userId`),
  CONSTRAINT `fk_RegistrationConfirm_Users` FOREIGN KEY (`userId`) REFERENCES `Users` (`idUsers`) ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Reviews`
--

DROP TABLE IF EXISTS `Reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Reviews` (
  `idReviews` int(11) NOT NULL AUTO_INCREMENT,
  `review` text NOT NULL,
  `reviewerId` int(11) NOT NULL,
  `reviewForId` int(11) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `updated` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `rating` tinyint(4) NOT NULL,
  `status` char(1) NOT NULL,
  PRIMARY KEY (`idReviews`),
  KEY `fk_Reviews_Users_idx` (`reviewerId`),
  KEY `fk_Reviews_Users_idx1` (`reviewForId`),
  CONSTRAINT `fk_Reviews_Users_1` FOREIGN KEY (`reviewerId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Reviews_Users_2` FOREIGN KEY (`reviewForId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
-- Table structure for table `TicketLocks`
--

DROP TABLE IF EXISTS `TicketLocks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TicketLocks` (
  `idTicketLocks` int(11) NOT NULL AUTO_INCREMENT,
  `eventTicketId` int(11) NOT NULL,
  `lockTime` datetime NOT NULL,
  `userId` int(11) NOT NULL,
  `reservedTo` varchar(128) DEFAULT NULL,
  `status` char(1) NOT NULL DEFAULT 'P',
  PRIMARY KEY (`idTicketLocks`),
  KEY `fk_TicketLocks_EventTicket_idx` (`eventTicketId`),
  KEY `fk_TicketLocks_Users_idx` (`userId`),
  CONSTRAINT `fk_TicketLocks_EventTicket` FOREIGN KEY (`eventTicketId`) REFERENCES `EventTickets` (`idEventTickets`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_TicketLocks_Users` FOREIGN KEY (`userId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `TicketTypes`
--

DROP TABLE IF EXISTS `TicketTypes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TicketTypes` (
  `idTicketTypes` tinyint(4) NOT NULL,
  `localDescription` varchar(45) NOT NULL,
  PRIMARY KEY (`idTicketTypes`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

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
  `email` varchar(128) NOT NULL,
  `password` varchar(45) DEFAULT NULL,
  `phone1` varchar(15) DEFAULT NULL,
  `phone2` varchar(15) DEFAULT NULL,
  `about` text,
  `age` int(11) DEFAULT NULL,
  `facebook` varchar(45) DEFAULT NULL,
  `twitter` varchar(45) DEFAULT NULL,
  `google` varchar(45) DEFAULT NULL,
  `connectedVia` char(1) NOT NULL,
  `languagesSpoken` varchar(60) DEFAULT NULL,
  `experiences` text,
  `status` char(1) NOT NULL DEFAULT 'D',
  `imageURL` varchar(512) DEFAULT NULL,
  `birthday` date DEFAULT NULL,
  `isShipOwner` tinyint(4) NOT NULL,
  `sailingLicense` varchar(45) DEFAULT NULL,
  `navigationLicense` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idUsers`),
  UNIQUE KEY `idx_Users_Email` (`email`),
  KEY `fk_Role_idx` (`roleId`),
  CONSTRAINT `fk_Users_Role` FOREIGN KEY (`roleId`) REFERENCES `Roles` (`idRoles`) ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=304 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
) ENGINE=InnoDB AUTO_INCREMENT=170 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-11-02  7:40:39
