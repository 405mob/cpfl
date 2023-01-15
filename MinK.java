package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;
import java.util.Collections;

// ---------------------------------------------------------------------
// Author: Elaine Gao
// For: CMSC 420
// Date: Fall 2022
//
// This is an implementation of the MinK structure, which supports
// a key-value pair, with the key being the distance from the query
// point and the value being the point itself. 
// By also managing a binary heap, MinK is able to maintain only 
// a certain number of points closest to the query. 
// ---------------------------------------------------------------------

public class MinK<Key extends Comparable<Key>, Value> {

	// -----------------------------------------------------------------
	// Key Value Class
	// -----------------------------------------------------------------
	private class KeyValue {
		Key key; // distance from query point
		Value value; // Point 

		/*
		 * Basic Constructor
		 */
		public KeyValue(Key key, Value value) {
			this.key = key;
			this.value = value;
		}
	}

	// -----------------------------------------------------------------
	// Heap Structure
	// -----------------------------------------------------------------
	public class BinaryHeap {
		ArrayList<KeyValue> elements;

		/*
		 * Basic Constructor
		 */
		public BinaryHeap() {
			this.elements = new ArrayList<KeyValue>();
			elements.add(null);
		}

		/*
		 * Return the index of i's left child
		 */
		int getLeft(int i) {
			if ((2*i) <= k) {
				return (2*i);
			}
			return 0;
		}

		/*
		 * Return the index of i's right child
		 */
		int getRight(int i) {
			if ((2*i)+1 <= k) {
				return (2*i)+1;
			}
			return 0;
		}

		/*
		 * Return the index of i's parent
		 */
		int getParent(int i) {
			if ((i) >= 2) {
				return (i/2);
			}
			return 0;
		}

		/*
		 * Inserts a value into the heap, preserving a set amount
		 */
		void insert(KeyValue toAdd){
			if (elements.size()-1 < k) {
				elements.add(toAdd);
				int index = siftup(elements.size()-1, toAdd.key);
				elements.set(index, toAdd);
			}
			else {
				KeyValue root = elements.get(1);
				if (root.key.compareTo(toAdd.key) > 0) {
					elements.set(1, toAdd);
					int index = siftdown(1, toAdd.key);
					elements.set(index, toAdd);
				}
			}
		}

		/*
		 * "Sifts up" to maintain order
		 */
		int siftup(int i, Key x) {
			while (i > 1 && x.compareTo(elements.get(getParent(i)).key) > 0) {
				Collections.swap(elements, getParent(i), i);
				i = getParent(i);
			}
			return i;
		}

		/*
		 * "Sifts down" to maintain order
		 */
		int siftdown(int i, Key x) {
			while (getLeft(i) != 0) {
				int u = getLeft(i);
				int v = getRight(i);

				if (v!= 0 && (elements.get(v) != null) && ((elements.get(v).key).compareTo(elements.get(u).key) > 0)) {
					u = v;
				}
				if (elements.get(u).key.compareTo(x) > 0) {
					elements.set(i, elements.get(u));
					i = u;
				}
				else {
					break;
				}
			}
			return i;
		}


	}

	// -----------------------------------------------------------------
	// Class Members
	// -----------------------------------------------------------------

	int k;
	Key maxKey;
	BinaryHeap heap;

	/*
	 * Basic Constructor
	 */
	public MinK(int k, Key maxKey) {
		this.k = k;
		this.maxKey = maxKey;
		this.heap = new BinaryHeap();
	}

	/*
	 * Returns the current size of the heap
	 */
	public int size() { 
		return heap.elements.size()-1; 
	}

	/*
	 * Clears the Binary Heap
	 */
	public void clear() {
		heap = new BinaryHeap();
	}

	/*
	 * Returns the first value of the heap
	 */
	public Key getKth() {
		if (heap.elements.size()-1 == k) {
			return heap.elements.get(1).key;
		}
		return maxKey;
	}

	/*
	 * Adds the key, value pair to the heap
	 */
	public void add(Key x, Value v) { 
		heap.insert(new KeyValue(x, v));
	}

	/*
	 * returns arraylist of all values
	 */
	public ArrayList<Value> list() {
		ArrayList<Value> values = new ArrayList<>();
		ArrayList<KeyValue> sorted = new ArrayList<>(heap.elements.subList(1, heap.elements.size()));

		//sorts the values by the Key comparator
		sorted.sort((p1, p2) -> (p1.key.compareTo(p2.key)));
		for (KeyValue kv : sorted) {
			values.add(kv.value);
		}
		return values;
	}
	
}
