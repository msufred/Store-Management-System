CREATE SCHEMA `billings` ;

CREATE TABLE `billings`.`accounts` (
  `account_no` VARCHAR(8) NOT NULL,
  `firstname` VARCHAR(45) NOT NULL,
  `lastname` VARCHAR(45) NOT NULL,
  `street` VARCHAR(45) NULL,
  `barangay` VARCHAR(45) NULL,
  `city` VARCHAR(45) NULL,
  `contact_no` VARCHAR(12) NULL,
  `data_plan` INT NOT NULL,
  `status` VARCHAR(10) NULL,
  `date_registered` DATETIME NOT NULL,
  `last_date_paid` DATETIME NULL,
  PRIMARY KEY (`account_no`));

CREATE TABLE `billings`.`billings` (
  `billing_no` INT NOT NULL AUTO_INCREMENT,
  `account_no` VARCHAR(8) NOT NULL,
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
    ON DELETE NO ACTION
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
  `id` INT NOT NULL,
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

CREATE TABLE `billings`.`products` (
  `product_no` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `price` DOUBLE NOT NULL DEFAULT 0.0,
  `count` INT NOT NULL DEFAULT 0,
  `description` LONGTEXT NULL,
  PRIMARY KEY (`product_no`));

