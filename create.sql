CREATE TABLE `accounts` (
  `account_no` varchar(8) NOT NULL,
  `firstname` varchar(45) NOT NULL,
  `lastname` varchar(45) NOT NULL,
  `contact_no` varchar(12) NOT NULL,
  `status` varchar(10) NOT NULL,
  `date_registered` datetime NOT NULL,
  `last_date_paid` datetime DEFAULT NULL,
  `type` varchar(45) NOT NULL DEFAULT 'Personal',
  PRIMARY KEY (`account_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `addresses` (
  `account_no` varchar(8) NOT NULL,
  `province` longtext NOT NULL,
  `city` longtext NOT NULL,
  `barangay` longtext NOT NULL,
  `landmark` varchar(45) NOT NULL,
  PRIMARY KEY (`account_no`),
  CONSTRAINT `acct_no3` FOREIGN KEY (`account_no`) REFERENCES `accounts` (`account_no`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `internet_subscriptions` (
  `subscription_no` int(11) NOT NULL AUTO_INCREMENT,
  `account_no` varchar(8) NOT NULL,
  `bandwidth` int(11) NOT NULL DEFAULT '0',
  `amount` double NOT NULL DEFAULT '0',
  `ip_address` varchar(45) DEFAULT NULL,
  `latitude` float DEFAULT '0',
  `longitude` float DEFAULT '0',
  `elevation` float DEFAULT '0',
  PRIMARY KEY (`subscription_no`),
  KEY `acct_no2_idx` (`account_no`),
  CONSTRAINT `acct_no2` FOREIGN KEY (`account_no`) REFERENCES `accounts` (`account_no`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `billings` (
  `billing_no` int(11) NOT NULL AUTO_INCREMENT,
  `account_no` varchar(8) DEFAULT NULL,
  `amount` double NOT NULL,
  `billing_date` datetime NOT NULL,
  `from_date` varchar(45) DEFAULT NULL,
  `to_date` varchar(45) DEFAULT NULL,
  `due_date` varchar(45) NOT NULL,
  `status` varchar(45) NOT NULL,
  `date_updated` datetime DEFAULT NULL,
  `type` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`billing_no`),
  KEY `account_no_idx` (`account_no`),
  CONSTRAINT `account_no` FOREIGN KEY (`account_no`) REFERENCES `accounts` (`account_no`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `payments` (
  `payment_no` int(11) NOT NULL AUTO_INCREMENT,
  `billing_no` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `amount` double NOT NULL,
  `quantity` int(11) NOT NULL,
  `total_amount` double NOT NULL,
  `description` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`payment_no`),
  KEY `billing_no_idx` (`billing_no`),
  CONSTRAINT `billing_no` FOREIGN KEY (`billing_no`) REFERENCES `billings` (`billing_no`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `billings_processed` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `billing_no` int(11) NOT NULL,
  `amount_due` double NOT NULL,
  `amound_paid` double NOT NULL,
  `date_transaction` datetime NOT NULL,
  `remarks` longtext,
  PRIMARY KEY (`id`),
  KEY `billing_no_idx` (`billing_no`),
  CONSTRAINT `billing_no2` FOREIGN KEY (`billing_no`) REFERENCES `billings` (`billing_no`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `balances` (
  `balance_no` int(11) NOT NULL AUTO_INCREMENT,
  `billing_processed_no` int(11) NOT NULL,
  `account_no` varchar(8) DEFAULT NULL,
  `amount` double DEFAULT NULL,
  `paid` varchar(5) NOT NULL DEFAULT 'false',
  `date_paid` datetime DEFAULT NULL,
  PRIMARY KEY (`balance_no`),
  KEY `account_fk_idx` (`account_no`),
  KEY `billing_processed_fk_idx` (`billing_processed_no`),
  CONSTRAINT `account_fk` FOREIGN KEY (`account_no`) REFERENCES `accounts` (`account_no`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `billing_processed_fk` FOREIGN KEY (`billing_processed_no`) REFERENCES `billings_processed` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `services` (
  `service_no` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `est_price` double DEFAULT NULL,
  `description` longtext,
  PRIMARY KEY (`service_no`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `products` (
  `product_no` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `price` double NOT NULL DEFAULT '0',
  `count` int(11) NOT NULL DEFAULT '0',
  `description` longtext,
  PRIMARY KEY (`product_no`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `billings`.`users`
(`username`,
`password`,
`authority`)
VALUES
('admin',
'admin',
'administrator');
