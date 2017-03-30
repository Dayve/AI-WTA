package application;

import model.IrisData;
import model.Neuron;
import model.TaggedPoint4D;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class Main {
	
	private static Set<TaggedPoint4D> wholeDataset = new HashSet<TaggedPoint4D>();
	
	private static Set<TaggedPoint4D> trainingSet = new HashSet<TaggedPoint4D>();
	private static Set<TaggedPoint4D> testingSet = new HashSet<TaggedPoint4D>();
	
	private static int networkSize = 30;
	private static int nOfIterationsOverTrainingSet = 120;
	private static double learningRate = 0.12;
	private static double percentForTesting = 0.08;
	
	private static List<Neuron> neuralNetwork = new ArrayList<Neuron>();
	
	
	private static XYDataset createXYDatasetFromList(Set<TaggedPoint4D> givenDataList) {
	    Map<String, XYSeries> seriesMap = new HashMap<String, XYSeries>();
	    
	    for(int g=0 ; g<IrisData.numberOfDistinctGroups ; ++g) {
	    	seriesMap.put(IrisData.groupNames[g], new XYSeries(IrisData.groupNames[g]));
	    }
	    seriesMap.put(Neuron.defaultName, new XYSeries(Neuron.defaultName));
   
	    for(TaggedPoint4D data : givenDataList) {
	    	// Dimensions 3 and 4 (2 and 3 if we count from zero) are highly correlated with the group membership
	    	seriesMap.get(data.getName()).add(data.getVector().getEntry(2), data.getVector().getEntry(3));
	    }
	    
	    XYSeriesCollection result = new XYSeriesCollection();
	    
	    for(XYSeries series : seriesMap.values()) {
	    	result.addSeries(series);
	    }
	    
	    return result;
	}
	
	
	private static void normalizeDataset(Set<TaggedPoint4D> forNormalizing) {
		// Determine maximums and minimums for normalization:  --------------------------
		double[] maximums = new double[TaggedPoint4D.numberOfDimensions], 
				 minimums = new double[TaggedPoint4D.numberOfDimensions];
		
		Arrays.fill(maximums, 0.0);
		Arrays.fill(minimums, Double.MAX_VALUE);

		for(TaggedPoint4D data : forNormalizing) {
			for(int i=0 ; i<TaggedPoint4D.numberOfDimensions ; ++i) {
				if(data.getVector().getEntry(i) > maximums[i]) maximums[i] = data.getVector().getEntry(i);
				if(data.getVector().getEntry(i) < minimums[i]) minimums[i] = data.getVector().getEntry(i);
			}
		}
		
		// Normalize the whole dataset: -------------------------------------------------
		for(TaggedPoint4D data : forNormalizing) {
			double[] dataVector = data.getVector().toArray();
			
			for(int k=0 ; k<data.getVector().getDimension() ; ++k) {
				dataVector[k] = normalize(dataVector[k], -1.0, 1.0, minimums[k], maximums[k]);
			}
			
			data.setVector(dataVector);
		}
	}
	
	
	private static void showPlotOfXYSeriesCollection(String plotTitle, String frameTitle, XYDataset dataset) {
	    JFreeChart chart = ChartFactory.createScatterPlot(plotTitle, "X", "Y", dataset, PlotOrientation.VERTICAL ,true, true, false);

	    ChartFrame frame = new ChartFrame(frameTitle, chart);
	    frame.pack();
	    frame.setVisible(true);
	}
	
	
	public static double normalize(double value, double minOutput, double maxOutput, double minValue, double maxValue) {
		return (maxOutput-minOutput)/(maxValue-minValue) * (value - maxValue) + maxOutput;
	}
	
	
	private static void loadCSV(String CSVFilePath, Set<TaggedPoint4D> data) {
		System.out.println("\n> Parsing a CSV file: (" + CSVFilePath + ")");
		File csvDataFile = new File(CSVFilePath);
		
		try {
			CSVParser parser = CSVParser.parse(csvDataFile, StandardCharsets.UTF_8, CSVFormat.newFormat(','));
			
			for (CSVRecord csvRecord : parser.getRecords()) {
				double[] partialData = new double[csvRecord.size()-1];
				
				for(int i=0 ; i<csvRecord.size()-1 ; ++i) {
					partialData[i] = Double.parseDouble(csvRecord.get(i));
				}
				
				String groupName = csvRecord.get(csvRecord.size()-1);
				
				data.add(new TaggedPoint4D(partialData, groupName));
			}
			
			System.out.println("  > Data loaded successfully\n  > Number of records: " + data.size());
		}
		catch (IOException e) {
			System.out.println("  > A problem occured while parsing a CSV file.");
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		// Get the parameters: ------------------------------------------------------------------------------
		System.out.print("Do you want to run with the default parameters?"
				+ "\n -> Network size: 30\n -> Training set iterations: 120\n -> Learning rate: 0.12\n -> Percent of testing examples: 8%"
				+ "\nInsert: [d]efault / [m]anually choose the values"
				+ "\n -> ");
		
		Scanner reader = new Scanner(System.in);
		reader.useLocale(Locale.US);
		
		if(reader.nextLine().equals("m")) {
			System.out.print("Choose the size of the network: ");
			networkSize = reader.nextInt();
	
			System.out.print("Choose the number of iterations over the training set: ");
			nOfIterationsOverTrainingSet = reader.nextInt();
			
			System.out.print("Choose the learning rate value (0.01 to 0.50, use a dot): ");
			learningRate = reader.nextDouble();
			
			if(learningRate < 0.01) learningRate = 0.01;
			else if(learningRate > 0.5)  learningRate = 0.50;
			
			System.out.print("Choose the fraction (0.10 to 0.50) of testing examples (use a dot): ");
			percentForTesting = reader.nextDouble();
			
			if(percentForTesting < 0.1) percentForTesting = 0.1;
			else if(percentForTesting > 0.5)  percentForTesting = 0.5;
		}
		
		reader.close();
		
		
		// Load data from CSV to a list and normalize it: ---------------------------------------------------
		String currPath = System.getProperty("user.dir");		
		loadCSV(currPath + "/data/iris.data", wholeDataset);
		
		normalizeDataset(wholeDataset);
		
		
		// Initialize the network and show the initial coordinates: -----------------------------------------
		for(int i=0 ; i<networkSize ; ++i) {
			neuralNetwork.add(new Neuron());
		}
		
		// List of both neurons' weight vectors and data vectors:
		Set<TaggedPoint4D> weightAndDataVectors = Stream.concat(wholeDataset.stream(), neuralNetwork.stream()).collect(Collectors.toSet());
		
		showPlotOfXYSeriesCollection("Normalized iris dataset and weight vectors", "Initial state [SI Lab 1]", createXYDatasetFromList(weightAndDataVectors));
		
		
		// Partition the whole dataset into training and testing sets:  -------------------------------------
		long howManyForTesting = Math.round(percentForTesting*wholeDataset.size()), counter = 0;
		
		for(TaggedPoint4D data : wholeDataset) {
			if(counter < howManyForTesting) testingSet.add(data);
			else trainingSet.add(data);
			
			counter++;
		}

		// Show the results of partitioning:
		System.out.println("> Sizes:");
		System.out.println("  > Training set  | " + trainingSet.size());
		System.out.println("  > Testing set   | " + testingSet.size());
		System.out.println("  > Whole dataset | " + wholeDataset.size() + "\n");
		
		
		// Train the neural network:  -----------------------------------------------------------------------
		for(int r=0 ; r<nOfIterationsOverTrainingSet ; ++r) {
			
			for(TaggedPoint4D data : trainingSet) {
				
				double bestResponseValue = -Double.MAX_VALUE;
				Neuron bestRespondingNeuron = null;
				
				for(Neuron neuron : neuralNetwork) {
					double currentNeuronsResponse = neuron.neuronOutput(data.getVector());
					
					if(currentNeuronsResponse > bestResponseValue) {
						bestResponseValue = currentNeuronsResponse;
						bestRespondingNeuron = neuron;
					}
				}
				
				bestRespondingNeuron.getResponseStats().get(data.getName()).increment();
				bestRespondingNeuron.updateWeightVector(learningRate, data.getVector());			
			}
		}
		
		showPlotOfXYSeriesCollection("Normalized iris dataset and weight vectors", "Final state [SI Lab 1]", createXYDatasetFromList(weightAndDataVectors));
	
		/*
		for(Neuron neuron : neuralNetwork) {
			neuron.displayStats();
			System.out.println("Conclusion (best fit group): " + neuron.getGroupNameForMostResponses() + "\n");
		}
		*/
		
		
		// Test the neural network:  ------------------------------------------------------------------------
		int numberOfGoodAnswers = 0;
		
		System.out.format("%1$16s | %2$16s\n", "Neuron's answer", "Dataset");
		System.out.println("-----------------+------------------");
		
		for(TaggedPoint4D data : testingSet) {
			
			double bestResponseValue = -Double.MAX_VALUE;
			Neuron bestRespondingNeuron = null;
			
			for(Neuron neuron : neuralNetwork) {
				double currentNeuronsResponse = neuron.neuronOutput(data.getVector());
				
				if(currentNeuronsResponse > bestResponseValue) {
					bestResponseValue = currentNeuronsResponse;
					bestRespondingNeuron = neuron;
				}
			}
			
			//System.out.println(bestRespondingNeuron.getGroupNameForMostResponses() + " | " + data.getName());
			System.out.format("%1$16s | %2$16s\n", bestRespondingNeuron.getGroupNameForMostResponses(), data.getName());
			
			if(bestRespondingNeuron.getGroupNameForMostResponses().equals(data.getName())) {
				numberOfGoodAnswers++;
			}
		}
		
		System.out.println("-----------------+------------------\n");
		System.out.println("Correct answers: " + numberOfGoodAnswers + " / " + testingSet.size() +
			" (" + String.format("%.2f", 100.0*numberOfGoodAnswers/testingSet.size()) + " %)");
	}
	
}



