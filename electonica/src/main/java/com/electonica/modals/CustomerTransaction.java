package com.electonica.modals;

import java.util.Date;

// CustomerTransaction.java
public class CustomerTransaction {
    private int id;
    private Date orderDate;
    private int productID;
    private int customerID;
    private String customerName;
    private String gender;
    private int quantityOrdered;


	public CustomerTransaction(int id, Date orderDate2, int productID, int customerID, String customerName,
			String gender, int quantityOrdered) {
		this.id = id;
		this.orderDate = orderDate2;
		this.productID = productID;
		this.customerID = customerID;
		this.customerName = customerName;
		this.gender = gender;
		this.quantityOrdered = quantityOrdered;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public int getProductID() {
		return productID;
	}

	public void setProductID(int productID) {
		this.productID = productID;
	}

	public int getCustomerID() {
		return customerID;
	}

	public void setCustomerID(int customerID) {
		this.customerID = customerID;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public int getQuantityOrdered() {
		return quantityOrdered;
	}

	public void setQuantityOrdered(int quantityOrdered) {
		this.quantityOrdered = quantityOrdered;
	}


}
