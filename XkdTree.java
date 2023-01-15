package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;
import java.util.*;

// ---------------------------------------------------------------------
// Author: Elaine Gao
// For: CMSC 420
// Date: Fall 2022
//
// This is an implementation of a variant of the kd-tree data structure,
// called an XKdTree. The XkdTree has both internal nodes and external 
// nodes (leaves), storing a number of points dependent on a bucket size.
// Extending off of the previous project, this version now manages
// deleting points and returning a specific number of closest points.
// ---------------------------------------------------------------------

public class XkdTree<LPoint extends LabeledPoint2D> {

	// -----------------------------------------------------------------
	// Class Members
	// -----------------------------------------------------------------
	
	int bucketSize;
	Rectangle2D bbox;
	int size;
	Node root;
	
	// -----------------------------------------------------------------
	// Comparators
	// -----------------------------------------------------------------
	
	/*
	 *  Compares pt1 and pt2 lexicographically by X and then Y
	 */
	private class ByXThenY implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			double oneX = pt1.getX();
			double twoX = pt2.getX();

			if (oneX == twoX) {
				double oneY = pt1.getY();
				double twoY = pt2.getY();
				return Double.compare(oneY, twoY);
			}
			return Double.compare(oneX, twoX);
		}
	}
	/*
	 *  Compares pt1 and pt2 lexicographically by Y and then X
	 */
	private class ByYThenX implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			double oneY = pt1.getY();
			double twoY = pt2.getY();

			if (oneY == twoY) {
				double oneX = pt1.getX();
				double twoX = pt2.getX();
				return Double.compare(oneX, twoX);
			}
			return Double.compare(oneY, twoY);
		}
	}
	/*
	 *  Compares pt1 and pt2 alphabetically by the label name
	 */
	private class ByName implements Comparator<LPoint> {
		public int compare(LPoint pt1, LPoint pt2) {
			String one = pt1.getLabel();
			String two = pt2.getLabel();
			return one.compareTo(two);
		}
	}
	
	// -----------------------------------------------------------------
	// Generic Node in Tree
	// -----------------------------------------------------------------

	private abstract class Node { // generic node (purely abstract)
		abstract LPoint find(Point2D pt); // find helper - abstract
		abstract Node bulkInsert(ArrayList<LPoint> pts);
		abstract void list(ArrayList<String> res);
		abstract LPoint nearestNeighbor (Point2D center, Rectangle2D cell, LPoint best);
		
		abstract Node delete(Point2D pt) throws Exception;
		abstract void kNearestNeighbor(Point2D q, int k, Rectangle2D cell, MinK<Double, LPoint> best);
	}	
	
	// -----------------------------------------------------------------
	// Internal Node
	// -----------------------------------------------------------------
	
	private class InternalNode extends Node {
		int cutDim; // the cutting dimension (0 = x, 1 = y)
		double cutVal; // the cutting value
		Node left, right; // children
		
		/*
		 * Goes to the left/right subtree depending on the cutting value and the point's
		 * value at the cut dimension. If the two are the same, checks and compares both
		 */
		LPoint find(Point2D pt) { 
			if (pt.get(cutDim) > cutVal) {
				return right.find(pt);
			}
			// go to the left
			else if (pt.get(cutDim) < cutVal) {
				return left.find(pt);
			}
			else {
				LPoint leftval = left.find(pt);
				if (leftval == null) {
					return right.find(pt);
				}
				return leftval;
			}
		}
		
		/*
		 * Splits the points to the left and right depending on the cutting value,
		 * and inserts them into the according subtree
		 */
		Node bulkInsert(ArrayList<LPoint> pts) {
			ArrayList<LPoint> leftlist = new ArrayList<LPoint>();
			ArrayList<LPoint> rightlist = new ArrayList<LPoint>();
			
			for (LPoint p : pts) {
				if (p.get(cutDim) >= cutVal) {
					rightlist.add(p);
				}
				// go to the left
				else {
					leftlist.add(p);
				}
			}
			left = left.bulkInsert(leftlist);
			right = right.bulkInsert(rightlist);
			return this;
		}

		/*
		 * Lists the current cutting dimension/value and continues down a 
		 * right-to-left preorder traversal
		 */
		void list(ArrayList<String> res) {
			if (cutDim == 0) {
				res.add("(x=" + cutVal + ")");
			}
			else {
				res.add("(y=" + cutVal + ")");
			}
			right.list(res);
			left.list(res);
		}
		
		/*
		 * Splits the cell into left and right, goes down the left/right subtree with the 
		 * updated cell depending on the cutting value and updates the best value. 
		 * If the right distance is less than the best, the best gets updated to the right value.
		 */
		LPoint nearestNeighbor (Point2D center, Rectangle2D cell, LPoint best) {
			
			Rectangle2D leftcell = cell.leftPart(cutDim, cutVal);
			Rectangle2D rightcell = cell.rightPart(cutDim, cutVal);
			
			if (center.get(cutDim) < cutVal) {
				best = left.nearestNeighbor(center, leftcell, best);
				if (rightcell.distanceSq(center) < center.distanceSq(best.getPoint2D())) {
					best = right.nearestNeighbor(center, rightcell, best);
				}
			}
			else {
				best = right.nearestNeighbor(center, rightcell, best);
				if (leftcell.distanceSq(center) < center.distanceSq(best.getPoint2D())) {
					best = left.nearestNeighbor(center, leftcell, best);
				}
			}
			return best;
		}
		

		boolean onLeft(Point2D pt) { // in the left subtree? (for points)
			return pt.get(cutDim) <= cutVal;
		}

		boolean onRight(Point2D pt) { // in the left subtree? (for points)
			return pt.get(cutDim) >= cutVal;
		}

		/*
		 * Updates the left and right values after deleting the nodes.
		 * If the child has been completely deleted, the children shift accordingly
		 */
		Node delete(Point2D pt) throws Exception {

			LPoint result = null;
			if (onLeft(pt)) {
				result = left.find(pt);
				if (result != null) {
					this.left = left.delete(pt);
				}
			}
			if (result == null && onRight(pt)) {
				result = right.find(pt);
				if (result != null) {
					this.right = right.delete(pt);
				}
			}
			if (result == null) {
				throw new Exception("Deletion of nonexistent point");
			}

			// in case the child gets deleted
			if (left == null) {
				return right;
			}
			else if (right == null) {
				return left;
			}
			else {
				return this;
			}
		}
		
		/*
		 * Splits the cell into left and right, goes down the left/right subtree with the 
		 * updated cell depending on the cutting value and updates the best MinK structure
		 */
		void kNearestNeighbor(Point2D q, int k, Rectangle2D cell, MinK<Double, LPoint> best) {

			Rectangle2D leftcell = cell.leftPart(cutDim, cutVal);
			Rectangle2D rightcell = cell.rightPart(cutDim, cutVal);
			
			if (best.getKth().compareTo(cell.distanceSq(q)) < 0) {
				return;
			}

			if (q.get(cutDim) < cutVal) {
				this.left.kNearestNeighbor(q, k, leftcell, best);
				this.right.kNearestNeighbor(q, k, rightcell, best);
			}
			else {
				this.right.kNearestNeighbor(q, k, rightcell, best);
				this.left.kNearestNeighbor(q, k, leftcell, best);
			}
		}

		/*
		 * Basic Constructor
		 */
		public InternalNode(int cutDim, double cutVal, Node left, Node right){
			this.cutDim = cutDim;
			this.cutVal = cutVal;
			this.left = left;
			this.right = right;
		}

		
	}
	
	// -----------------------------------------------------------------
	// External Node
	// -----------------------------------------------------------------

	private class ExternalNode extends Node {
		ArrayList<LPoint> points; // the bucket

		/*
		 * Returns the point if it is found and null otherwise 
		 */
		LPoint find(Point2D pt) {
			for (LPoint p : points){
				if (p.getPoint2D().equals(pt)) {
					return p;
				}
			}
			return null;
		}
		
		/*
		 * Adds all the values to the bucket and sorts the points alphabetically. 
		 * If the new bucket exceeds the bucket size, a new internal node is created after
		 * calculating the new cut dimension/cut value. The new bucket is split into left 
		 * and right, and new nodes are created/rechecked accordingly.
		 */
		Node bulkInsert(ArrayList<LPoint> pts) {
			// add all values to the bucket
			points.addAll(pts);
			Collections.sort(points, new ByName());
			
			if (points.size() > bucketSize) {
				// create the bounding box with all the points
				Rectangle2D boundingbox = new Rectangle2D();
				for (int i = 0; i < points.size(); i++) {
					boundingbox.expand(points.get(i).getPoint2D());
				}
				
				int split = 1;
				// taller, so change split to the y axis (1)
				if (boundingbox.getWidth(0) >= boundingbox.getWidth(1)) {
					split = 0;
				}
				
				// compare against x
				if (split == 0) {
					Collections.sort(points, new ByXThenY());
				}
				// compare against y
				else {
					Collections.sort(points, new ByYThenX());
				}

				int m = points.size()/2;
				double cutval = 0;
				// odd number of items
				if (points.size() % 2 != 0) {
					cutval = points.get(m).get(split);
				}
				// even number of items
				else {
					cutval = (points.get(m-1).get(split) + points.get(m).get(split));
					cutval /= 2;
				}

				ArrayList<LPoint> leftlist = new ArrayList<LPoint>();
				ArrayList<LPoint> rightlist = new ArrayList<LPoint>();
				for (int i = 0; i < m; i++) {
					leftlist.add(points.get(i));
				}
				for (int i = m; i < points.size(); i++) {
					rightlist.add(points.get(i));
				}
				
				// create new internal node
				Node left = new ExternalNode();
				Node right = new ExternalNode();
				left = left.bulkInsert(leftlist);
				right = right.bulkInsert(rightlist);
				
				InternalNode update = new InternalNode(split, cutval, left, right);
				return update;
			}
			return this;
		}
		
		/*
		 * Lists all the values in the bucket
		 */
		void list(ArrayList<String> res) {
			String ret = "[ ";
			for (LPoint p: points) {
				String curr = "{" + p.toString() + "} ";
				ret += curr;
			}
			ret += "]";
			res.add(ret);
		}
		
		/*
		 * Finds and returns the nearest point in the bucket 
		 */
		LPoint nearestNeighbor (Point2D center, Rectangle2D cell, LPoint best) {
			for (LPoint p: points) {
				if (p != null) {
					if (best == null) {
						best = p;
					}
					else if (center.distance(p.getPoint2D()) < center.distance(best.getPoint2D())) {
						best = p;
					}
				}
			}
			return best;
		}
		
		/*
		 * Goes through the bucket to find and delete the point sent in
		 * If deleting the point emptys the bucket, returns null as representation
		 */
		Node delete(Point2D pt) throws Exception {
			// iterate through all the points in the bucket
			for (LPoint p: points) {
				if (p.getPoint2D().equals(pt)) {
					points.remove(p);
					if (points.isEmpty()) {
						if (root.equals(this)) {
							return this;
						}
						else {
							return null;
						}
					}
					else {
						return this;
					}
				}
			}
			throw new Exception("Deletion of nonexistent point");
		}

		/*
		 * Creates the current best MinK structure for the current bucket
		 */
		void kNearestNeighbor(Point2D q, int k, Rectangle2D cell, MinK<Double, LPoint> best) {
			for (LPoint p : points) {
				best.add(q.distanceSq(p.getPoint2D()), p);
			}
		}

		// External Node constructor
		public ExternalNode(){
			points = new ArrayList<LPoint>();
		}

	}
	
	/*
	 * XkdTree Constructor
	 */
	public XkdTree(int bucketSize, Rectangle2D bbox) {
		this.bucketSize = bucketSize;
		this.bbox = bbox;
		size = 0;
		root = new ExternalNode();
	}
	
	/*
	 * Clears the entire structure/removes all entries from tree
	 */
	public void clear() {
		size = 0;
		root = null;
	}
	
	/*
	 * Returns the number of points in the tree
	 */
	public int size() { 
		return size;
	}

	/*
	 * Determines whether point q occurs within the tree
	 */
	public LPoint find(Point2D pt) { 
		if (size == 0) {
			return null;
		}
		return root.find(pt);
	}
	
	/*
	 * Inserts the single labeled point into the tree, utilizes bulkInsert
	 */
	public void insert(LPoint pt) throws Exception {
		if (!bbox.contains(pt.getPoint2D())) {
			throw new Exception("Attempt to insert a point outside bounding box");
		}
		ArrayList<LPoint> pts = new ArrayList<LPoint>();
		pts.add(pt);
		bulkInsert(pts);
	}
	
	/*
	 * Inserts a set of labeled points into the tree
	 */
	public void bulkInsert(ArrayList<LPoint> pts) throws Exception {
		for (LPoint p : pts){
			if (!bbox.contains(p.getPoint2D())) {
				throw new Exception("Attempt to insert a point outside bounding box");
			}
		}
		if (size == 0) {
			root = new ExternalNode(); 
		}
		root = root.bulkInsert(pts);
		size += pts.size();
	}

	/*
	 * Generates a right-to-left preorder traversal of the nodes in the tree
	 */
	public ArrayList<String> list() {
		ArrayList<String> helper = new ArrayList<String>();
		if (size == 0) {
			root = new ExternalNode(); 
		}
		root.list(helper);
		return helper;
	}
	
	/*
	 * Computes the closest point in the tree to the query point, center
	 */
	public LPoint nearestNeighbor(Point2D center) { 
		if (size == 0) {
			return null;
		}	
		return root.nearestNeighbor(center, bbox, null);
	}

	/*
	 * deletes the point sent in
	 */
	public void delete(Point2D pt) throws Exception {
		root = root.delete(pt);
		size -= 1;
		
	}

	/*
	 * Returns an list of the number of k closest points to the center point
	 */
	public ArrayList<LPoint> kNearestNeighbor(Point2D center, int k) {
		if (size == 0) {
			return new ArrayList<>();
		}
		MinK best = new MinK(k, Double.POSITIVE_INFINITY);
		root.kNearestNeighbor(center, k, bbox, best);
		return best.list();
	}
}
