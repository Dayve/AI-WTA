package model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;


public class Neuron extends TaggedPoint4D {
	
	public static final String defaultName = "Neuron's weight vector";
	private Map<String, MutableInteger> responseStats = new HashMap<String, MutableInteger>();
	
	
	public Neuron(double[] rawData) {
		super(rawData, defaultName);
		
		for(int n=0 ; n<IrisData.numberOfDistinctGroups ; ++n) {
			responseStats.put(IrisData.groupNames[n], new MutableInteger(0));
		}
	}
	
	
	public Neuron() {
		super(defaultName);
		
		for(int n=0 ; n<IrisData.numberOfDistinctGroups ; ++n) {
			responseStats.put(IrisData.groupNames[n], new MutableInteger(0));
		}
		
		double[] uniformRandomDoubles = new double[numberOfDimensions];
		
		UniformRealDistribution uniformDist = new UniformRealDistribution(-1.0, 1.0);
		uniformRandomDoubles = uniformDist.sample(numberOfDimensions);
		
		vector = new ArrayRealVector(uniformRandomDoubles);
	}
	
	
	public Map<String, MutableInteger> getResponseStats() {
		return responseStats;
	}
	
	
	public void displayStats() {
		System.out.println("------- Neuron's stats:");
		for (Map.Entry<String, MutableInteger> entry : responseStats.entrySet()) {
		    System.out.println("Group: " + entry.getKey() + " -> number of best responses: " + entry.getValue());
		}
	}
	
	
	/**
	 * @return the name of the group for which this neuron responded
	 * the best of all the neurons in the network the most number of times
	 */
	public String getGroupNameForMostResponses() {
		Map.Entry<String, MutableInteger> maxEntry = null;

		for(Map.Entry<String, MutableInteger> entry : responseStats.entrySet()) {
		    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
		        maxEntry = entry;
		    }
		}
		
		return maxEntry.getKey();
	}
	

	public double neuronOutput(ArrayRealVector inputData) {
		double result = 0.0;
		
		for(int a=0 ; a<inputData.getDimension() ; ++a) {
			result += vector.getEntry(a)*inputData.getEntry(a);
		}
		
		// Returns the value of an activation function for 
		// matrix product of inputData vector and weightVector transposed
		return result;
	}
	
	
	public void updateWeightVector(double learningRate, ArrayRealVector inputData) {
		ArrayRealVector differenceVector = inputData.subtract(vector);
		differenceVector.mapMultiplyToSelf(learningRate);
		
		vector = vector.add(differenceVector);
	}
}

