package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;

// ---------------------------------------------------------------------
// Author: Elaine Gao
// For: CMSC 420
// Date: Fall 2022
//
// This is an implementation of a LeftistHeap data structure. This is a
// priority queue, which in addition to supporting insertion and extract
// min, it supports merging of two heaps.
// ---------------------------------------------------------------------


public class LeftistHeap<Key extends Comparable<Key>, Value> {

	// -----------------------------------------------------------------
	// Node in the tree
	// -----------------------------------------------------------------
	
	class LHNode {

		Key key;
		Value value;
		LHNode left, right;
		int npl;

		/*
		 * Basic constructor
		 */
		public LHNode(Key x, Value v) {
			key = x;
			value = v;
			left = null;
			right = null;
			npl = 0;
		}
	}

	// -----------------------------------------------------------------
	// Private members
	// -----------------------------------------------------------------

	private LHNode root; // the root value
	
	// -----------------------------------------------------------------
	// Local utilities
	// -----------------------------------------------------------------


	/*
	 * Basic Constructor
	 */
	public LeftistHeap() { 
		root = null;
	}
	
	/*
	 * Returns true if the current heap has no entries
	 */
	public boolean isEmpty() { 
		return root == null;
	}
	
	/*
	 * Resets the key to its initial state
	 */
	public void clear() { 
		root = null;
	}
	
	/*
	 * Inserts the key-value pair into the heap
	 */
	public void insert(Key x, Value v) { 
		root = merge((new LHNode(x,v)), root);
	}
	
	/*
	 * Merges the specific LHNodes u and v and returns the new LHNode
	 */
	private LHNode merge(LHNode u, LHNode v) {
		
		if (u == null) {
			return v;
		}
		if (v == null) {
			return u;
		}
		if (u.key.compareTo(v.key) > 0) {
			LHNode temp = u;
			u = v;
			v = temp;
		}
		if (u.left == null) {
			u.left = v;
		}
		else {
			u.right = merge(u.right, v);
			if (u.left.npl < u.right.npl) {
				LHNode temp = u.left;
				u.left = u.right;
				u.right = temp;
			}
			u.npl = u.right.npl + 1;
		}
		return u;
	}
	
	/*
	 * Merges the current heap with the 
	 */
	public void mergeWith(LeftistHeap<Key, Value> h2) { 
		if (h2 != null && this != h2) {
			root = merge(this.root, h2.root);
			h2.root = null;
		}
	}
	
	/*
	 * Finds the subtrees to unlink, dependent on the key-value
	 */
	private LHNode splitUnlink(Key x, LHNode u, ArrayList<LHNode> list) {
		if (u == null) {
			return null;
		}
		
		if (u.key.compareTo(x) > 0) {
			list.add(u);
			u = null;
		}
		else {
			u.left = splitUnlink(x, u.left, list);
			u.right = splitUnlink(x, u.right, list);
		}
		return u;
	}

	/*
	 * Manages the spliting process
	 */
	private LHNode splitOrder(LHNode u) {
		
		if (u == null) {
			return null;
		}
		if (u.left == null) {
			u.left = splitOrder(u.right);
			u.npl = 0;
			u.right = null;
		}
		else if (u.right == null) {
			u.left = splitOrder(u.left);
			u.npl = 0;
		}
		else {
			u.left = splitOrder(u.left);
			u.right = splitOrder(u.right);
			
			if (u.left.npl < u.right.npl) {
				LHNode temp = u.left;
				u.left = u.right;
				u.right = temp;
			}
			u.npl = u.right.npl + 1;
		}
		return u;
	}  
	
	/**
	 * Split this heap about x. Elements less than or equal to x remain
	 * in this heap, and larger elements are added to a second heap, which
	 * is returned.
	 */
	public LeftistHeap<Key, Value> split(Key x) {
		
		ArrayList<LHNode> unlinked = new ArrayList<LHNode>();
		root = splitUnlink(x, root, unlinked);
		
		LeftistHeap<Key, Value> h2 = new LeftistHeap<Key, Value>();
		
		for (int i = 0; i < unlinked.size(); i++) {
			h2.root = merge(h2.root, unlinked.get(i));
		}
		root = splitOrder(root);
		return h2;
	}
	
	/**
	 * Get the minimum key from the heap.
	 */
	public Key getMinKey() {
		if (isEmpty()) {
			return null;
		}
		return root.key;
	}
	
	/**
	 * Extract the minimum item from the heap.
	 */
	public Value extractMin() throws Exception {
		if (isEmpty()) {
			throw new Exception("Empty heap");
		}
		Value val = root.value;
		root = merge(this.root.left, this.root.right);
		return val;
	}
	
	/**
	 * The list() helperf function that adds all of the values 
	 * to the ArrayList via traversal
	 */
	private ArrayList<String> listHelp(LHNode u, ArrayList<String> list) {
		if (u == null) {
			list.add("[]");
		}
		else {
			list.add("(" + u.key + ", " + u.value + ") [" + u.npl + "]");
			listHelp(u.right, list);
			listHelp(u.left, list);
		}
		return list;
	}
	
	/**
	 * Get a list of entries in reverse (right-left) preorder.
	 */
	public ArrayList<String> list() { 
		ArrayList<String> list = new ArrayList<String>();
		return listHelp(root, list);
	}
}