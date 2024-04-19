package com.electonica.controller;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;

import com.electonica.DataBaseConfig.MySQLConnection;

public class DataLoaderController {

    private MySQLConnection mySQLConnection = new MySQLConnection();

    public void loadData(String csvFilePath, String tableName) {
        Connection connection = null;
        try {
            connection = mySQLConnection.getConnection();

            // Load data into the specified table
            loadData(connection, csvFilePath, tableName);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mySQLConnection.closeConnection(connection);
        }
    }

    private void loadData(Connection connection, String csvFilePath, String tableName) throws Exception {
    	   System.out.println("File path: " + csvFilePath);
    	String sql = "LOAD DATA LOCAL INFILE ? INTO TABLE " + tableName +
                " FIELDS TERMINATED BY ',' LINES TERMINATED BY '\r\n'";
        System.out.println("Executing SQL query: " + sql);
        System.out.println();
     

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, csvFilePath);
            int rowsAffected = preparedStatement.executeUpdate();

            System.out.println("Rows affected: " + rowsAffected);

            if (rowsAffected > 0) {
                System.out.println("Data loaded into table '" + tableName + "' successfully.");
            } else {
                System.out.println("No data loaded into table '" + tableName + "'. Please check for errors.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
