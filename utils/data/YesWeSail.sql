CREATE DATABASE  IF NOT EXISTS `yeswesail` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `yeswesail`;
-- MySQL dump 10.13  Distrib 5.5.49, for debian-linux-gnu (x86_64)
--
-- Host: 127.0.0.1    Database: yeswesail
-- ------------------------------------------------------
-- Server version	5.5.49-0ubuntu0.14.04.1

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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `AddressInfo`
--

LOCK TABLES `AddressInfo` WRITE;
/*!40000 ALTER TABLE `AddressInfo` DISABLE KEYS */;
INSERT INTO `AddressInfo` VALUES (1,2,'D','','LCCSLD64T24D150','via della fornace 7','','Pessano con Bornago','20060','MI','Italy'),(2,2,'I','L-Soft ltd','109006152345','via Negroni 2','int 5 - citofonare giusy','Pessano con Bornago','20060','MI','Italy');
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
  `insurance` varchar(45) DEFAULT NULL,
  `securityCertification` varchar(45) DEFAULT NULL,
  `RTFLicense` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idBoats`),
  KEY `fk_Users_idx` (`ownerId`),
  CONSTRAINT `fk_Boats_Users` FOREIGN KEY (`ownerId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Boats`
--

LOCK TABLES `Boats` WRITE;
/*!40000 ALTER TABLE `Boats` DISABLE KEYS */;
INSERT INTO `Boats` VALUES (1,1,'N','N/A','N/A','N/A',0,0,0,0,0,0,NULL,NULL,NULL),(2,2,'V','IT10000','myboat','odissey44',44,2009,2,2,1,1,NULL,NULL,NULL);
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
  `url` varchar(56) NOT NULL,
  PRIMARY KEY (`idCategories`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Categories`
--

LOCK TABLES `Categories` WRITE;
/*!40000 ALTER TABLE `Categories` DISABLE KEYS */;
INSERT INTO `Categories` VALUES (1,'Relax','images/application/categoryRelax.png'),(2,'Pesca','images/application/categoryFishing.png'),(3,'Sport','images/application/categorySport.png');
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
) ENGINE=InnoDB AUTO_INCREMENT=133 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `EventDescription`
--

LOCK TABLES `EventDescription` WRITE;
/*!40000 ALTER TABLE `EventDescription` DISABLE KEYS */;
INSERT INTO `EventDescription` VALUES (1,1,2,0,'25 Aprile, dolce far nulla'),(2,1,1,0,'Questa è la descrizione dell\'evento 1'),(3,1,3,0,'Ponte del 25 aprile tra il Golfo dei Poeti e il Tigullio'),(4,1,4,0,'Pesca al bolentino'),(10,1,2,1,'<H2>Una vacanza di tutto relax!</H2>La nostra vacanza a bordo di Miolì, l’abbiamo pensata per il puro relax.<br>\nDedicarsi tre giorni in barca a vela alla scoperta delle bellezze del mare dell’Isola d’Elba è un buon modo per inaugurare l’arrivo della primavera! Scivoleremo fino all’isola ad ammirare le sue coste caratterizzate da borghi di pescatori, paesini arroccati, antichi castelli, verdi vallate, incantevoli golfi e splendide spiagge di sabbia e ghiaia dove, la notte, daremo fondo all’ancora per la cena con del buon vino e buona musica.<br>Se le condizioni meteo lo permettono potremo dirigerci verso Capraia che brulla e selvaggia sorge su terreno tufico, originata dal bordo dell’antico vulcano sprofondato nel mare.<br>A terra sarà  possibile visitare il piccolo borgo e l’ormai abbandonato penitenziario incorniciato dal giallo delle ginestre e il blu del mare. Oppure potremo dirigerci a sud verso Pianosa e l’Isola di Montecristo, perla del nostro arcipelago per i miti e le leggende che la raccontano, i panorami mozzafiato e il suo parco naturale.'),(11,1,2,2,'Imbarco e sbarco da Marina di Cala de’ Medici (LI), partenza il 22 aprile, ritorno il 25 aprile.<br>L’imbarcazione ospita fino a un massimo di 8 persone. /La vacanza è confermata con un MINIMO DI 4 PARTECIPANTI.<br>E’ possibile prenotare l’intera barca (contatta l’armatore)<br>Barca e programma possono subire variazioni, senza preavviso, in base alle condizioni meteo-climatiche per garantire la sicurezza a bordo<br>Verifica l’itinerario della vacanza con il tuo skipper prima di prenotare le escursioni a terra.'),(12,1,2,3,'<ul><li>imbarcazione Bénéteau First 45f5 con 4 cabine e 2 bagni</li><li>skipper (anche istruttore di vela, potrà insegnarvi i rudimenti dell’andar per mare)</li><li>pernottamento in cuccetta singola o doppia matrimoniale</li><li>gasolio</li><li>assicurazione</li><li>attrezzatura da pesca e tender (gommone)</li></ul>'),(13,1,2,4,'<ul><li>la cambusa (i pasti) che sarà acquistata e preparata con il contributo di tutto l’equipaggio. In base alle richieste è possibile pranzare o cenare a bordo oppure attraccare e scegliere di apprezzare la cucina locale al ristorante.</li><li>la sosta nei porti differenti a quello di imbarco che sarà a carico dell’equipaggio.</li><li>Le spese extra, che ammontano a circa 15 euro a persona al giorno.</li></ul>'),(29,2,5,1,'<p>This is a description</p>'),(30,2,5,2,'this is the event logisitic'),(31,2,5,3,'<p>This is patrick</p>'),(32,2,5,4,''),(33,2,2,1,'<p>Hello</p>'),(34,2,2,2,'<p>I am</p>'),(35,2,2,3,'<p>in english</p>'),(36,2,2,4,''),(119,2,1,0,'This is the description for the event 1'),(129,1,5,1,'<H2>Crociera a zonzo per il Mar Ionio</H2><p>Scopri cosa significa essere un turista del mare, viaggiare verso nuovi porti e nuove destinazioni, godere di panorami mozzafiato, incontrare popoli e culture diverse, rilassarsi, condividere del tempo insieme, mettersi in discussione e a confronto!<br>Perché nel Mar Ionio? Prima di tutto perché è affascinante, limpido, di un blu cobalto, salatissimo, ricco di fauna e flora marina…ti sorprenderà e ne sarai sedotto. Inoltre è ricco di storia…un po’ tutti ci hanno navigato: Magna Grecia, Bizantini, Romani, Longobardi, Serenissimi, Turchi, Egiziani e fin dall’antichità è stato crocevia di intrecci commerciali da e per l’oriente. E’ uno spettacolo e te ne innamorerai!<br><br>A bordo del Moana60 vivrai un’esperienza genuina al naturale, sperimentando l’antico viaggiar andando per mare, apprenderai le poche, fondamentali regole del mare, assaporando l’emozione di approdare in memorabili città e luoghi, carichi di storia e tradizioni.<br><br>Nicola, il tuo skipper, sarà a disposizione per illustrare, chiarire, approfondire e confrontarsi (non si finisce mai di imparare) con l’equipaggio più curioso…. Dal semplice nodo parlato (i parabordi), alle diverse tecniche e usi del sestante (strumento prezioso al pari di una stella) utilizzato ancor oggi per la determinazione del punto nave. Betelgeuse, Bellatrix, Orione, Cassiopea e la Polare diventeranno astri e costellazioni fedeli che vi accompagneranno non solo in mare, ma anche durante un’escursione notturna in alta montagna o durante una cena romantica con la vostra amata/o a lume di candela. Potremmo ammirare albe, tramonti e costellazioni che credevamo scomparse dai cieli, con un ritmo sottile e leggero della vita che scorre al meglio.<br><br>Navigheremo verso tranquille e verdi baie dove pranzeremo tra bagni di sole e di mare.'),(130,1,5,2,'Il sabato imbarco a Sami, Cefalonia e sistemazione a bordo. In alternativa… possibilità d’imbarco il giovedì al porto di S. Nikolas, Zante e sbarco il giovedì successivo.<br>Ogni mattina, dopo una buona colazione ed un tuffo per risvegliarci, si mollano gli ormeggi e si parte verso una nuova destinazione.<br>L’itinerario prevede: Fiskardo (Cefalonia), Khioni (Itaca), una notte in baia a sud di Itaca, Agios Nikolaos (Zante), escursione alla famosissima Spiaggia del Relitto a Zante, Sami (Cefalonia). Il sabato sbarco a Sami, Cefalonia.<br>Tra una destinazione e l’altra, tanta vela, divertimento e relax con soste in baie per tuffi in acque cristalline.'),(131,1,5,3,'<ul><li>Aperitivo di benvenuto</li><li>Tasse portuali</li><li>Carburante</li></ul>'),(132,1,5,4,'<ul><li>Cambusa</li><li>Trasferimenti da e verso il porto di imbarco</li></ul>');
/*!40000 ALTER TABLE `EventDescription` ENABLE KEYS */;
UNLOCK TABLES;

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
  `description` varchar(128) DEFAULT NULL,
  `seq` smallint(6) NOT NULL,
  PRIMARY KEY (`idEventRoute`),
  KEY `fk_EventRoute_Events_idx` (`eventId`),
  CONSTRAINT `fk_EventRoute_Events` FOREIGN KEY (`eventId`) REFERENCES `Events` (`idEvents`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `EventRoute`
--

LOCK TABLES `EventRoute` WRITE;
/*!40000 ALTER TABLE `EventRoute` DISABLE KEYS */;
INSERT INTO `EventRoute` VALUES (2,2,'44.4241292','8.7975872','Genova Pegli Castelluccio',0),(3,3,'42.7131595','10.9804434','Porto della Maremma - Marina di San Rocco',0),(4,4,'44.4241292','8.7975872','Genova Pegli Castelluccio',0),(7,1,'38.25141480000001','20.64716880000003','No Description',0),(12,5,'38.25141480000001','20.64716880000003','No Description',0);
/*!40000 ALTER TABLE `EventRoute` ENABLE KEYS */;
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
  `bookedTo` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`idEventTickets`),
  KEY `fk_EventTickets_Events_idx` (`eventId`),
  CONSTRAINT `fk_EventTickets_Events` FOREIGN KEY (`eventId`) REFERENCES `Events` (`idEvents`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `EventTickets`
--

LOCK TABLES `EventTickets` WRITE;
/*!40000 ALTER TABLE `EventTickets` DISABLE KEYS */;
INSERT INTO `EventTickets` VALUES (1,1,1,1,1,80,0,NULL),(2,1,1,1,-1,50,0,NULL),(3,2,2,2,0,120,0,NULL),(4,2,1,4,0,70,0,NULL),(5,1,1,1,0,70,0,NULL),(6,3,1,1,0,63,0,NULL),(7,3,2,2,0,80,0,NULL),(8,4,1,2,0,73,0,NULL),(9,4,2,1,0,90,0,NULL),(10,5,1,4,4,400,0,NULL),(11,5,2,1,1,450,1,NULL),(12,5,2,1,1,480,1,NULL),(14,5,2,1,0,520,0,NULL),(15,5,2,1,0,520,0,NULL),(16,5,3,1,0,600,0,NULL),(17,5,3,1,0,600,0,NULL);
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
INSERT INTO `EventTicketsDescription` VALUES (1,1,1,'Cuccetta - bagno in comune'),(2,1,2,'Cabina - bagno in comune'),(3,1,3,'Cabina - con bagno'),(4,1,4,'Cabina armatoriale con bagno e self godeur');
/*!40000 ALTER TABLE `EventTicketsDescription` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `EventTicketsSold`
--

LOCK TABLES `EventTicketsSold` WRITE;
/*!40000 ALTER TABLE `EventTicketsSold` DISABLE KEYS */;
INSERT INTO `EventTicketsSold` VALUES (1,4,3,'jn5tz3','2016-05-29 13:08:05'),(2,4,3,'jn5tz3','2016-05-29 13:08:09'),(3,3,3,'jn5tz3','2016-05-29 13:08:11'),(4,4,3,'jn5tz3','2016-05-29 13:08:13'),(5,3,3,'jn5tz3','2016-05-29 13:08:15'),(6,4,3,'jn5tz3','2016-05-29 13:08:17'),(7,10,3,'jn5tz3','2016-05-29 13:08:19'),(8,11,3,'jn5tz3','2016-05-29 13:08:21'),(9,10,3,'jn5tz3','2016-05-29 13:08:22'),(10,8,3,'jn5tz3','2016-05-29 13:08:24'),(11,11,3,'kd65sx','2016-05-29 14:00:44'),(12,12,3,'kd65sx','2016-05-29 14:00:44'),(13,10,3,'kd65sx','2016-05-29 14:00:44'),(14,14,3,'8wfscc','2016-05-29 14:25:38'),(15,10,3,'8wfscc','2016-05-29 14:25:38'),(16,10,3,'8wfscc','2016-05-29 14:25:38'),(17,10,3,'8wfscc','2016-05-29 14:25:38'),(19,15,3,'g957cc','2016-05-29 14:33:01'),(20,10,3,'78v2sc','2016-05-29 14:36:26'),(21,10,3,'78v2sc','2016-05-29 14:36:26'),(22,10,3,'78v2sc','2016-05-29 14:36:26'),(23,11,3,'78v2sc','2016-05-29 14:36:26'),(24,10,3,'hznknc','2016-05-29 14:43:06'),(25,12,3,'hznknc','2016-05-29 14:43:06');
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
  `imageURL` varchar(256) DEFAULT NULL,
  `shipOwnerId` int(11) NOT NULL,
  `shipId` int(11) NOT NULL,
  `labels` varchar(90) DEFAULT NULL,
  `status` char(1) NOT NULL,
  `earlyBooking` tinyint(4) DEFAULT NULL,
  `lastMinute` tinyint(4) DEFAULT NULL,
  `hotEvent` tinyint(4) DEFAULT NULL,
  `eventRef` varchar(15) DEFAULT NULL,
  `aggregateKey` varchar(64) DEFAULT NULL,
  `createdBy` int(11) NOT NULL,
  `createdOn` datetime NOT NULL,
  PRIMARY KEY (`idEvents`),
  KEY `fk_Users_idx` (`shipOwnerId`),
  KEY `fk_Events_Boats_idx` (`shipId`),
  KEY `fk_Events_Eventtype_idx` (`eventType`),
  KEY `fk_Events_Categories_idx` (`categoryId`),
  KEY `fk_Events_Users_create_idx` (`createdBy`),
  CONSTRAINT `fk_Events_Boats` FOREIGN KEY (`shipId`) REFERENCES `Boats` (`idBoats`) ON DELETE NO ACTION,
  CONSTRAINT `fk_Events_Categories` FOREIGN KEY (`categoryId`) REFERENCES `Categories` (`idCategories`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Events_Eventtype` FOREIGN KEY (`eventType`) REFERENCES `EventTypes` (`idEventTypes`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Events_Users` FOREIGN KEY (`shipOwnerId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_Events_Users_create` FOREIGN KEY (`createdBy`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Events`
--

LOCK TABLES `Events` WRITE;
/*!40000 ALTER TABLE `Events` DISABLE KEYS */;
INSERT INTO `Events` VALUES (1,1,'2016-06-16 00:00:00','2016-06-25 00:00:00','Toscana',1,'http://yeswesail.ddns.net:8080/YesWeSail/images/events/ev_1_0.jpg',2,2,NULL,'P',0,0,1,NULL,'1',1,'2016-05-14 23:12:11'),(2,1,'2016-07-25 00:00:00','2016-12-17 00:00:00','LIGURIA',1,'http://yeswesail.ddns.net:8080/YesWeSail/images/events/ev_2_0.jpg',2,2,NULL,'A',1,0,1,NULL,'e3a79514-f7a4-475d-8174-1fe68e6ea7a7',1,'2016-05-14 23:12:11'),(3,1,'2016-07-25 00:00:00','2016-12-15 00:00:00','Maremma',1,'http://yeswesail.ddns.net:8080/YesWeSail/images/events/ev_3_0.jpg',2,2,NULL,'A',0,1,1,NULL,'1',1,'2016-05-14 23:12:11'),(4,1,'2016-07-25 00:00:00','2016-12-15 00:00:00','Elba',2,'http://yeswesail.ddns.net:8080/YesWeSail/images/events/ev_4_0.jpg',2,2,NULL,'A',0,0,0,NULL,NULL,1,'2016-05-14 23:12:11'),(5,1,'2016-07-25 00:00:00','2016-12-23 00:00:00','Mar Ionio',1,'http://yeswesail.ddns.net:8080/YesWeSail/images/events/ev_1_0.jpg',2,2,NULL,'P',0,0,1,'','1',1,'2016-05-14 23:12:11');
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PendingActions`
--

LOCK TABLES `PendingActions` WRITE;
/*!40000 ALTER TABLE `PendingActions` DISABLE KEYS */;
INSERT INTO `PendingActions` VALUES (1,'review',2,'/rest/reviews/1','2016-01-04 23:00:00','P','0000-00-00 00:00:00');
/*!40000 ALTER TABLE `PendingActions` ENABLE KEYS */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RegistrationConfirm`
--

LOCK TABLES `RegistrationConfirm` WRITE;
/*!40000 ALTER TABLE `RegistrationConfirm` DISABLE KEYS */;
/*!40000 ALTER TABLE `RegistrationConfirm` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Reviews`
--

LOCK TABLES `Reviews` WRITE;
/*!40000 ALTER TABLE `Reviews` DISABLE KEYS */;
INSERT INTO `Reviews` VALUES (1,'Stefan e\' una faccia di culo',2,3,'2016-06-15 07:55:04','2016-06-04 22:00:00',4,'P');
/*!40000 ALTER TABLE `Reviews` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Roles`
--

LOCK TABLES `Roles` WRITE;
/*!40000 ALTER TABLE `Roles` DISABLE KEYS */;
INSERT INTO `Roles` VALUES (1,'Dummy'),(3,'Viaggiatore'),(6,'Armatore'),(9,'Amministratore');
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
INSERT INTO `RolesLanguages` VALUES (1,1,'Dummy'),(1,2,'Dummy'),(3,1,'Viaggiatore'),(3,2,'Traveller'),(6,1,'Armatore'),(6,2,'Ship Owner'),(9,1,'Amministratore'),(9,2,'Admin');
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
  `userId` int(11) NOT NULL,
  `reservedTo` varchar(128) DEFAULT NULL,
  `status` char(1) NOT NULL DEFAULT 'P',
  PRIMARY KEY (`idTicketLocks`),
  KEY `fk_TicketLocks_EventTicket_idx` (`eventTicketId`),
  KEY `fk_TicketLocks_Users_idx` (`userId`),
  CONSTRAINT `fk_TicketLocks_EventTicket` FOREIGN KEY (`eventTicketId`) REFERENCES `EventTickets` (`idEventTickets`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_TicketLocks_Users` FOREIGN KEY (`userId`) REFERENCES `Users` (`idUsers`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=254 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Users`
--

LOCK TABLES `Users` WRITE;
/*!40000 ALTER TABLE `Users` DISABLE KEYS */;
INSERT INTO `Users` VALUES (1,'Fake','Fake',1,'cantaccess@nodomain.com','','','','',0,'','','','','','','D','','0000-00-00',0,NULL,NULL),(2,'Osvaldo','Lucchini',6,'osvaldo.lucchini@gmail.com','12345678',NULL,NULL,NULL,50,'10208802349917933',NULL,NULL,'F',NULL,NULL,'A','https://scontent-mxp1-1.xx.fbcdn.net/v/t1.0-1/c0.41.153.153/1897878_10203059351146553_1428969181_n.jpg?oh=631c448bf10972dd6feddc9b68d55f6a&oe=579B2AAB','1964-12-24',0,NULL,NULL),(3,'Stefan','Amarie',9,'amarie.stefan@gmail.com','test1234',NULL,NULL,NULL,0,'10209676010000980',NULL,NULL,'F',NULL,NULL,'A','https://scontent.xx.fbcdn.net/hprofile-xpa1/v/t1.0-1/c0.6.50.50/p50x50/14587_10200353390621322_1989525648_n.jpg?oh=70c7ed4ecbf677c2ee2db3645e6f7928&oe=579F9B81','1991-12-25',1,NULL,NULL),(4,'Stefan','Amarie',6,'s.amarie@itsoftware.it','test1234',NULL,NULL,NULL,0,NULL,NULL,NULL,'P',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(5,'Jacopo','Gazzola',6,'flutejb@gmail.com',NULL,NULL,NULL,NULL,0,'10154319055151159',NULL,NULL,'F',NULL,NULL,'D','https://scontent.xx.fbcdn.net/hprofile-xla1/v/t1.0-1/p50x50/12963374_10154271812561159_4618465219709942847_n.jpg?oh=35c25aa08fea0da67c048870112b2a01&oe=57B3588A',NULL,0,NULL,NULL),(7,'Camilla ','Platania',3,'camilla@platanialab.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(13,'Giulia ','Bruno',3,'giuliavela@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(23,'Gianluca',NULL,3,'copionline@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(24,'Patrizio',NULL,3,'armatore@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(27,'Emanuela',NULL,3,'emanumela@libero.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(29,'Dario',NULL,3,'armatore.yeswesail@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(43,'Carlo ','Terruzzi',3,'karlooie@yahoo.it',NULL,NULL,NULL,NULL,NULL,'10156456751175328',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(46,'Udzejcca',NULL,3,'udzejcca@libero.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(47,'Carla ','Antonini',3,'carla.antonini9@gmail.com',NULL,NULL,NULL,NULL,NULL,'920668441385089',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(67,'Marco ','Troiano',3,'marcotrox@gmail.com',NULL,NULL,NULL,NULL,NULL,'10207161429747117',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(68,'Marco ','Didio',3,'marcodidio65@gmail.com',NULL,NULL,NULL,NULL,NULL,'10208893857292547',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(69,'Carmine',NULL,3,'emikobearvahf@yahoo.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(78,'Sara ','Chiaravalli Bassani',3,'sara.chiaravallibassani@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(84,'Marco ','Vigano',3,'marco@forsailing.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(86,'Chiara',NULL,3,'chiara@forsailing.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(117,'Eugenio ','Orsi',3,'eugenio.orsi@gmail.com',NULL,NULL,NULL,NULL,NULL,'10153220153151557',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(118,'Paolo ','Bologna',3,'p.bologna@finpegasus.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(133,'Laura ','Cernuschi',3,'laura.cernuschi@yahoo.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(161,'Roberto ','Magnifico',3,'r.magnifico@gmail.com',NULL,NULL,NULL,NULL,NULL,'10208628715465755',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(165,'Tiziana ','Scarcella',3,'tiziana.scarcella74@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(166,'Maria',NULL,3,'maria.a82@hotmail.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(167,'Gabriel',NULL,3,'dgabriel86@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(169,'Moana60',NULL,3,'infovelare@libero.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(170,'Roberto ','Tessitore',3,'rtessitore@gmail.com',NULL,NULL,NULL,NULL,NULL,'10154007297852884',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(171,'Marta','',3,'nuovaguinea77@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(172,'Klikk',NULL,3,'klikk01@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(173,'Lorenza ','Fontana',3,'lorenza.fontana2009@gmail.com',NULL,NULL,NULL,NULL,NULL,'927181264064025',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(174,'Riccardo ','Padovani',3,'riccardopadovani@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(176,'Claudita ','Fanni Fertino',3,'clauditafannifertino@libero.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(177,'Ousmane ','Dieng',3,'uzzi89@hotmail.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(178,'Federica ','Invernizzi',3,'federica.invernizzi@libero.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(179,'Cristian ','Landriani',3,'cristian.landriani@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(180,'Marco ','Brambilla',3,'marcobrambilla191@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(181,'Claudio ','Mirasole',3,'claudio.mirasole@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(182,'Christian',NULL,3,'whaska@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(183,'Marzia ','Muratori',3,'marziamu@libero.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(184,'Salvatore',NULL,3,'cs-72@libero.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(185,'Massimiliano ','Catano',3,'massimiliano.catano@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(186,'Luisa ','Conti',3,'conti.luisa73@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(187,'Anna ','Trinci',3,'anna.trinci@hotmail.it',NULL,NULL,NULL,NULL,NULL,'1259755760720631',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(188,'Adriana',' Puga',3,'puga19@virgilio.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(189,'Renato ','Zanon',3,'renato_zanon@tiscali.it',NULL,NULL,NULL,NULL,NULL,'1690842274523725',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(190,'Armando ','Racioppi',3,'racioppiarmando@libero.it',NULL,NULL,NULL,NULL,NULL,'1253159411384915',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(191,'Marco ','Nobili',3,'marco.nobili@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(192,'Patrizio',NULL,3,'patrizio.yeswesail@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(193,'Dario',NULL,3,'dario.yeswesail@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(194,'Monica ','Vinci',3,'monny.momo@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(195,'Francesco ','Signori',3,'francescosignori@alice.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(196,'Corinne ','Graziano',3,'corinne.graziano@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(197,'Gionata ','Coacci',3,'gionata.coacci@oceanofilm.com',NULL,NULL,NULL,NULL,NULL,'1194270517304269',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(198,'Giovanni ','Levoni',3,'giovanni.levoni@gmail.com',NULL,NULL,NULL,NULL,NULL,'10209442583165613',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(200,'Vincenzo ','Puccia',3,'vincenzo.puccia@gmail.com',NULL,NULL,NULL,NULL,NULL,'10154002813794693',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(201,'Giuseppe ','Sorbello',3,'giuseppesorbello@libero.it',NULL,NULL,NULL,NULL,NULL,'10207454437738010',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(202,'Alessandro ','Borghesi',3,'ale.borg1@alice.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(203,'Barbara ','Riccitelli',3,'barbyrace@inwind.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(205,'Cianluca ','Cecchetti',3,'gianluca_cecchetti@iol.it',NULL,NULL,NULL,NULL,NULL,'575999665899260',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(206,'Paolo',NULL,3,'ziopaperone007@hotmail.it',NULL,NULL,NULL,NULL,NULL,'10209511465049750',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(207,'Roberto',NULL,3,'robpao@yahoo.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(208,'Ale ','Furini',3,'furini@plastifur.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(210,'Virginia',' Banti',3,'vbanti@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(211,'Giovanna',NULL,3,'giovanna.bettanini@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(212,'Cristina',NULL,3,'cristina@sottolineo.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(213,'Denis ','Lanza',3,'lanzadionigi@libero.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(214,'Sabry',NULL,3,'sweetsab74@yahoo.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(215,'Daniela ','Palatucci',3,'danypalatucci@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(216,'P ','Canaletto',3,'pcanaletto@yahoo.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(218,'Federico ','Bergomi',3,'federico.bergomi@hotmail.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(219,'Massimo ','Masera',3,'mmasera@comune.chieri.to.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(220,'Marchello ','Miranda',3,'mmarchello@comune.chieri.to.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(221,'Luigi ','Scotto di Santolo',3,'villariflesso@hotmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(222,'Joe  ','Peregrine',3,'gilesfalcon@hotmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(223,'Gabriele',NULL,3,'info@gisail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(224,'Renata',NULL,3,'renataantonella@live.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(225,'Fabrizio ','Liguori',3,'fabrix8@tiscali.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(227,'Trapani ','In Barca',3,'trapaniinbarca@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(228,'Valerio ','Ossola',3,'ossolavalerio@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(229,'Giordan',NULL,3,'giordan703@libero.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(230,'Antonio ','Cicchetti',3,'antoniocicchetti1@virgilio.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(231,'Mimmo',NULL,3,'mimmo.web@virgilio.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(232,'Francesco ','Morici',3,'moricifrancesco@hotmail.com',NULL,NULL,NULL,NULL,NULL,'10154128938358745',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(233,'Marco ','Locatelli',3,'m_locatelli@hotmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(234,'Nino ','Frezza',3,'nino.frezza@alice.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(235,'Giulia ','Rossi',3,'giuliahair@gmail.com',NULL,NULL,NULL,NULL,NULL,'970667829668433',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(236,'Massimo ','Campi',3,'massimocampi@baybroggini.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(237,'Giovanna',NULL,3,'arcicleo@libero.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(238,'Marco ','Politano',3,'marco.politano@fastwebnet.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(239,'Marco ','Rondinelli',3,'marko.rondinelli@gmail.com',NULL,NULL,NULL,NULL,NULL,'10208330200885990',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(240,'Claudio ','Labarbera',3,'claudio.labarbera@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(241,'Teresa ','Sartori',3,'teresa.sart@tin.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(242,'Lele',NULL,3,'leleavela69@virgilio.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(243,'Francesco ','Morici',3,'fieramilanoapartament@gmail.com',NULL,NULL,NULL,NULL,NULL,'1820796234808599',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(244,'Giulia ','Sestini',3,'giuly_29@hotmail.it',NULL,NULL,NULL,NULL,NULL,'10210120284386877',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(245,'Gainpaolo ','Sacco',3,'geogpsacco@yahoo.it',NULL,NULL,NULL,NULL,NULL,'10201672139642532',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(246,'Claudia ','Migliorato',3,'claudiamigliorato@libero.it',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(247,'Massimo ','Governa',3,'governamassimo@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(248,'Fabio ','Verna',3,'f.verna@fabioverna.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(250,'Nicola ','Antelmo',3,'n.antelmo@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(251,'Roberta ','Grippi',3,'roberta.grippi@gmail.com',NULL,NULL,NULL,NULL,NULL,'',NULL,NULL,'L',NULL,NULL,'A',NULL,NULL,0,NULL,NULL),(253,'iosdgd','aaaaaa',1,'ssasssss@gmail.com',NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,'X',NULL,NULL,'A',NULL,NULL,0,NULL,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `UsersAuth`
--

LOCK TABLES `UsersAuth` WRITE;
/*!40000 ALTER TABLE `UsersAuth` DISABLE KEYS */;
INSERT INTO `UsersAuth` VALUES (36,4,'2016-04-24 15:39:35','2016-04-24 16:00:13','14f61b2a-b74d-49b9-9c70-d709aff88411'),(37,5,'2016-04-25 22:51:48','2016-04-25 22:51:48','c4d5a5a6-c562-45ab-bf59-68a10cf47539'),(60,2,'2016-05-15 22:19:00','2016-06-05 15:06:31','51f93591-b501-47f5-9107-af8937fd172c'),(68,3,'2016-06-26 17:03:56','2016-06-26 23:57:50','13a1ec72-f580-4d46-8b27-ffffdedf99fc');
/*!40000 ALTER TABLE `UsersAuth` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wp_users`
--

DROP TABLE IF EXISTS `wp_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wp_users` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_login` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `name` varchar(45) DEFAULT NULL,
  `surname` varchar(45) DEFAULT NULL,
  `user_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `user_url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  `user_registered` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `user_status` int(11) NOT NULL DEFAULT '0',
  `display_name` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=252 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wp_users`
--

LOCK TABLES `wp_users` WRITE;
/*!40000 ALTER TABLE `wp_users` DISABLE KEYS */;
INSERT INTO `wp_users` VALUES (7,'Camilla Platania','Camilla ','Platania','camilla@platanialab.it','','2016-01-13 14:39:53',0,'Camilla'),(13,'Giulia Bruno','Giulia ','Bruno','giuliavela@gmail.com','','2016-01-25 11:59:22',2,'Giulia B.'),(23,'Gianluca N','Gianluca',NULL,'copionline@gmail.com','','2016-02-09 14:27:17',0,'Gianluca N.'),(24,'Patrizio D I','Patrizio',NULL,'armatore@gmail.com','','2016-02-10 09:19:18',0,'Patrizio d.I.'),(27,'Emanuela','Emanuela',NULL,'emanumela@libero.it','','2016-02-10 18:46:32',0,'emanuela'),(29,'Dario','Dario',NULL,'armatore.yeswesail@gmail.com','','2016-02-11 12:46:07',0,'Dario'),(43,'Carlo Terruzzi','Carlo ','Terruzzi','karlooie@yahoo.it','10156456751175328','2016-02-16 16:52:07',0,'Carlo Terruzzi'),(46,'Udzejcca','Udzejcca',NULL,'udzejcca@libero.it','','2016-02-17 10:20:30',0,'udzejcca'),(47,'Carla Antonini','Carla ','Antonini','carla.antonini9@gmail.com','920668441385089','2016-02-17 12:58:33',0,'Carla Antonini'),(67,'Marco Troiano','Marco ','Troiano','marcotrox@gmail.com','10207161429747117','2016-02-21 17:23:35',0,'Marco Troiano'),(68,'Marco Didio','Marco ','Didio','marcodidio65@gmail.com','10208893857292547','2016-02-21 18:40:20',0,'Marco Didio'),(69,'Carmine','Carmine',NULL,'emikobearvahf@yahoo.com','','2016-02-22 01:29:54',0,'carminepalafox'),(78,'Sara Chiaravalli Bassani','Sara ','Chiaravalli Bassani','sara.chiaravallibassani@gmail.com','','2016-02-23 11:31:05',0,'sachiba'),(84,'Marco Vigano','Marco ','Vigano','marco@forsailing.it','','2016-02-24 11:55:01',0,'Marco V'),(86,'Chiara','Chiara',NULL,'chiara@forsailing.it','','2016-02-24 14:10:46',0,'chiara'),(117,'Eugenio Orsi','Eugenio ','Orsi','eugenio.orsi@gmail.com','10153220153151557','2016-02-29 18:36:35',0,'Eugenio Orsi'),(118,'Paolo Bologna','Paolo ','Bologna','p.bologna@finpegasus.com','','2016-03-01 09:52:40',0,'Paolo Bologna'),(133,'Laura Cernuschi','Laura ','Cernuschi','laura.cernuschi@yahoo.it','','2016-03-03 10:09:37',0,'laura'),(161,'Roberto Magnifico','Roberto ','Magnifico','r.magnifico@gmail.com','10208628715465755','2016-03-05 16:52:16',0,'Roberto Magnifico'),(165,'Tiziana Scarcella','Tiziana ','Scarcella','tiziana.scarcella74@gmail.com','','2016-03-13 10:00:15',0,'Tiziana S.'),(166,'Maria','Maria',NULL,'maria.a82@hotmail.it','','2016-03-13 10:16:49',0,'Maria'),(167,'Gabriel','Gabriel',NULL,'dgabriel86@gmail.com','','2016-03-13 10:37:07',0,'prova'),(169,'Moana60','Moana60',NULL,'infovelare@libero.it','','2016-03-14 15:03:20',0,'Nicola P.'),(170,'Roberto Tessitore','Roberto ','Tessitore','rtessitore@gmail.com','10154007297852884','2016-03-15 18:22:17',0,'Roberto Tessitore'),(171,'Marta','Marta','','nuovaguinea77@gmail.com','','2016-03-16 12:59:18',0,'marta'),(172,'klikk','Klikk',NULL,'klikk01@gmail.com','','2016-03-17 15:44:38',0,'klikk'),(173,'Lorenza Fontana','Lorenza ','Fontana','lorenza.fontana2009@gmail.com','927181264064025','2016-03-17 21:23:00',0,'Lorenza Fontana'),(174,'Riccardo Padovani','Riccardo ','Padovani','riccardopadovani@gmail.com','','2016-03-18 13:31:22',0,'riblash'),(176,'Claudita Fanni Fertino','Claudita ','Fanni Fertino','clauditafannifertino@libero.it','','2016-03-21 09:08:58',0,'claudita'),(177,'Ousmane Dieng','Ousmane ','Dieng','uzzi89@hotmail.it','','2016-03-21 13:04:44',0,'Ousmane Dieng'),(178,'Federica Invernizzi','Federica ','Invernizzi','federica.invernizzi@libero.it','','2016-03-21 15:39:14',0,'FEDERICA'),(179,'Cristian Landriani','Cristian ','Landriani','cristian.landriani@gmail.com','','2016-03-21 18:50:18',0,'Cri Landri'),(180,'Marco Brambilla','Marco ','Brambilla','marcobrambilla191@gmail.com','','2016-03-21 18:54:16',0,'marco b.'),(181,'Claudio Mirasole','Claudio ','Mirasole','claudio.mirasole@gmail.com','','2016-03-24 08:55:31',0,'Claudio Mirasole'),(182,'Christian','Christian',NULL,'whaska@gmail.com','','2016-03-24 15:10:24',0,'Christian'),(183,'Marzia Muratori','Marzia ','Muratori','marziamu@libero.it','','2016-03-29 16:38:57',0,'marzia muratori'),(184,'Salvatore','Salvatore',NULL,'cs-72@libero.it','','2016-03-31 08:05:21',0,'Salvatore'),(185,'Massimiliano Catano','Massimiliano ','Catano','massimiliano.catano@gmail.com','','2016-03-31 12:33:43',0,'massimiliano'),(186,'Luisa Conti','Luisa ','Conti','conti.luisa73@gmail.com','','2016-03-31 15:14:13',0,'luisaconti'),(187,'Anna Trinci','Anna ','Trinci','anna.trinci@hotmail.it','1259755760720631','2016-03-31 20:44:22',0,'Anna Trinci'),(188,'Adriana Puga','Adriana',' Puga','puga19@virgilio.it','','2016-04-01 10:45:40',0,'Adriana P.'),(189,'Renato Zanon','Renato ','Zanon','renato_zanon@tiscali.it','1690842274523725','2016-04-02 10:17:19',0,'Renato Z.'),(190,'Armando Racioppi','Armando ','Racioppi','racioppiarmando@libero.it','1253159411384915','2016-04-02 17:50:49',0,'Armando Racioppi'),(191,'Marco Nobili','Marco ','Nobili','marco.nobili@gmail.com','','2016-04-03 16:24:55',0,'freewind'),(192,'Patrizio','Patrizio',NULL,'patrizio.yeswesail@gmail.com','','2016-04-05 09:46:14',0,'Patrizio'),(193,'Dario A','Dario',NULL,'dario.yeswesail@gmail.com','','2016-04-05 11:05:01',0,'Dario A.'),(194,'Monica Vinci','Monica ','Vinci','monny.momo@gmail.com','','2016-04-08 05:39:54',0,'Monica Vinci'),(195,'Francesco Signori','Francesco ','Signori','francescosignori@alice.it','','2016-04-08 18:33:47',0,'Francesco'),(196,'Corinne Graziano','Corinne ','Graziano','corinne.graziano@gmail.com','','2016-04-09 06:23:31',0,'Corinne'),(197,'Gionata Coacci','Gionata ','Coacci','gionata.coacci@oceanofilm.com','1194270517304269','2016-04-09 17:21:07',0,'Gionata Coacci'),(198,'Giovanni Levoni','Giovanni ','Levoni','giovanni.levoni@gmail.com','10209442583165613','2016-04-10 16:39:21',0,'Giovanni Levoni'),(200,'Vincenzo Puccia','Vincenzo ','Puccia','vincenzo.puccia@gmail.com','10154002813794693','2016-04-11 12:14:57',0,'Vincenzo Puccia'),(201,'Giuseppe Sorbello','Giuseppe ','Sorbello','giuseppesorbello@libero.it','10207454437738010','2016-04-11 14:16:47',0,'Giuseppe \"Peppe\"'),(202,'Alessandro Borghesi','Alessandro ','Borghesi','ale.borg1@alice.it','','2016-04-11 23:20:44',0,'Alessandro borghesi'),(203,'Barbara Riccitelli','Barbara ','Riccitelli','barbyrace@inwind.it','','2016-04-12 21:31:33',0,'Barbara Riccitelli'),(205,'Cianluca Cecchetti','Cianluca ','Cecchetti','gianluca_cecchetti@iol.it','575999665899260','2016-04-14 09:09:10',0,'Gianluca Cecchetti'),(206,'Paolo','Paolo',NULL,'ziopaperone007@hotmail.it','10209511465049750','2016-04-14 09:44:48',0,'Paolo Lia'),(207,'Roberto','Roberto',NULL,'robpao@yahoo.it','','2016-04-14 16:02:47',0,'Roberto'),(208,'Ale Furini','Ale ','Furini','furini@plastifur.it','','2016-04-14 17:30:03',0,'Ale'),(210,'Virginia Banti','Virginia',' Banti','vbanti@gmail.com','','2016-04-16 08:46:34',0,'Virginia'),(211,'Giovanna','Giovanna',NULL,'giovanna.bettanini@gmail.com','','2016-04-19 10:16:38',0,'Giovanna80'),(212,'Cristina','Cristina',NULL,'cristina@sottolineo.it','','2016-04-19 10:48:44',0,'CRISTINA'),(213,'Denis Lanza','Denis ','Lanza','lanzadionigi@libero.it','','2016-04-19 13:18:25',0,'Denis'),(214,'Sabry','Sabry',NULL,'sweetsab74@yahoo.it','','2016-04-20 18:20:40',0,'sabrysun'),(215,'Daniela Palatucci','Daniela ','Palatucci','danypalatucci@gmail.com','','2016-05-02 04:13:39',0,'DanielaSea'),(216,'P Canaletto','P ','Canaletto','pcanaletto@yahoo.it','','2016-05-03 11:46:19',0,'Marinaio'),(218,'Federico Bergomi','Federico ','Bergomi','federico.bergomi@hotmail.it','','2016-05-04 07:08:43',0,'Ico'),(219,'Massimo Masera','Massimo ','Masera','mmasera@comune.chieri.to.it','','2016-05-04 08:08:01',0,'Massimo Masera'),(220,'Marchello Miranda','Marchello ','Miranda','mmarchello@comune.chieri.to.it','','2016-05-04 12:04:22',0,'MARCHELLO MIRANDA'),(221,'Luigi','Luigi ','Scotto di Santolo','villariflesso@hotmail.com','','2016-05-04 15:08:26',0,'Luigi Scotto di Santolo'),(222,'Joe Peregrine ','Joe  ','Peregrine','gilesfalcon@hotmail.com','','2016-05-05 10:37:44',0,'Peregrine Joe'),(223,'Gisail','Gabriele',NULL,'info@gisail.com','','2016-05-05 14:25:25',0,'Gabriele e Ico'),(224,'Renata','Renata',NULL,'renataantonella@live.it','','2016-05-07 18:07:00',0,'Renata'),(225,'Fabrizio Liguori','Fabrizio ','Liguori','fabrix8@tiscali.it','','2016-05-09 05:39:48',0,'Fabrizio Liguori'),(227,'Trapani In Barca','Trapani ','In Barca','trapaniinbarca@gmail.com','','2016-05-09 13:27:00',0,'Trapani in Barca'),(228,'Valerio Ossola','Valerio ','Ossola','ossolavalerio@gmail.com','','2016-05-09 20:24:07',0,'valerio'),(229,'Giordan','Giordan',NULL,'giordan703@libero.it','','2016-05-11 18:24:12',0,'giordan'),(230,'Antonio Cicchetti','Antonio ','Cicchetti','antoniocicchetti1@virgilio.it','','2016-05-12 22:15:15',0,'acicchetti'),(231,'Mimmo','Mimmo',NULL,'mimmo.web@virgilio.it','','2016-05-13 22:04:34',0,'mimmo'),(232,'Francesco Morici','Francesco ','Morici','moricifrancesco@hotmail.com','10154128938358745','2016-05-16 10:19:06',0,'Francesco Morici'),(233,'Marco Locatelli','Marco ','Locatelli','m_locatelli@hotmail.com','','2016-05-17 15:38:14',0,'Marco L.'),(234,'Nino Frezza','Nino ','Frezza','nino.frezza@alice.it','','2016-05-19 06:16:41',0,'ninetto1'),(235,'Giulia Rossi','Giulia ','Rossi','giuliahair@gmail.com','970667829668433','2016-05-19 11:59:01',0,'Giulia Rossi'),(236,'Massimo Campi','Massimo ','Campi','massimocampi@baybroggini.it','','2016-05-19 14:31:06',0,'mascampi'),(237,'Giovanna','Giovanna',NULL,'arcicleo@libero.it','','2016-05-20 06:57:25',0,'giovanna'),(238,'Marco Politano','Marco ','Politano','marco.politano@fastwebnet.it','','2016-05-20 13:07:22',0,'MARCO'),(239,'Marco Rondinelli','Marco ','Rondinelli','marko.rondinelli@gmail.com','10208330200885990','2016-05-24 17:19:34',0,'Marco Rondinelli'),(240,'Claudio Labarbera','Claudio ','Labarbera','claudio.labarbera@gmail.com','','2016-05-25 08:13:04',0,'claudio.labarbera'),(241,'Teresa Sartori','Teresa ','Sartori','teresa.sart@tin.it','','2016-05-29 20:35:56',0,'mtsartori'),(242,'Lele','Lele',NULL,'leleavela69@virgilio.it','','2016-05-30 19:16:17',0,'lele'),(243,'Francesco Morici','Francesco ','Morici','fieramilanoapartament@gmail.com','1820796234808599','2016-05-31 09:53:34',0,'Francesco Morici'),(244,'Giulia Sestini','Giulia ','Sestini','giuly_29@hotmail.it','10210120284386877','2016-06-01 14:59:17',0,'Giulia Sestini'),(245,'Gainpaolo Sacco','Gainpaolo ','Sacco','geogpsacco@yahoo.it','10201672139642532','2016-06-02 17:32:46',0,'Gianpaolo Sacco'),(246,'Claudia Migliorato','Claudia ','Migliorato','claudiamigliorato@libero.it','','2016-06-06 17:35:41',0,'Claudia Migliorato'),(247,'Massimo Governa','Massimo ','Governa','governamassimo@gmail.com','','2016-06-06 21:34:59',0,'Massimo Governa'),(248,'Fabio Verna','Fabio ','Verna','f.verna@fabioverna.com','','2016-06-08 15:14:47',0,'Fabio V.'),(250,'Nicola Antelmo','Nicola ','Antelmo','n.antelmo@gmail.com','','2016-06-11 15:20:34',0,'NICOLA'),(251,'Roberta Grippi','Roberta ','Grippi','roberta.grippi@gmail.com','','2016-06-14 15:05:09',0,'robertag');
/*!40000 ALTER TABLE `wp_users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-06-27  0:54:26
