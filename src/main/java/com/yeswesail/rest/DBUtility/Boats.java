package com.yeswesail.rest.DBUtility;

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

}
