CREATE SCHEMA `billings` ;

CREATE TABLE `billings`.`accounts` (
  `account_no` VARCHAR(8) NOT NULL,
  `firstname` VARCHAR(45) NOT NULL,
  `lastname` VARCHAR(45) NOT NULL,
  `address` LONGTEXT NOT NULL,
  `contact_no` VARCHAR(12) NOT NULL,
  `status` VARCHAR(10) NOT NULL,
  `date_registered` DATETIME NOT NULL,
  `last_date_paid` DATETIME NULL,
  `type` VARCHAR(45) NOT NULL DEFAULT 'Personal',
  PRIMARY KEY (`account_no`));

/*
`addresses` entries will be removed if their parent table entry is deleted.
*/
CREATE TABLE `billings`.`addresses` (
  `account_no` VARCHAR(8) NOT NULL,
  `province` LONGTEXT NOT NULL,
  `city` LONGTEXT NOT NULL,
  `barangay` LONGTEXT NOT NULL,
  `landmark` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`account_no`),
  CONSTRAINT `acct_no3`
    FOREIGN KEY (`account_no`)
    REFERENCES `billings`.`accounts` (`account_no`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);

/*
We will set the `account_no` to NULL if the parent table entry is deleted.
*/
CREATE TABLE `billings`.`billings` (
  `billing_no` INT NOT NULL AUTO_INCREMENT,
  `account_no` VARCHAR(8) NULL, 
  `amount` DOUBLE NOT NULL,
  `billing_date` DATETIME NOT NULL,
  `from_date` VARCHAR(45),
  `to_date` VARCHAR(45),
  `due_date` VARCHAR(45) NOT NULL,
  `status` VARCHAR(45) NOT NULL,
  `date_updated` DATETIME,
  `type` VARCHAR(10),
  PRIMARY KEY (`billing_no`),
  INDEX `account_no_idx` (`account_no` ASC) VISIBLE,
  CONSTRAINT `account_no`
    FOREIGN KEY (`account_no`)
    REFERENCES `billings`.`accounts` (`account_no`)
    ON DELETE SET NULL
	ON UPDATE CASCADE);

CREATE TABLE `billings`.`payments` (
  `payment_no` INT NOT NULL AUTO_INCREMENT,
  `billing_no` INT NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `amount` DOUBLE NOT NULL,
  `quantity` INT NOT NULL,
  `total_amount` DOUBLE NOT NULL,
  `description` VARCHAR(200) NULL,
  PRIMARY KEY (`payment_no`),
  INDEX `billing_no_idx` (`billing_no` ASC) VISIBLE,
  CONSTRAINT `billing_no`
    FOREIGN KEY (`billing_no`)
    REFERENCES `billings`.`billings` (`billing_no`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
    
CREATE TABLE `billings`.`billings_processed` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `billing_no` INT NOT NULL,
  `amount_due` DOUBLE NOT NULL,
  `amound_paid` DOUBLE NOT NULL,
  `date_transaction` DATETIME NOT NULL,
  `remarks` LONGTEXT NULL,
  PRIMARY KEY (`id`),
  INDEX `billing_no_idx` (`billing_no` ASC) VISIBLE,
  CONSTRAINT `billing_no2`
    FOREIGN KEY (`billing_no`)
    REFERENCES `billings`.`billings` (`billing_no`)
    ON DELETE NO ACTION
    ON UPDATE CASCADE);

CREATE TABLE `billings`.`balances` (
  `balance_no` INT NOT NULL AUTO_INCREMENT,
  `billing_processed_no` INT NOT NULL,
  `account_no` VARCHAR(8) NULL,
  `amount` DOUBLE NULL,
  `paid` VARCHAR(5) NOT NULL DEFAULT 'false',
  `date_paid` DATETIME NULL,
  PRIMARY KEY (`balance_no`),
  INDEX `billing_processed_fk_idx` (`billing_processed_no` ASC) VISIBLE,
  INDEX `account_fk_idx` (`account_no` ASC) VISIBLE,
  CONSTRAINT `billing_processed_fk`
    FOREIGN KEY (`billing_processed_no`)
    REFERENCES `billings`.`billings_processed` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `account_fk`
    FOREIGN KEY (`account_no`)
    REFERENCES `billings`.`accounts` (`account_no`)
    ON DELETE SET NULL
    ON UPDATE CASCADE);
	
    
    ALTER TABLE `billings`.`balances` 
ADD COLUMN `account_no` VARCHAR(8) NULL AFTER `billing_no`,
ADD INDEX `account_no_fk_idx` (`account_no` ASC) VISIBLE;
;
ALTER TABLE `billings`.`balances` 
ADD CONSTRAINT `account_no_fk`
  FOREIGN KEY (`account_no`)
  REFERENCES `billings`.`accounts` (`account_no`)
  ON DELETE SET NULL
  ON UPDATE CASCADE;


CREATE TABLE `billings`.`products` (
  `product_no` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `price` DOUBLE NOT NULL DEFAULT 0.0,
  `count` INT NOT NULL DEFAULT 0,
  `description` LONGTEXT NULL,
  PRIMARY KEY (`product_no`));
  
CREATE TABLE `billings`.`services` (
  `service_no` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `est_price` DOUBLE NULL,
  `description` LONGTEXT NULL,
  PRIMARY KEY (`service_no`));
  
CREATE TABLE `billings`.`internet_subscriptions` (
  `subscription_no` INT NOT NULL AUTO_INCREMENT,
  `account_no` VARCHAR(8) NOT NULL,
  `bandwidth` INT NOT NULL DEFAULT 0,
  `amount` DOUBLE NOT NULL DEFAULT 0.0,
  `ip_address` VARCHAR(45) NULL,
  `latitude` FLOAT NULL DEFAULT 0,
  `longitude` FLOAT NULL DEFAULT 0,
  `elevation` FLOAT NULL DEFAULT 0,
  PRIMARY KEY (`subscription_no`),
  INDEX `acct_no2_idx` (`account_no` ASC) VISIBLE,
  CONSTRAINT `acct_no2`
    FOREIGN KEY (`account_no`)
    REFERENCES `billings`.`accounts` (`account_no`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
