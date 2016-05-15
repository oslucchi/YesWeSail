CREATE DATABASE  IF NOT EXISTS `yeswesail` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `yeswesail`;
-- MySQL dump 10.13  Distrib 5.5.47, for debian-linux-gnu (x86_64)
--
-- Host: 127.0.0.1    Database: yeswesail
-- ------------------------------------------------------
-- Server version	5.5.47-0ubuntu0.14.04.1

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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Boats`
--

LOCK TABLES `Boats` WRITE;
/*!40000 ALTER TABLE `Boats` DISABLE KEYS */;
INSERT INTO `Boats` VALUES (1,1,'N','N/A','N/A','N/A',0,0,0,0,0,0),(2,74,'V','IT10000','myboat','odissey44',44,2009,2,2,1,1);
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
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
  `languageId` int(11) NOT NULL,
  `eventId` int(11) NOT NULL,
  `anchorZone` tinyint(4) NOT NULL,
  `description` text NOT NULL,
  PRIMARY KEY (`idEventDescription`),
  KEY `fk_EventDescriptaion_Languges_idx` (`languageId`),
  KEY `fk_EventDescription_Event_idx` (`eventId`),
  CONSTRAINT `fk_EventDescriptaion_Languges` FOREIGN KEY (`languageId`) REFERENCES `Languages` (`idLanguages`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_EventDescription_Event` FOREIGN KEY (`eventId`) REFERENCES `Events` (`idEvents`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `EventDescription`
--

LOCK TABLES `EventDescription` WRITE;
/*!40000 ALTER TABLE `EventDescription` DISABLE KEYS */;
INSERT INTO `EventDescription` VALUES (1,1,2,0,'25 Aprile, dolce far nulla'),(2,1,1,0,'QUESTA E\' LA DESCRIZIONE DELL\'EVENTO 2'),(3,1,3,0,'Ponte del 25 aprile tra il Golfo dei Poeti e il Tigullio'),(4,1,4,0,'Pesca al bolentino'),(5,1,5,0,'A zonzo per il Mar Ionio'),(6,1,5,1,'<H2>Crociera a zonzo per il Mar Ionio</H2><p>Scopri cosa significa essere un turista del mare, viaggiare verso nuovi porti e nuove destinazioni, godere di panorami mozzafiato, incontrare popoli e culture diverse, rilassarsi, condividere del tempo insieme, mettersi in discussione e a confronto!<br>Perché nel Mar Ionio? Prima di tutto perché è affascinante, limpido, di un blu cobalto, salatissimo, ricco di fauna e flora marina…ti sorprenderà e ne sarai sedotto. Inoltre è ricco di storia…un po’ tutti ci hanno navigato: Magna Grecia, Bizantini, Romani, Longobardi, Serenissimi, Turchi, Egiziani e fin dall’antichità è stato crocevia di intrecci commerciali da e per l’oriente. E’ uno spettacolo e te ne innamorerai!<br><br>A bordo del Moana60 vivrai un’esperienza genuina al naturale, sperimentando l’antico viaggiar andando per mare, apprenderai le poche, fondamentali regole del mare, assaporando l’emozione di approdare in memorabili città e luoghi, carichi di storia e tradizioni.<br><br>Nicola, il tuo skipper, sarà a disposizione per illustrare, chiarire, approfondire e confrontarsi (non si finisce mai di imparare) con l’equipaggio più curioso…. Dal semplice nodo parlato (i parabordi), alle diverse tecniche e usi del sestante (strumento prezioso al pari di una stella) utilizzato ancor oggi per la determinazione del punto nave. Betelgeuse, Bellatrix, Orione, Cassiopea e la Polare diventeranno astri e costellazioni fedeli che vi accompagneranno non solo in mare, ma anche durante un’escursione notturna in alta montagna o durante una cena romantica con la vostra amata/o a lume di candela. Potremmo ammirare albe, tramonti e costellazioni che credevamo scomparse dai cieli, con un ritmo sottile e leggero della vita che scorre al meglio.<br><br>Navigheremo verso tranquille e verdi baie dove pranzeremo tra bagni di sole e di mare.'),(7,1,5,2,'Il sabato imbarco a Sami, Cefalonia e sistemazione a bordo. In alternativa… possibilità d’imbarco il giovedì al porto di S. Nikolas, Zante e sbarco il giovedì successivo.<br>Ogni mattina, dopo una buona colazione ed un tuffo per risvegliarci, si mollano gli ormeggi e si parte verso una nuova destinazione.<br>L’itinerario prevede: Fiskardo (Cefalonia), Khioni (Itaca), una notte in baia a sud di Itaca, Agios Nikolaos (Zante), escursione alla famosissima Spiaggia del Relitto a Zante, Sami (Cefalonia). Il sabato sbarco a Sami, Cefalonia.<br>Tra una destinazione e l’altra, tanta vela, divertimento e relax con soste in baie per tuffi in acque cristalline.'),(8,1,5,3,'<ul><li>Aperitivo di benvenuto</li><li>Tasse portuali</li><li>Carburante</li></ul>'),(9,1,5,4,'<ul><li>Cambusa</li><li>Trasferimenti da e verso il porto di imbarco</li></ul>'),(10,1,2,1,'<H2>Una vacanza di tutto relax!</H2>La nostra vacanza a bordo di Miolì, l’abbiamo pensata per il puro relax.<br>\nDedicarsi tre giorni in barca a vela alla scoperta delle bellezze del mare dell’Isola d’Elba è un buon modo per inaugurare l’arrivo della primavera! Scivoleremo fino all’isola ad ammirare le sue coste caratterizzate da borghi di pescatori, paesini arroccati, antichi castelli, verdi vallate, incantevoli golfi e splendide spiagge di sabbia e ghiaia dove, la notte, daremo fondo all’ancora per la cena con del buon vino e buona musica.<br>Se le condizioni meteo lo permettono potremo dirigerci verso Capraia che brulla e selvaggia sorge su terreno tufico, originata dal bordo dell’antico vulcano sprofondato nel mare.<br>A terra sarà  possibile visitare il piccolo borgo e l’ormai abbandonato penitenziario incorniciato dal giallo delle ginestre e il blu del mare. Oppure potremo dirigerci a sud verso Pianosa e l’Isola di Montecristo, perla del nostro arcipelago per i miti e le leggende che la raccontano, i panorami mozzafiato e il suo parco naturale.'),(11,1,2,2,'Imbarco e sbarco da Marina di Cala de’ Medici (LI), partenza il 22 aprile, ritorno il 25 aprile.<br>L’imbarcazione ospita fino a un massimo di 8 persone. /La vacanza è confermata con un MINIMO DI 4 PARTECIPANTI.<br>E’ possibile prenotare l’intera barca (contatta l’armatore)<br>Barca e programma possono subire variazioni, senza preavviso, in base alle condizioni meteo-climatiche per garantire la sicurezza a bordo<br>Verifica l’itinerario della vacanza con il tuo skipper prima di prenotare le escursioni a terra.'),(12,1,2,3,'<ul><li>imbarcazione Bénéteau First 45f5 con 4 cabine e 2 bagni</li><li>skipper (anche istruttore di vela, potrà insegnarvi i rudimenti dell’andar per mare)</li><li>pernottamento in cuccetta singola o doppia matrimoniale</li><li>gasolio</li><li>assicurazione</li><li>attrezzatura da pesca e tender (gommone)</li></ul>'),(13,1,2,4,'<ul><li>la cambusa (i pasti) che sarà acquistata e preparata con il contributo di tutto l’equipaggio. In base alle richieste è possibile pranzare o cenare a bordo oppure attraccare e scegliere di apprezzare la cucina locale al ristorante.</li><li>la sosta nei porti differenti a quello di imbarco che sarà a carico dell’equipaggio.</li><li>Le spese extra, che ammontano a circa 15 euro a persona al giorno.</li></ul>'),(22,1,18,0,'questo e\' il titolo'),(23,1,18,1,'<H2>descrizione del mio primo evento (diretto)</H2><p>E questo e\' il testo</p>'),(24,1,18,2,'Corsica'),(25,1,18,3,'<ul><li>questo</li><li>quello</li></ul><br>diretto'),(26,1,18,4,'<ul><li>un terzo e</li><li>un quarto</li></ul><br>diretto'),(27,1,29,0,'Il titolo dell\'evento di Stefan'),(29,2,5,1,'<p>This is a description</p>'),(30,2,5,2,'this is the event logisitic'),(31,2,5,3,'<p>This is patrick</p>'),(32,2,5,4,''),(33,2,2,1,'<p>Hello</p>'),(34,2,2,2,'<p>I am</p>'),(35,2,2,3,'<p>in english</p>'),(36,2,2,4,''),(38,1,30,0,'Mio Evento'),(39,1,30,1,'<p>Descrizione</p>'),(40,1,30,2,'<p>Logistica</p>'),(41,1,30,3,'<p>Include</p>'),(42,1,30,4,'<p>Esclude</p>');
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
  `ticketType` tinyint(4) NOT NULL,
  `available` int(11) NOT NULL,
  `booked` int(11) NOT NULL,
  `price` int(11) NOT NULL,
  `cabinRef` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`idEventTickets`),
  KEY `fk_EventTickets_Events_idx` (`eventId`),
  CONSTRAINT `fk_EventTickets_Events` FOREIGN KEY (`eventId`) REFERENCES `Events` (`idEvents`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `EventTickets`
--

LOCK TABLES `EventTickets` WRITE;
/*!40000 ALTER TABLE `EventTickets` DISABLE KEYS */;
INSERT INTO `EventTickets` VALUES (1,1,1,1,1,80,NULL),(2,1,1,1,0,50,NULL),(3,2,2,2,2,120,NULL),(4,2,2,4,2,70,NULL),(5,1,1,1,0,70,NULL),(6,3,1,1,0,63,0),(7,3,2,2,0,80,0),(8,4,1,2,1,73,0),(9,4,2,1,0,90,NULL),(10,5,1,4,2,400,0),(11,5,2,1,0,450,1),(12,5,2,1,0,480,1);
/*!40000 ALTER TABLE `EventTickets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `EventTicketsDescription`
--

DROP TABLE IF EXISTS `EventTicketsDescription`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventTicketsDescription` (
  `idEventTicketsDescription` int(11) NOT NULL,
  `languageId` int(11) NOT NULL,
  `ticketType` int(11) NOT NULL,
  `description` varchar(128) NOT NULL,
  PRIMARY KEY (`idEventTicketsDescription`),
  KEY `fk_EventTicketsDescription_Languages_idx` (`languageId`),
  KEY `fk_EventTicketsDescription_EventTickets_idx` (`ticketType`),
  CONSTRAINT `fk_EventTicketsDescription_EventTickets` FOREIGN KEY (`ticketType`) REFERENCES `EventTickets` (`idEventTickets`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_EventTicketsDescription_Languages` FOREIGN KEY (`languageId`) REFERENCES `Languages` (`idLanguages`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `EventTicketsDescription`
--

LOCK TABLES `EventTicketsDescription` WRITE;
/*!40000 ALTER TABLE `EventTicketsDescription` DISABLE KEYS */;
INSERT INTO `EventTicketsDescription` VALUES (1,1,1,'Posto in cuccetta - bagno in comune'),(2,1,2,'Posto in cabina con bagno'),(3,1,3,'Posto in cuccetta - bagno in comune'),(4,1,4,'Cabina armatoriale con bagno e self godeur');
/*!40000 ALTER TABLE `EventTicketsDescription` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `EventTicketsSold`
--

DROP TABLE IF EXISTS `EventTicketsSold`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EventTicketsSold` (
  `idEventTicketsSold` int(11) NOT NULL,
  `eventTicketId` int(11) NOT NULL,
  `userId` int(11) NOT NULL,
  PRIMARY KEY (`idEventTicketsSold`),
  KEY `fk_EventTicketsSold_Events_idx` (`eventTicketId`),
  KEY `fk_EventTicketsSold_Users_idx` (`userId`),
  CONSTRAINT `fk_EventTicketsSold_EventsTickets` FOREIGN KEY (`eventTicketId`) REFERENCES `EventTickets` (`idEventTickets`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_EventTicketsSold_Users` FOREIGN KEY (`userId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `EventTicketsSold`
--

LOCK TABLES `EventTicketsSold` WRITE;
/*!40000 ALTER TABLE `EventTicketsSold` DISABLE KEYS */;
INSERT INTO `EventTicketsSold` VALUES (1,10,91),(2,10,94);
/*!40000 ALTER TABLE `EventTicketsSold` ENABLE KEYS */;
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
  `description` varchar(45) NOT NULL,
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
INSERT INTO `EventTypes` VALUES (1,1,'Esperienza mare'),(2,1,'Esperienza a terra'),(3,2,'Sailing'),(4,2,'Experience on the ground');
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
  `status` char(1) NOT NULL,
  `earlyBooking` char(1) DEFAULT NULL,
  `lastMinute` char(1) DEFAULT NULL,
  `eventRef` varchar(15) DEFAULT NULL,
  `aggregateKey` varchar(15) DEFAULT NULL,
  `createdBy` int(11) NOT NULL,
  `createdOn` datetime NOT NULL,
  PRIMARY KEY (`idEvents`),
  KEY `fk_Users_idx` (`shipownerId`),
  KEY `fk_Events_Boats_idx` (`shipId`),
  KEY `fk_Events_Eventtype_idx` (`eventType`),
  KEY `fk_Events_Categories_idx` (`categoryId`),
  KEY `fk_Events_Users_create_idx` (`createdBy`),
  CONSTRAINT `fk_Events_Users_create` FOREIGN KEY (`createdBy`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Events_Boats` FOREIGN KEY (`shipId`) REFERENCES `Boats` (`idBoats`) ON DELETE NO ACTION,
  CONSTRAINT `fk_Events_Categories` FOREIGN KEY (`categoryId`) REFERENCES `Categories` (`idCategories`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Events_Eventtype` FOREIGN KEY (`eventType`) REFERENCES `EventTypes` (`idEventTypes`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Events_Users` FOREIGN KEY (`shipownerId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Events`
--

LOCK TABLES `Events` WRITE;
/*!40000 ALTER TABLE `Events` DISABLE KEYS */;
INSERT INTO `Events` VALUES (1,1,'2016-04-25 00:00:00','2016-12-27 00:00:00','toscana',1,'http://yeswesail.ddns.net:8080/YesWeSail/images/events/ev_1_0.jpg',74,2,NULL,'A',NULL,NULL,NULL,NULL,1,'2016-05-14 23:12:11'),(2,1,'2016-04-15 00:00:00','2016-12-17 00:00:00','LIGURIA',1,'http://yeswesail.ddns.net:8080/YesWeSail/images/events/ev_2_0.jpg',74,2,NULL,'P','N','N',NULL,NULL,1,'2016-05-14 23:12:11'),(3,1,'2016-05-01 00:00:00','2016-12-15 00:00:00','Maremma',1,'http://yeswesail.ddns.net:8080/YesWeSail/images/events/ev_3_0.jpg',74,2,'','A',NULL,NULL,NULL,NULL,1,'2016-05-14 23:12:11'),(4,1,'2016-05-05 00:00:00','2016-12-15 00:00:00','Elba',2,'http://yeswesail.ddns.net:8080/YesWeSail/images/events/ev_4_0.jpg',74,2,'','A',NULL,NULL,NULL,NULL,1,'2016-05-14 23:12:11'),(5,1,'2016-04-23 00:00:00','2016-12-23 00:00:00','Mar Ionio',1,'http://www.placehold.it/1920x400?text=Here goes your event image',74,2,'','P','N','N',NULL,NULL,1,'2016-05-14 23:12:11'),(18,1,'1970-01-01 00:00:00','1970-01-01 00:00:00','Corsica',1,'http://www.placehold.it/1920x400?text=Here goes your event image',74,1,NULL,'P','N','N',NULL,NULL,1,'2016-05-14 23:12:11'),(21,1,'1970-01-01 00:00:00','1970-01-01 00:00:00','TBD',1,'http://www.placehold.it/1920x400?text=Here goes your event image',1,1,NULL,'P','N','N',NULL,NULL,1,'2016-05-14 23:12:11'),(22,1,'1970-01-01 00:00:00','1970-01-01 00:00:00','TBD',1,'http://www.placehold.it/1920x400?text=Here goes your event image',1,1,NULL,'P','N','N',NULL,NULL,1,'2016-05-14 23:12:11'),(23,1,'1970-01-01 00:00:00','1970-01-01 00:00:00','TBD',1,'http://www.placehold.it/1920x400?text=Here goes your event image',1,1,NULL,'P','N','N',NULL,NULL,1,'2016-05-14 23:12:11'),(24,1,'1970-01-01 00:00:00','1970-01-01 00:00:00','TBD',1,'http://www.placehold.it/1920x400?text=Here goes your event image',1,1,NULL,'P','N','N',NULL,NULL,1,'2016-05-14 23:12:11'),(25,1,'1970-01-01 00:00:00','1970-01-01 00:00:00','TBD',1,'http://www.placehold.it/1920x400?text=Here goes your event image',1,1,NULL,'P','N','N',NULL,NULL,1,'2016-05-14 23:12:11'),(26,2,'1970-01-01 00:00:00','1970-01-01 00:00:00','TBD',1,'http://www.placehold.it/1920x400?text=Here goes your event image',1,1,NULL,'P','N','N',NULL,NULL,1,'2016-05-14 23:12:11'),(28,1,'1970-01-01 00:00:00','1970-01-01 00:00:00','TBD',1,'http://www.placehold.it/1920x400?text=Here goes your event image',74,1,NULL,'P','N','N',NULL,NULL,1,'2016-05-14 23:12:11'),(29,1,'1970-01-01 00:00:00','1970-01-01 00:00:00','TBD',1,'http://www.placehold.it/1920x400?text=Here goes your event image',95,1,NULL,'P','N','N',NULL,NULL,1,'2016-05-14 23:12:11'),(30,2,'1970-01-01 00:00:00','1970-01-01 00:00:00','Mar Ionio',1,'http://www.placehold.it/1920x600?text=Here goes your event image',95,1,NULL,'P','N','N',NULL,NULL,74,'2016-05-15 22:19:52');
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
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RegistrationConfirm`
--

LOCK TABLES `RegistrationConfirm` WRITE;
/*!40000 ALTER TABLE `RegistrationConfirm` DISABLE KEYS */;
INSERT INTO `RegistrationConfirm` VALUES (2,'2016-04-24 13:37:06','07a08b84-7136-43f7-bb4c-6d3c4d883ca8',94,'I');
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
  `reservedTo` varchar(128) DEFAULT NULL,
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
  `email` varchar(128) NOT NULL,
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
  `imageURL` varchar(512) DEFAULT NULL,
  `birthday` date DEFAULT NULL,
  PRIMARY KEY (`idUsers`),
  UNIQUE KEY `idx_Users_Email` (`email`),
  KEY `fk_Role_idx` (`roleId`),
  CONSTRAINT `fk_Users_Role` FOREIGN KEY (`roleId`) REFERENCES `Roles` (`idRoles`) ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=103 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Users`
--

LOCK TABLES `Users` WRITE;
/*!40000 ALTER TABLE `Users` DISABLE KEYS */;
INSERT INTO `Users` VALUES (1,'Fake','Fake',1,'cantaccess@nodomain.com','','','','',0,'','','','','','','','D','','0000-00-00'),(74,'Osvaldo','Lucchini',2,'osvaldo.lucchini@gmail.com','12345678',NULL,NULL,NULL,50,'10208802349917933',NULL,NULL,'F','F',NULL,NULL,'D','https://scontent-mxp1-1.xx.fbcdn.net/v/t1.0-1/c0.41.153.153/1897878_10203059351146553_1428969181_n.jpg?oh=631c448bf10972dd6feddc9b68d55f6a&oe=579B2AAB','1964-12-24'),(91,'Stefan','Amarie',1,'amarie.stefan@gmail.com','test1234',NULL,NULL,NULL,0,'10209676010000980',NULL,NULL,'F','F',NULL,NULL,'A','https://scontent.xx.fbcdn.net/hprofile-xpa1/v/t1.0-1/c0.6.50.50/p50x50/14587_10200353390621322_1989525648_n.jpg?oh=70c7ed4ecbf677c2ee2db3645e6f7928&oe=579F9B81','1991-12-25'),(94,'Stefan','Amarie',1,'s.amarie@itsoftware.it','test1234',NULL,NULL,NULL,0,NULL,NULL,NULL,'P','F',NULL,NULL,'A',NULL,NULL),(95,'Jacopo','Gazzola',2,'flutejb@gmail.com',NULL,NULL,NULL,NULL,0,'10154319055151159',NULL,NULL,'F','F',NULL,NULL,'D','https://scontent.xx.fbcdn.net/hprofile-xla1/v/t1.0-1/p50x50/12963374_10154271812561159_4618465219709942847_n.jpg?oh=35c25aa08fea0da67c048870112b2a01&oe=57B3588A',NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UsersAuth`
--

LOCK TABLES `UsersAuth` WRITE;
/*!40000 ALTER TABLE `UsersAuth` DISABLE KEYS */;
INSERT INTO `UsersAuth` VALUES (36,94,'2016-04-24 15:39:35','2016-04-24 16:00:13','14f61b2a-b74d-49b9-9c70-d709aff88411'),(37,95,'2016-04-25 22:51:48','2016-04-25 22:51:48','c4d5a5a6-c562-45ab-bf59-68a10cf47539'),(60,74,'2016-05-15 22:19:00','2016-05-15 22:19:00','f972e2e9-6eab-4186-a4dd-51f5e367d0da');
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

-- Dump completed on 2016-05-15 22:41:39
