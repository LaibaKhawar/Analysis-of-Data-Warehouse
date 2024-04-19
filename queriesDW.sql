-- Drill Down to Quarter and Month

-- Quarter level
SELECT
    SupplierID,
    QUARTER(OrderDate) AS Quarter,
    SUM(TotalSales) AS QuarterTotalSales
FROM
    (
        SELECT
            p.SupplierID,
            d.OrderDate,
            SUM(s.QuantityOrdered) AS TotalSales
        FROM
            fact_sales s
        JOIN
            dim_product p ON s.ProductID = p.ProductID
        JOIN
            dim_date d ON s.OrderDate = d.OrderDate
        GROUP BY
            p.SupplierID, d.OrderDate
    ) AS QuarterlySales
GROUP BY
    SupplierID, Quarter
ORDER BY
    SupplierID, Quarter;
    
-- Month Level
SELECT
    SupplierID,
    QUARTER(OrderDate) AS Quarter,
    SUM(TotalSales) AS QuarterTotalSales
FROM
    (
        SELECT
            p.SupplierID,
            d.OrderDate,
            SUM(s.QuantityOrdered) AS TotalSales
        FROM
            fact_sales s
        JOIN
            dim_product p ON s.ProductID = p.ProductID
        JOIN
            dim_date d ON s.OrderDate = d.OrderDate
        GROUP BY
            p.SupplierID, d.OrderDate
    ) AS QuarterlySales
GROUP BY
    SupplierID, Quarter
ORDER BY
    SupplierID, Quarter;
    
    
-- Total Sales for all products by month
SELECT
    p.ProductID,
    p.ProductName,
    MONTH(d.OrderDate) AS Month,
    SUM(fs.QuantityOrdered) AS MonthlyTotalSales
FROM
    fact_sales fs
JOIN
    dim_product p ON fs.ProductID = p.ProductID
JOIN
    dim_date d ON fs.OrderDate = d.OrderDate
WHERE
    YEAR(d.OrderDate) = 2019
GROUP BY
    p.ProductID, p.ProductName, MONTH(d.OrderDate)

UNION ALL
SELECT
    p.ProductID,
    p.ProductName,
    MONTH(d.OrderDate) AS Month,
    SUM(fs.QuantityOrdered) AS MonthlyTotalSales
FROM
    fact_sales fs
JOIN
    dim_product p ON fs.ProductID = p.ProductID
JOIN
    dim_date d ON fs.OrderDate = d.OrderDate
JOIN
    dim_customer c ON fs.CustomerID = c.CustomerID
WHERE
    c.Gender = 'M,F' AND YEAR(d.OrderDate) = 2019
GROUP BY
    p.ProductID, p.ProductName, MONTH(d.OrderDate)

ORDER BY
    ProductID, Month;
    
    
-- Find the 5 most popular products sold over the weekends
SELECT
    p.ProductID,
    p.ProductName,
    SUM(fs.QuantityOrdered) AS TotalSales
FROM
    fact_sales fs
JOIN
    dim_product p ON fs.ProductID = p.ProductID
JOIN
    dim_date d ON fs.OrderDate = d.OrderDate
WHERE
    DAYOFWEEK(d.OrderDate) IN (1, 7) -- Assuming 1 is Sunday and 7 is Saturday
GROUP BY
    p.ProductID, p.ProductName
ORDER BY
    TotalSales DESC
LIMIT 5;

-- Present the quarterly sales of each product for 2019 along with its total yearly sales
-- each quarter sale must be a column and yearly sale as well. Order result according to product
SELECT
    p.ProductID,
    p.ProductName,
    SUM(CASE WHEN MONTH(d.OrderDate) BETWEEN 1 AND 3 THEN fs.QuantityOrdered ELSE 0 END) AS Q1,
    SUM(CASE WHEN MONTH(d.OrderDate) BETWEEN 4 AND 6 THEN fs.QuantityOrdered ELSE 0 END) AS Q2,
    SUM(CASE WHEN MONTH(d.OrderDate) BETWEEN 7 AND 9 THEN fs.QuantityOrdered ELSE 0 END) AS Q3,
    SUM(CASE WHEN MONTH(d.OrderDate) BETWEEN 10 AND 12 THEN fs.QuantityOrdered ELSE 0 END) AS Q4,
    SUM(fs.QuantityOrdered) AS YearlyTotal
FROM
    fact_sales fs
JOIN
    dim_product p ON fs.ProductID = p.ProductID
JOIN
    dim_date d ON fs.OrderDate = d.OrderDate
WHERE
    YEAR(d.OrderDate) = 2019
GROUP BY
    p.ProductID, p.ProductName
ORDER BY
    p.ProductID;

-- Find an anomaly in the data warehouse dataset.
WITH SalesSummary AS (
    SELECT
        ProductID,
        AVG(QuantityOrdered) AS AvgQuantity,
        STDDEV(QuantityOrdered) AS StdDevQuantity
    FROM
        fact_sales
    GROUP BY
        ProductID
)

SELECT
    fs.OrderID,
    fs.ProductID,
    fs.QuantityOrdered,
    ss.AvgQuantity,
    ss.StdDevQuantity,
    (fs.QuantityOrdered - ss.AvgQuantity) / ss.StdDevQuantity AS ZScore
FROM
    fact_sales fs
JOIN
    SalesSummary ss ON fs.ProductID = ss.ProductID
WHERE
    ABS((fs.QuantityOrdered - ss.AvgQuantity) / ss.StdDevQuantity) > 3 -- Adjust the threshold as needed
ORDER BY
    ZScore DESC;


-- materialised view 
CREATE VIEW STOREANALYSIS_MV AS
SELECT
    md.StoreID,
    fs.ProductID,
    SUM(fs.QuantityOrdered) AS StoreTotal
FROM
    fact_sales fs
JOIN
    master_data md ON fs.ProductID = md.ProductID
GROUP BY
    md.StoreID, fs.ProductID;


-- the concept of Slicing calculate the total sales 
SELECT
    md.StoreName,
    fs.ProductID,
    MONTH(fs.OrderDate) AS Month,
    SUM(fs.QuantityOrdered) AS MonthlyTotalSales
FROM
    fact_sales fs
JOIN
    master_data md ON fs.ProductID = md.ProductID
WHERE
    md.StoreName = 'Tech Haven'
GROUP BY
    md.StoreName, fs.ProductID, MONTH(fs.OrderDate);

-- materialized view named "SUPPLIER_PERFORMANCE MV" that presents the monthly performance of each supplier.
CREATE  VIEW SUPPLIER_PERFORMANCE_MV AS
SELECT
    c.CustomerID AS SupplierID,  -- Assuming CustomerID is used as SupplierID in dim_customer
    MONTH(d.OrderDate) AS Month,
    SUM(fs.QuantityOrdered) AS MonthlyPerformance
FROM
    fact_sales fs
JOIN
    dim_date d ON fs.OrderDate = d.OrderDate
JOIN
    master_data m ON fs.ProductID = m.ProductID
JOIN
    dim_customer c ON m.SupplierID = c.CustomerID  -- Assuming SupplierID is stored in dim_customer
GROUP BY
    c.CustomerID, MONTH(d.OrderDate);


-- Identify the top 5 customers with the highest total sales in 2019, considering the number of unique products they purchased.
SELECT
    c.CustomerID,
    c.CustomerName,
    COUNT(DISTINCT fs.ProductID) AS UniqueProductCount,
    SUM(fs.QuantityOrdered) AS TotalSales
FROM
    fact_sales fs
JOIN
    dim_customer c ON fs.CustomerID = c.CustomerID
JOIN
    dim_date d ON fs.OrderDate = d.OrderDate
WHERE
    YEAR(d.OrderDate) = 2019
GROUP BY
    c.CustomerID, c.CustomerName
ORDER BY
    TotalSales DESC
LIMIT 5;

-- Create a materialized view named "CUSTOMER_STORE_SALES_MV" that presents the monthly sales analysis for each store and then customers wise
CREATE VIEW CUSTOMER_STORE_SALES_MV AS
SELECT
    m.StoreID,
    c.CustomerID,
    EXTRACT(MONTH FROM t.OrderDate) AS Month,
    EXTRACT(YEAR FROM t.OrderDate) AS Year,
    SUM(t.QuantityOrdered) AS MonthlySales
FROM
    transactions t
JOIN
    dim_customer c ON t.CustomerID = c.CustomerID
JOIN
    master_data m ON t.ProductID = m.ProductID
JOIN
    dim_date d ON t.OrderDate = d.OrderDate
GROUP BY
    m.StoreID, c.CustomerID, EXTRACT(MONTH FROM t.OrderDate), EXTRACT(YEAR FROM t.OrderDate);




