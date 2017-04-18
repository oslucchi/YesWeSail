package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;

public class Boats extends DBInterface 
{
	private static final long serialVersionUID = 7805943614787085014L;
	protected int idBoats;
	protected int ownerId;
	protected String engineType;
	protected String plate;
	protected String name;
	protected String model;
	protected int length;
	protected int year;
	protected int cabinsWithBathroom;
	protected int cabinsNoBathroom;
	protected int sharedBathrooms;
	protected int bunks;
	protected int wholeBoat = 1;
	protected int cabinsVIP;
	protected String insurance;
	protected String securityCertification;
	protected String RTFLicense;
	protected ArrayList<String> docs;
	protected ArrayList<String> images;
	protected ArrayList<String> imagesSmall;
	protected ArrayList<String> imagesMedium;
	protected ArrayList<String> imagesLarge;

	private static ArrayList<Boats> boats;

	private void setNames()
	{
		tableName = "Boats";
		idColName = "idBoats";
	}

	public Boats() throws Exception
	{
		setNames();
	}

	public Boats(DBConnection conn, int id) throws Exception
	{
		setNames();
		String sql = "SELECT * " +
					 "FROM " + tableName + " " +
					 "WHERE " + idColName + " = " + id;
		this.populateObject(conn, sql, this);
	}

	@SuppressWarnings("unchecked")
	public static Boats[] findAll(DBConnection conn, int languageId, int ownerId) throws Exception
	{
		String sql = "SELECT * " +
				 	 "FROM Boats " +
				 	 "WHERE ownerId = " + ownerId;
		
		boats = (ArrayList<Boats>) populateCollection(conn, sql, Boats.class);
		return(boats.toArray(new Boats[boats.size()]));
	}

	public static boolean userHasBoats(int ownerId)
	{
		String sql = "SELECT * " +
				 	 "FROM Boats " +
				 	 "WHERE ownerId = " + ownerId;
		DBConnection conn = null;
		try
		{
			conn = DBInterface.connect();
			if (populateCollection(conn, sql, Boats.class).isEmpty())
				return(false);
			else
				return(true);
		}
		catch(Exception e)
		{
			return(false);
		}
		finally
		{
			DBInterface.disconnect(conn);
		}
	}
	
	public int getIdBoats() {
		return idBoats;
	}

	public void setIdBoats(int idBoats) {
		this.idBoats = idBoats;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	public String getEngineType() {
		return engineType;
	}

	public void setEngineType(String engineType) {
		this.engineType = engineType;
	}

	public String getPlate() {
		return plate;
	}

	public void setPlate(String plate) {
		this.plate = plate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getCabinsWithBathroom() {
		return cabinsWithBathroom;
	}

	public void setCabinsWithBathroom(int cabinsWithBathroom) {
		this.cabinsWithBathroom = cabinsWithBathroom;
	}

	public int getCabinsNoBathroom() {
		return cabinsNoBathroom;
	}

	public void setCabinsNoBathroom(int cabinsNoBathroom) {
		this.cabinsNoBathroom = cabinsNoBathroom;
	}

	public int getSharedBathrooms() {
		return sharedBathrooms;
	}

	public void setSharedBathrooms(int sharedBathrooms) {
		this.sharedBathrooms = sharedBathrooms;
	}

	public int getBunks() {
		return bunks;
	}

	public void setBunks(int bunks) {
		this.bunks = bunks;
	}

	public void setImages(ArrayList<String> images)
	{
		this.images = images;
	}
	
	public ArrayList<String> getImages()
	{
		return images;
	}

	public String getInsurance() {
		return insurance;
	}

	public void setInsurance(String insurance) {
		this.insurance = insurance;
	}

	public String getSecurityCertification() {
		return securityCertification;
	}

	public void setSecurityCertification(String securityCertification) {
		this.securityCertification = securityCertification;
	}

	public String getRTFLicense() {
		return RTFLicense;
	}

	public void setRTFLicense(String RTFLicense) {
		this.RTFLicense = RTFLicense;
	}

	public ArrayList<String> getDocs() {
		return docs;
	}

	public void setDocs(ArrayList<String> docs) {
		this.docs = docs;
	}

	public ArrayList<String> getImagesSmall() {
		return imagesSmall;
	}

	public void setImagesSmall(ArrayList<String> imagesSmall) {
		this.imagesSmall = imagesSmall;
	}

	public ArrayList<String> getImagesMedium() {
		return imagesMedium;
	}

	public void setImagesMedium(ArrayList<String> imagesMedium) {
		this.imagesMedium = imagesMedium;
	}

	public ArrayList<String> getImagesLarge() {
		return imagesLarge;
	}

	public void setImagesLarge(ArrayList<String> imagesLarge) {
		this.imagesLarge = imagesLarge;
	}

	public int getCabinsVIP() {
		return cabinsVIP;
	}

	public void setCabinsVIP(int cabinsVIP) {
		this.cabinsVIP = cabinsVIP;
	}

	public int getWholeBoat() {
		return wholeBoat;
	}
	
}
