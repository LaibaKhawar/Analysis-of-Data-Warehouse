package com.electonica;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

import com.electonica.controller.DataLoaderController;
import com.electonica.modals.CustomerTransaction;
import com.electonica.modals.MasterDataSegment;
import com.electonica.modals.MasterDataTuple;
import com.electonica.services.HybridJoinThread;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args )
    {
        // Take input from the user for the database password
        Scanner scanner = new Scanner(System.in);
        String password;
        do {
            System.out.print("Enter the database password: ");
            password = scanner.nextLine();

            // Check if the password is correct
            if (!password.equals("root")) {
                System.out.print("Password incorrect. Want to try again? (y/n): ");
                String tryAgain = scanner.nextLine();

                if (!tryAgain.equalsIgnoreCase("y")) {
                    // Exit the application if the user chooses not to try again
                    System.out.println("Exiting the application.");
                    System.exit(0);
                }
            }
        } while (!password.equals("root"));

        DataLoaderController controller = new DataLoaderController();

        //TODO chnage the path to your local filePath
        // Load transactional data
        controller.loadData("C:\\Users\\**\\Downloads\\transactions.csv", "transactions");

        //TODO chnage the path to your local filePath 
        // Load master data
        controller.loadData("C:\\Users\\**\\Downloads\\master_data.csv", "master_data");

        // Create data structures needed for the thread
        Queue<Integer> joinAttributeQueue = new LinkedList<>();
        Map<Integer, CustomerTransaction> customerHashTable = new HashMap<>();
        Map<Integer, MasterDataSegment> diskBuffer = new HashMap<>();
        Map<Integer, MasterDataTuple> multiHashTable = new HashMap<>();

        // Create an instance of HybridJoinThread
        HybridJoinThread hybridJoinThread = new HybridJoinThread(
                joinAttributeQueue,
                customerHashTable,
                diskBuffer,
                multiHashTable
        );
        try {

            // Start the thread
            hybridJoinThread.start();
            // The thread has finished its execution
            System.out.println("HybridJoinThread has finished execution.");
            // Wait for the thread to finish (optional)

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}