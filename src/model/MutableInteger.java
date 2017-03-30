package model;

public class MutableInteger {
	
	private int value;
	
	public MutableInteger(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public void increment() {
		this.value++;
	}
	
	public int compareTo(MutableInteger otherInt) {
		return (this.value < otherInt.value) ? -1 : (this.value > otherInt.value) ? 1 : 0;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
