package com.electonica.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

import com.electonica.DataBaseConfig.MySQLConnection;
import com.electonica.modals.CustomerTransaction;
import com.electonica.modals.MasterDataSegment;
import com.electonica.modals.MasterDataTuple;

public class HybridJoinThread extends Thread {

    private Queue<Integer> joinAttributeQueue;
    private Map<Integer, CustomerTransaction> customerHashTable;
    private Map<Integer, MasterDataSegment> diskBuffer;
    private Map<Integer, MasterDataTuple> multiHashTable;

    // Database connection 
    MySQLConnection mysqlConnection = new MySQLConnection();


    // Constants
    private static final int CHUNK_SIZE = 10000;
    private static final int MD_SEGMENT_SIZE = 10;

    public HybridJoinThread(Queue<Integer> joinAttributeQueue,
                            Map<Integer, CustomerTransaction> customerHashTable,
                            Map<Integer, MasterDataSegment> diskBuffer,
                            Map<Integer, MasterDataTuple> multiHashTable) {
        this.joinAttributeQueue = joinAttributeQueue;
        this.customerHashTable = customerHashTable;
        this.diskBuffer = diskBuffer;
        this.multiHashTable = multiHashTable;
    }

    @Override
    public void run() {
        initialize();
        executeHybridJoin();
    }

    private void initialize() {
        loadInitialCustomerData(CHUNK_SIZE);
        loadInitialMasterDataSegments(MD_SEGMENT_SIZE);
        loadInitialMultiHashTableData(MD_SEGMENT_SIZE);
    }

    private void loadInitialCustomerData(int chunkSize) {
        try {
            Connection connection = mysqlConnection.getConnection();

            String query = "SELECT * FROM transactions LIMIT ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, chunkSize);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int orderId = resultSet.getInt("OrderID");
                Date orderDate = resultSet.getDate("OrderDate");
                int productId = resultSet.getInt("ProductID");
                int customerId = resultSet.getInt("CustomerID");
                String customerName = resultSet.getString("CustomerName");
                String gender = resultSet.getString("Gender");
                int quantityOrdered = resultSet.getInt("QuantityOrdered");

                // Assuming you have a CustomerTransaction class with appropriate constructors and setters
                CustomerTransaction transaction = new CustomerTransaction(orderId, orderDate, productId, customerId, customerName, gender, quantityOrdered);

                // Populate customerHashTable
                customerHashTable.put(customerId, transaction);

                // Populate joinAttributeQueue
                joinAttributeQueue.add(customerId);
            }

            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception
        }
    }

    private void loadInitialMasterDataSegments(int segmentSize) {
        // Implement logic to load initial master data segments to disk buffer from the database
        // ...

        // For example:
        try {
            Connection connection = mysqlConnection.getConnection();

            String query = "SELECT * FROM master_data LIMIT ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, segmentSize);
            // Execute the query and populate diskBuffer
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("ProductID"); // Replace with the actual primary key column name
                String productName = resultSet.getString("ProductName");
                double productPrice = resultSet.getDouble("ProductPrice");
                int supplierID = resultSet.getInt("SupplierID");
                String supplierName = resultSet.getString("SupplierName");

                // Assuming you have a MasterDataTuple class with appropriate constructors and setters
                MasterDataTuple masterDataTuple = new MasterDataTuple(id, productName, productPrice, supplierID, supplierName);

                // Assuming you have a MasterDataSegment class with appropriate constructors and setters
                MasterDataSegment masterDataSegment = new MasterDataSegment(id, Collections.singletonList(masterDataTuple));

                // Populate diskBuffer
                diskBuffer.put(id, masterDataSegment);
            }
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception
        }
    }
    private void loadInitialMultiHashTableData(int segmentSize) {
        // Implement logic to load initial data into multiHashTable from the database
        // ...

        // For example:
        try {
            Connection connection = mysqlConnection.getConnection();

            String query = "SELECT * FROM master_data LIMIT ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, segmentSize);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("ProductID"); // Replace with the actual primary key column name
                String productName = resultSet.getString("ProductName");
                double productPrice = resultSet.getDouble("ProductPrice");
                int supplierID = resultSet.getInt("SupplierID");
                String supplierName = resultSet.getString("SupplierName");

                // Assuming you have a MasterDataTuple class with appropriate constructors and setters
                MasterDataTuple masterDataTuple = new MasterDataTuple(id, productName, productPrice, supplierID, supplierName);

                // Populate multiHashTable
                multiHashTable.put(id, masterDataTuple);
            }
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception
        }
    }


    private void executeHybridJoin() {
        System.out.println("execute Hybrid Join");
     // Add logging in loadInitialMasterDataSegments
     // Inside loadInitialMasterDataSegments
        System.out.println("Loading Master Data Segments...");
        // Existing code for database query and populating diskBuffer...
        System.out.println("Disk Buffer Contents: " + diskBuffer.keySet());
        System.out.println("MultiHashTable Contents: " + multiHashTable.keySet());

        try {
            Iterator<Integer> iterator = joinAttributeQueue.iterator();
            while (iterator.hasNext()) {
                int joinAttributeValue = iterator.next();
                MasterDataSegment mdSegment = diskBuffer.get(joinAttributeValue);
                System.out.println();
                // Check if mdSegment is null before accessing its properties
                if (mdSegment != null && mdSegment.getTuples() != null) {
                    // Probe the MD segment into the multi-hash table
                    for (MasterDataTuple mdTuple : mdSegment.getTuples()) {
                        if (mdTuple != null && multiHashTable.containsKey(mdTuple.getId())) {
                            // Match found, join the records and produce output
                            CustomerTransaction transaction = customerHashTable.get(joinAttributeValue);

                            // Check if transaction is not null before proceeding
                            if (transaction != null) {
                                // Implement logic to join and output the result to the data warehouse
                                insertIntoDataWarehouse(transaction, mdTuple);

                                // Remove the matched tuple from the multi-hash table and the queue
                                multiHashTable.remove(mdTuple.getId());
                                iterator.remove(); // Use iterator's remove method to avoid ConcurrentModificationException
                                System.out.println("done");

                            } else {
                                // Handle the case where transaction is null
                                System.err.println("Null transaction for join attribute value: " + joinAttributeValue);
                            }
                        }
                    }
                } else {
                    // Handle the case where mdSegment or mdSegment.getTuples() is null
                    System.err.println("Null mdSegment or tuples for join attribute value: " + joinAttributeValue);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception
        }
    }


    private void insertIntoDataWarehouse(CustomerTransaction transaction, MasterDataTuple mdTuple) {
    	 try {
             Connection connection = mysqlConnection.getConnection();

             // Insert into DimProduct
             insertOrUpdateDimProduct(mdTuple, connection);

             // Insert into DimDate
             insertOrUpdateDimDate(transaction.getOrderDate(), connection);

             // Insert into DimCustomer
             insertOrUpdateDimCustomer(transaction, connection);

             // Insert into FactSales
             insertIntoFactSales(transaction, mdTuple, connection);

             connection.close();
         } catch (SQLException e) {
             e.printStackTrace();
             // Handle the exception
         }
    }
    
    private void insertOrUpdateDimProduct(MasterDataTuple mdTuple, Connection connection) throws SQLException {
        String query = "INSERT INTO dim_product (ProductID, ProductName, ProductPrice, SupplierID, StoreID) " +
                       "VALUES (?, ?, ?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE " +
                       "ProductName = VALUES(ProductName), " +
                       "ProductPrice = VALUES(ProductPrice), " +
                       "SupplierID = VALUES(SupplierID), " +
                       "StoreID = VALUES(StoreID)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, mdTuple.getId());
            statement.setString(2, mdTuple.getProductName());
            statement.setDouble(3, mdTuple.getProductPrice());
            statement.setInt(4, mdTuple.getSupplierID());
            statement.setInt(5, mdTuple.getStoreID());
            statement.executeUpdate();
        }
    }

    private void insertOrUpdateDimDate(Date orderDate, Connection connection) throws SQLException {
        String query = "INSERT INTO dim_date (OrderDate) VALUES (?) ON DUPLICATE KEY UPDATE OrderDate = VALUES(OrderDate)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDate(1, new java.sql.Date(orderDate.getTime()));
            statement.executeUpdate();
        }catch(Exception ex) {
        	ex.printStackTrace();
        }
    }
 

    private void insertOrUpdateDimCustomer(CustomerTransaction transaction, Connection connection) throws SQLException {
        String query = "INSERT INTO dim_customer (CustomerID, CustomerName, Gender) " +
                       "VALUES (?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE " +
                       "CustomerName = VALUES(CustomerName), " +
                       "Gender = VALUES(Gender)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, transaction.getCustomerID());
            statement.setString(2, transaction.getCustomerName());
            statement.setString(3, transaction.getGender());
            statement.executeUpdate();
        }
    }

    private void insertIntoFactSales(CustomerTransaction transaction, MasterDataTuple mdTuple, Connection connection) throws SQLException {
        String query = "INSERT INTO fact_sales (OrderID, OrderDate, ProductID, CustomerID, QuantityOrdered) " +
                       "VALUES (?, ?, ?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE " +
                       "OrderDate = VALUES(OrderDate), " +
                       "ProductID = VALUES(ProductID), " +
                       "CustomerID = VALUES(CustomerID), " +
                       "QuantityOrdered = VALUES(QuantityOrdered)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, transaction.getId());
            statement.setDate(2, new java.sql.Date(transaction.getOrderDate().getTime()));
            statement.setInt(3, mdTuple.getId());
            statement.setInt(4, transaction.getCustomerID());
            statement.setInt(5, transaction.getQuantityOrdered());
            statement.executeUpdate();
        }
    }
}
