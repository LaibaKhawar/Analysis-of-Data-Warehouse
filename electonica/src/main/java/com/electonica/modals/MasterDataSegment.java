package com.electonica.modals;

// MasterDataSegment.java
import java.util.List;

public class MasterDataSegment {
    private int index;  // Assuming index is the join attribute
    private List<MasterDataTuple> tuples;

    // Constructors, getters, and setters

    public MasterDataSegment(int index, List<MasterDataTuple> tuples) {
        this.index = index;
        this.tuples = tuples;
    }
    // Getters and setters

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<MasterDataTuple> getTuples() {
		return tuples;
	}

	public void setTuples(List<MasterDataTuple> tuples) {
		this.tuples = tuples;
	}

}
