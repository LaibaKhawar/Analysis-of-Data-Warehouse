GRANT FILE ON *.* TO 'root'@'localhost';

-- Enable local_infile on the server side
SET GLOBAL local_infile = 1;

-- Create the transactional data table
CREATE TABLE IF NOT EXISTS transactions (
    OrderID INT,
    OrderDate DATE,
    ProductID INT,
    CustomerID INT,
    CustomerName VARCHAR(255),
    Gender VARCHAR(1),
    QuantityOrdered INT,
    PRIMARY KEY (OrderID)
);

-- Create the master data table
CREATE TABLE IF NOT EXISTS master_data (
    ProductID INT,
    ProductName VARCHAR(255),
    ProductPrice DECIMAL(10, 2),
    SupplierID INT,
    SupplierName VARCHAR(255),
    StoreID INT,
    StoreName VARCHAR(255),
    PRIMARY KEY (ProductID)
);

-- Create the product dimension table
CREATE TABLE IF NOT EXISTS dim_product (
    ProductID INT PRIMARY KEY,
    ProductName VARCHAR(255),
    ProductPrice DECIMAL(10,2),
    SupplierID INT,
    StoreID INT
);

-- Create the date dimension table
CREATE TABLE IF NOT EXISTS dim_date (
    OrderDate DATE PRIMARY KEY
    -- add other date-related attributes if needed
);

-- Create the customer dimension table
CREATE TABLE IF NOT EXISTS dim_customer (
    CustomerID INT PRIMARY KEY,
    CustomerName VARCHAR(255),
    Gender VARCHAR(10)
);

-- Create the fact sales table
CREATE TABLE IF NOT EXISTS fact_sales (
    OrderID INT PRIMARY KEY,
    OrderDate DATE,
    ProductID INT,
    CustomerID INT,
    QuantityOrdered INT 
);
