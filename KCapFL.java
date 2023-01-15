package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;

//---------------------------------------------------------------------
// Author: Elaine Gao
// For: CMSC 420
// Date: Fall 2022
//
// This is an implementation of a k-capacitates facility locator, which
// follows the greedy facility location algorithm. This utilizes the 
// previously written LeftistHeap and XkdTree classes. 
//---------------------------------------------------------------------

public class KCapFL<LPoint extends LabeledPoint2D> {

	// -----------------------------------------------------------------
	// Class Members
	// -----------------------------------------------------------------

	int capacity;
	XkdTree<LPoint> kdTree;
	LeftistHeap<Double, ArrayList<LPoint>> heap;
	
	/*
	 * Basic Constructor
	 */
	public KCapFL(int capacity, int bucketSize, Rectangle2D bbox) {
		this.capacity = capacity;
		this.kdTree = new XkdTree<>(bucketSize, bbox);
		this.heap = new LeftistHeap<>();
	}

	/*
	 * clears the data structure
	 */
	public void clear() {
		kdTree.clear();
		heap.clear();
	}

	/*
	 * initializes the structure by calling the insert functions of 
	 * both the kdTree and the heap
	 */
	public void build(ArrayList<LPoint> pts) throws Exception {
		if(pts.size() <= 0 || (pts.size()%capacity != 0)) {
			throw new Exception("Invalid point set size");
		}
		kdTree.bulkInsert(pts);
		for (LPoint p : pts) {
			ArrayList<LPoint> nearest = kdTree.kNearestNeighbor(p.getPoint2D(), capacity);
			double r = p.getPoint2D().distanceSq(nearest.get(capacity-1).getPoint2D());
			heap.insert(r, nearest);
		}
	}

	/*
	 * Performs one step of the greedy algorithm and attempts to
	 * find a cluster
	 */
	public ArrayList<LPoint> extractCluster() {
		if (kdTree.size() == 0) {
			return null;
		}
		try {
			boolean found = false;
			while(found == false) {
				double ri = heap.getMinKey();
				ArrayList<LPoint> Li = heap.extractMin();
				for (LPoint p : Li) {
					if (kdTree.find(p.getPoint2D()) == null) {
						found = false;
						break;
					}
					else {
						found = true;
					}
				}
				if (found == true) {
					for (LPoint p : Li) {
						kdTree.delete(p.getPoint2D());
					}
					return Li;
				}
				else {
					LPoint ci = Li.get(0);
					if (kdTree.find(ci.getPoint2D()) != null) {
						ArrayList<LPoint> newnearest = kdTree.kNearestNeighbor(ci.getPoint2D(), capacity);
						double newr = ci.getPoint2D().distanceSq(newnearest.get(capacity-1).getPoint2D());
				
						heap.insert(newr, newnearest);
					}
				}
			}

		}catch(Exception e) {
			return null;
		}
		return null;
	}

	/*
	 * Returns the KdTree
	 */
	public ArrayList<String> listKdTree() {
		return kdTree.list();
	}

	/*
	 * Returns the Heap
	 */
	public ArrayList<String> listHeap() {
		return heap.list();
	}
}
