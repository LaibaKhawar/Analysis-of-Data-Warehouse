package com.electonica.modals;
// MasterDataTuple.java
public class MasterDataTuple {
    private int id;  // Assuming id is the primary key
    private String productName;
    private double productPrice;
    private int supplierID;
    private String supplierName;
    private int storeID;  // Add storeID field
    // Constructors, getters, and setters

    public MasterDataTuple(int id, String productName, double productPrice, int supplierID, String supplierName) {
        this.id = id;
        this.productName = productName;
        this.productPrice = productPrice;
        this.supplierID = supplierID;
        this.supplierName = supplierName;
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public double getProductPrice() {
		return productPrice;
	}

	public void setProductPrice(double productPrice) {
		this.productPrice = productPrice;
	}

	public int getSupplierID() {
		return supplierID;
	}

	public void setSupplierID(int supplierID) {
		this.supplierID = supplierID;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public int getStoreID() {
		return storeID;
	}

	public void setStoreID(int storeID) {
		this.storeID = storeID;
	}


}
