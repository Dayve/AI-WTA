package model;

import model.TaggedPoint4D;

public class IrisData extends TaggedPoint4D {

	public static final int numberOfDistinctGroups = 3;
	public static final String[] groupNames = new String[] {"Iris-setosa", "Iris-versicolor", "Iris-virginica"};
	
	public IrisData(double[] rawData, String givenName) {
		super(rawData, givenName);
	}
}
