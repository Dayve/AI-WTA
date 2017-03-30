package model;

import org.apache.commons.math3.linear.ArrayRealVector;

public class TaggedPoint4D {
	
	protected ArrayRealVector vector;
	protected String name;
	
	public static final int numberOfDimensions = 4;
	
	
	public TaggedPoint4D(double[] rawData, String givenName) {
		this.vector = new ArrayRealVector(rawData);
		this.name = givenName;
	}
	
	
	public TaggedPoint4D(String givenName) {
		this.name = givenName;
		this.vector = new ArrayRealVector();
	}
	
	
	public TaggedPoint4D() {
		this.name = new String("Not assigned");
		this.vector = new ArrayRealVector();
	}
	
	
	public ArrayRealVector getVector() {
		return vector;
	}
	
	
	public void setVector(double[] doubleArray) {
		this.vector = new ArrayRealVector(doubleArray);
	}
	
	
	public String getName() {
		return name;
	}
	
	
	public void setName(String name) {
		this.name = name;
	}


	@Override
	public String toString() {
		return "Point4D [vector=" + vector + ", name=" + name + "]";
	}
}
