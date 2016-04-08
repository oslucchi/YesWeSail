package com.yeswesail.rest.DBUtility;

public class Users extends DBInterface
{	
	private static final long serialVersionUID = -4849479160608801245L;
	
	protected int idUsers;
	protected String name;
	protected String surname;
	protected int roleId;
	protected String email;
	protected String password;
	protected String phone1;
	protected String phone2;
	protected String selfComments;
	protected int age;
	protected String facebook;
	protected String twitter;
	protected String google;
	protected String connectedVia;
	protected String isShipOwner;
	protected String languagesSpoken;
	protected String experiences;
	protected String status;
	protected String imageURL;
	
	private void setNames()
	{
		tableName = "Users";
		idColName = "idUsers";
	}

	public Users() throws Exception
	{
		setNames();
	}

	public Users(int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(sql, this);
	}

	public Users(String email) throws Exception
	{
		findByEmail(email);
	}

	public void findByEmail(String email) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE email = '" + email + "'";
		this.populateObject(sql, this);
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

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

	public String getSelfComments() {
		return selfComments;
	}

	public void setSelfComments(String selfComments) {
		this.selfComments = selfComments;
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

	public String getIsShipOwner() {
		return isShipOwner;
	}

	public void setIsShipOwner(String isShipOwner) {
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
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
}
