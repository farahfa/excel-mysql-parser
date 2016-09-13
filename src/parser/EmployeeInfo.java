package parser;

public class EmployeeInfo {
	// Representation of my "MySQL Table"
	private static int Id;
	private static int generatedKey;
	private static String FirstName;
	private static String LastName;
	private static double Hours;
	private static double Rate;
	private static double TotalCost;
	private static double TotalCostAllEmployees;
	
	// Auto-Generated Getters And Setters
	public static int getId() {
		return Id;
	}
	public static void setId(int id) {
		Id = id;
	}
	public static int getGeneratedKey() {
		return generatedKey;
	}
	public static void setGeneratedKey(int generatedKey) {
		EmployeeInfo.generatedKey = generatedKey;
	}
	public static String getFirstName() {
		return FirstName;
	}
	public static void setFirstName(String firstName) {
		FirstName = firstName;
	}
	public static String getLastName() {
		return LastName;
	}
	public static void setLastName(String lastName) {
		LastName = lastName;
	}
	public static double getHours() {
		return Hours;
	}
	public static void setHours(double hours) {
		Hours = hours;
	}
	public static double getRate() {
		return Rate;
	}
	public static void setRate(double rate) {
		Rate = rate;
	}
	public static double getTotalCost() {
		return TotalCost;
	}
	public static void setTotalCost(double totalCost) {
		TotalCost = totalCost;
	}
	public static double getTotalCostAllEmployees() {
		return TotalCostAllEmployees;
	}
	public static void setTotalCostAllEmployees(double totalCostAllEmployees) {
		TotalCostAllEmployees = totalCostAllEmployees;
	}
}
