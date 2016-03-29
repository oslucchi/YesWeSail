package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;

public class AddressInfo extends DBInterface
{	
	private static final long serialVersionUID = 2032712558768445807L;

	protected int idAddressInfo;
	protected int userId;
	protected char type;
	protected String companyName;
	protected String taxCode;
	protected String address1;
	protected String address2;
	protected String city;
	protected String zip;
	protected String province;
	protected String country;

	private void setNames()
	{
		tableName = "AddressInfo";
		idColName = "idAddressInfo";
	}

	public AddressInfo() throws Exception
	{
		setNames();
	}

	public AddressInfo(int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(sql, this);
	}

	public static AddressInfo[] findUserId(int userId) throws Exception
	{
		String sql = "SELECT * " +
				 "FROM AddressInfo " +
				 "WHERE  usersId = " + userId;
		@SuppressWarnings("unchecked")
		ArrayList<AddressInfo> adList = 
				(ArrayList<AddressInfo>) DBInterface.populateCollection(sql, AddressInfo.class);
		AddressInfo[] adi = (AddressInfo[]) adList.toArray();
		return(adi);
	}
	
	public int getIdAddressInfo() {
		return idAddressInfo;
	}

	public void setIdAddressInfo(int idAddressInfo) {
		this.idAddressInfo = idAddressInfo;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = type;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getTaxCode() {
		return taxCode;
	}

	public void setTaxCode(String taxCode) {
		this.taxCode = taxCode;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
}
