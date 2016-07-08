package com.yeswesail.rest.DBUtility;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Boats extends DBInterface 
{
	private static final Logger log = Logger.getLogger(Boats.class);
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
	protected String insurance;
	protected String securityCertification;
	protected String RTFLicense;
	protected ArrayList<String> docs;
	protected ArrayList<String> images;

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
	public static Boats[] findAll(int languageId, int ownerId) throws Exception
	{
		String sql = "SELECT * " +
				 	 "FROM Boats " +
				 	 "WHERE ownerId = " + ownerId;
		
		log.trace("trying to populate collection with sql '" + sql + "'");
		boats = (ArrayList<Boats>) populateCollection(sql, Boats.class);
		log.trace("Done. There are " + boats.size() + " elemets");		
		return(boats.toArray(new Boats[boats.size()]));
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
}
