package com.yeswesail.rest.DBUtility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.yeswesail.rest.ApplicationProperties;

public class Users extends DBInterface
{	
	private static final long serialVersionUID = -4849479160608801245L;
	private final Logger log = Logger.getLogger(this.getClass());
	private static final ApplicationProperties prop = ApplicationProperties.getInstance();
	
	protected int idUsers;
	protected String name;
	protected String surname;
	protected int roleId;
	protected String email;
	// protected String password;
	protected String phone1;
	protected String phone2;
	protected String interests;
	protected int age;
	protected String facebook;
	protected String twitter;
	protected String google;
	protected String connectedVia;
	protected boolean isShipOwner;
	protected String languagesSpoken;
	protected String experiences;
	protected String status;
	protected String imageURL;
	protected Date birthday;
	protected String about;
	protected String sailingLicense;
	protected String navigationLicense;
	private AddressInfo personalInfo;
	private AddressInfo billingInfo;

	
	private void setNames()
	{
		tableName = "Users";
		idColName = "idUsers";
	}

	public Users() throws Exception
	{
		setNames();
	}

	public Users(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
		if (!getImageURL().startsWith("http"))
		{
			setImageURL(prop.getWebHost() + "/" + getImageURL());
		}
	}

	public Users(DBConnection conn, String email) throws Exception
	{
		setNames();
		findByEmail(conn, email);
	}

	public void findByEmail(DBConnection conn, String email) throws Exception
	{
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE email = '" + email + "'";
		this.populateObject(conn, sql, this);
		if (!getImageURL().startsWith("http"))
		{
			setImageURL(prop.getWebHost() + "/" + getImageURL());
		}
	}

	public void findByFacebookID(DBConnection conn, String id) throws Exception
	{
		String sql = "SELECT * " +
					 "FROM Users " +
					 "WHERE facebook = '" + id + "'";
		this.populateObject(conn, sql, this);
		if ((getImageURL() != null) && !getImageURL().startsWith("http"))
		{
			setImageURL(prop.getWebHost() + "/" + getImageURL());
		}
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Users> findUsersbyRole(int role) throws Exception
	{
		String sql = "SELECT name, surname, idUsers, imageURL " +
				 "FROM Users " +
				 "WHERE roleId = " + role;
		ArrayList<Users> retList = (ArrayList<Users>) populateCollection(sql, Users.class);
		for(Users u : retList)
		{
			if ((u.imageURL != null) && !u.getImageURL().startsWith("http"))
			{
				u.setImageURL(prop.getWebHost() + "/" + u.getImageURL());
			}
		}
		return retList;
	}
	
	public int getIdUsers() {
		return idUsers;
	}

	public void setIdUsers(int idUsers) {
		this.idUsers = idUsers;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

//	public String getPassword() {
//		return "*******";
//	}
//
//	public String getArchivedPassword() {
//		return password;
//	}
//
//	public void setPassword(String password) {
//		this.password = password;
//	}
//
	public String getPhone1() {
		return phone1;
	}

	public void setPhone1(String phone1) {
		this.phone1 = phone1;
	}

	public String getPhone2() {
		return phone2;
	}

	public void setPhone2(String phone2) {
		this.phone2 = phone2;
	}

	public String getInterests() {
		return interests;
	}

	public void setInterests(String interests) {
		this.interests = interests;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getFacebook() {
		return facebook;
	}

	public void setFacebook(String facebook) {
		this.facebook = facebook;
	}

	public String getTwitter() {
		return twitter;
	}

	public void setTwitter(String twitter) {
		this.twitter = twitter;
	}

	public String getGoogle() {
		return google;
	}

	public void setGoogle(String google) {
		this.google = google;
	}

	public String getConnectedVia() {
		return connectedVia;
	}

	public void setConnectedVia(String connectedVia) {
		this.connectedVia = connectedVia;
	}

	public boolean getIsShipOwner() {
		return isShipOwner;
	}

	public void setIsShipOwner(boolean isShipOwner) {
		this.isShipOwner = isShipOwner;
	}

	public String getLanguagesSpoken() {
		return languagesSpoken;
	}

	public void setLanguagesSpoken(String languagesSpoken) {
		this.languagesSpoken = languagesSpoken;
	}

	public String getExperiences() {
		return experiences;
	}

	public void setExperiences(String experiences) {
		this.experiences = experiences;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getImageURL() {
		if (imageURL == null)
		{
			return (prop.getWebHost() + "/images/default_icon.jpg");
		}
		else
		{
			return imageURL;
		}
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
	
	public boolean isAFakeEmail() {
		if (email == null)
		{
			return true;
		}
		if (email.startsWith("fake.") && email.endsWith("@yeswesail.com"))
		{
			return true;
		}
		return false;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	
	public void setBirthday(String birthday, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try 
		{
			this.birthday = sdf.parse(birthday);
		}
		catch (ParseException e) 
		{
			log.warn("Exception " + e.getMessage() + " convertind " + birthday + " to a date");
		}
	}

	public String getSailingLicense() {
		return sailingLicense;
	}

	public void setSailingLicense(String sailingLicense) {
		this.sailingLicense = sailingLicense;
	}

	public String getNavigationLicense() {
		return navigationLicense;
	}

	public void setNavigationLicense(String navigationLicense) {
		this.navigationLicense = navigationLicense;
	}

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public AddressInfo getBillingInfo() {
		return billingInfo;
	}

	public void setBillingInfo(AddressInfo billingInfo) {
		this.billingInfo = billingInfo;
	}

	public AddressInfo getPersonalInfo() {
		return personalInfo;
	}

	public void setPersonalInfo(AddressInfo personalInfo) {
		this.personalInfo = personalInfo;
	}
	
}
