package eg.edu.alexu.csd.filestructure.btree;


import javax.management.RuntimeErrorException;
import java.util.ArrayList;
import java.util.List;

public class BTree<K extends Comparable<K>, V> implements IBTree <K,V> {
	private BNode root = null;
	private int mindegree;

	public BTree( int mindegree) {
	    if(mindegree <2)
	        throw new RuntimeErrorException(new ExceptionInInitializerError());
		this.mindegree = mindegree;
	}

	@Override
	public int getMinimumDegree() {
		return mindegree;
	}

	@Override
	public IBTreeNode getRoot() {
		return root;
	}

	@Override
	public void insert(K key, V value) {
	    if(key == null || value == null)
	        throw new RuntimeErrorException(new ExceptionInInitializerError());
		Point<K,V> point = new Point<>(key,value);
		if(root == null) {
			root = new BNode<>(2 * mindegree , null);
			root.addPoint(point);
			return;
		}
		insert(root ,point);
	}
	private void insert(BNode<K, V> node ,Point<K,V> point){
		List<K> keys = node.getKeys();
		if(node.isLeaf()){
		    for(K key:keys){
		        if (point.getKey().compareTo(key)==0)
		            return;
            }
			node.addPoint(point);
			checkDataSize(node);
			return;
		}
		for (int i = 0 ; i < keys.size() ; i++){
			if(point.getKey().compareTo(keys.get(i))<0){
				insert((BNode<K, V>) node.getChild(i),point);
				return;
			}else if(point.getKey().compareTo(keys.get(i))>0){
				/*if it's the last key in the list*/
				if(i == keys.size()-1){
					insert((BNode<K, V>) node.getChild(i+1),point);
					return;
				}
			}else {
				return;
			}
		}
	}
	private void checkDataSize(BNode<K,V> node){
		if(node.getData().size()>2 * mindegree - 1){
			split((BNode<K, V>) node.getParent(), node);
		}
	}
	private void split(BNode<K, V> parent , BNode<K, V> node){
		if(node == root){
			parent = new BNode<>(2*mindegree , null);
			parent.setLeaf(false);
			parent.addChild(node);
			node.setParent(parent);
			root = parent;
		}
		List<Point<K,V>> nodeData = node.getData();
		List<IBTreeNode<K, V>> nodeChildren = node.getChildren();
		int numOfKeys = 2 * mindegree - 1;
		BNode<K,V> splitNode = new BNode<>(2*mindegree , parent);
		List<Point<K,V>> splitNodeData = new ArrayList<>();
		if(!node.isLeaf()){
			splitNode.setLeaf(false);
            splitNode.setChildren(nodeChildren.subList(nodeChildren.size()/2+1,nodeChildren.size()));
            for(int i = nodeChildren.size()/2+1 ; i < nodeChildren.size() ; i++){
                ((BNode<K,V>)nodeChildren.get(i)).setParent(splitNode);
            }
            node.setChildren(nodeChildren.subList(0,nodeChildren.size()/2+1));
        }
		parent.addPoint(nodeData.get(numOfKeys/2+1));
		for(int i =numOfKeys/2+2 ; i <= numOfKeys ; i++){
			splitNodeData.add(nodeData.get(i));
		}
		splitNode.setData(splitNodeData);
//		or node.size()
		node.setData(nodeData.subList(0,numOfKeys/2+1));
//		siblings.add(splitNode);
//		parent.setChildren(siblings);
//		List<IBTreeNode<K, V>> siblings = parent.getChildren();
//		int index =siblings.indexOf(node)+1;
		parent.addChildAfter(node,splitNode);
		checkDataSize(parent);
	}


	@Override
	public Object search(Comparable key) {
		if(key==null)
			throw new RuntimeErrorException(new ExceptionInInitializerError());
		BNode node=root;
		if(root==null)return null;
		V found=null;
		while(true) {
			ArrayList<K> ke=(ArrayList<K>) node.getKeys();
			ArrayList<V> va=(ArrayList<V>) node.getValues();
			ArrayList<BNode> ch=(ArrayList<BNode>) node.getChildren();
			int i=0;
			for(i=0;i<ke.size();i++) {
				int comp=key.compareTo(ke.get(i));
				if(comp==0) {
					found=(V) va.get(i);
					break;
				}else if(comp<0) {
					node= ch.get(i);
					break;
				}
			}
			if(found==null && i==ke.size())node= ch.get(i);
			if(found!=null||node == null) {
				break;
			}

		}
		if(found==null)return null;
		else return found;

	}
	@Override
	public boolean delete(Comparable key) {
		if(key == null) throw new RuntimeErrorException(null);
		BNode x = searching(key);
		if(x==null) throw new RuntimeErrorException(null);
		if(x==root && x.isLeaf()){
			if(x.getNumOfKeys() == 1){
				root=null;
			}else {
				List<Point<K,V>> datax=x.getData();
				Point<K,V> theOne=null;
				for(int i=0;i<datax.size();i++){
					int comp = key.compareTo(datax.get(i).getKey());
					if(comp==0){
						theOne=(datax.get(i));
						x.removePoint(theOne);
						break;
					}
				}
			}
		}
		else if (x.isLeaf()){
			deleteLeaf(x,key);
		}else if(!x.isLeaf()){
			deleteInternal(x,key);
		}

		return true;
	}

	private void deleteInternal(BNode x,Comparable key) {
		List<Point<K,V>> datax=x.getData();
		Point<K,V> theOne=null;
		int indexOfKey =0;
		for(int i=0;i<datax.size();i++){
			int comp = key.compareTo(datax.get(i).getKey());
			if(comp==0){
				theOne=(datax.get(i));
				indexOfKey=i;
				break;
			}
		}
		BNode succ=null,pre;
		pre=predesessor((BNode) x.getChild(indexOfKey));
		if(pre.getNumOfKeys() == mindegree-1) {
			succ=inordersucssessor((BNode) x.getChild(indexOfKey+1));
		}
		if(succ == null){
			x.removePoint(theOne);
			x.addPoint(pre.getpoint(pre.getNumOfKeys()-1));
			pre.removePoint(pre.getpoint(pre.getNumOfKeys()-1));
		}else {
			if(succ.getNumOfKeys() >= mindegree){
				x.removePoint(theOne);
				x.addPoint(succ.getpoint(0));
				succ.removePoint(succ.getpoint(0));
			}
			else {
				x.removePoint(theOne);
				x.addPoint(pre.getpoint(pre.getNumOfKeys()-1));
				deleteLeaf(pre,pre.getNumOfKeys()-1);
			}


		}


	}

	private void deleteLeaf(BNode x,Comparable key) {
		List<Point<K,V>> datax=x.getData();
		Point<K,V> theOne=null;
		for(int i=0;i<datax.size();i++){
			int comp = key.compareTo(datax.get(i).getKey());
			if(comp==0){
				theOne=(datax.get(i));
			}
		}
		if(x.getNumOfKeys() >= this.mindegree){
			x.removePoint(theOne);
			return;
		}else{
			BNode y = (BNode) x.getParent();
			BNode left=null,right=null;
			int indexOfX=0;
			List<BNode<K, V>> childs = y.getChildren();
			for(int i=0;i<childs.size();i++){
				if (childs.get(i) == x) {
					indexOfX=i;
					if (i != 0) left = (BNode) y.getChild(i-1);
					if (i != childs.size() - 1) right = (BNode) y.getChild(i+1);
				}
			}
			if(right!=null && right.getNumOfKeys() >= this.mindegree){
				x.removePoint(theOne);
				x.addPoint(y.getpoint(indexOfX));
				y.removePoint(y.getpoint(indexOfX));
				y.addPoint(right.getpoint(0));
				right.removePoint(right.getpoint(0));
			}else if (left !=null && left.getNumOfKeys() >= this.mindegree){
				x.removePoint(theOne);
				x.addPoint(y.getpoint(indexOfX-1));
				y.removePoint(y.getpoint(indexOfX-1));
				y.addPoint(left.getpoint(left.getData().size()-1));
				left.removePoint(left.getpoint(left.getData().size()-1));
			}else{
				x.removePoint(theOne);
				if(right!=null){
					merging(x,y,right,indexOfX);
				}else if(left!=null){
					merging(x,y,left,indexOfX-1);
				}
			}

		}
	}
	public void merging(BNode x,BNode y,BNode place,int index){

		x.addPoint(y.getpoint(index));
		int k=0;
		for ( k = 0; k < place.getKeys().size(); k++) {
			x.addPoint(place.getpoint(k));
			if(!x.isLeaf()){
				BNode ch= (BNode) place.getChild(k);
				ch.setParent(x);
				x.addChild(ch);

			}
		}
		if(!x.isLeaf()){
			BNode ch= (BNode) place.getChild(k);
			ch.setParent(x);
			x.addChild(ch);

		}
		y.removePoint(y.getpoint(index));
		y.removechild(place);
		if(y.getNumOfKeys() < mindegree-1 ){
			//ReArrange
			ReArrange(x,y);
		}
	}

	private void ReArrange(BNode z,BNode x) {   //z==>BNode x;      x==>BNode y ||x.getParent();
		if(x==root){
			if(x.getNumOfKeys()==0){
				root=z;
				return;
			}else {
				return ;
			}
		}
		BNode y = (BNode) x.getParent();
		BNode left=null,right=null;
		int indexOfX=0;
		List<BNode<K, V>> childs = y.getChildren();
		for(int i=0;i<childs.size();i++){
			if (childs.get(i) == x) {
				indexOfX=i;
				if (i != 0) left = (BNode) y.getChild(i-1);
				if (i != childs.size() - 1) right = (BNode) y.getChild(i+1);
			}
		}
		if(right!=null && right.getNumOfKeys() >= this.mindegree){
			x.addPoint(y.getpoint(indexOfX));
			y.removePoint(y.getpoint(indexOfX));
			y.addPoint(right.getpoint(0));
			right.removePoint(right.getpoint(0));
			BNode ch= (BNode) right.getChild(0);
			ch.setParent(x);
			x.addChild(ch);
			right.removechild(ch);
		}else if (left !=null && left.getNumOfKeys() >= this.mindegree){
			x.addPoint(y.getpoint(indexOfX-1));
			y.removePoint(y.getpoint(indexOfX-1));
			y.addPoint(left.getpoint(left.getNumOfKeys()-1));
			BNode ch= (BNode) left.getChild(left.getChildren().size()-1);
			left.removePoint(left.getpoint(left.getData().size()-1));
			ch.setParent(x);
			x.addChild(0,ch);
			left.removechild(ch);
		}else{
			if(right!=null){
				merging(x,y,right,indexOfX);
			}else if(left!=null){
				merging(x,y,left,indexOfX-1);
			}
		}


	}

	public BNode searching(Comparable key) {
		if(key==null)return null;
		BNode node=root;
		V found=null;
		while(true) {
			ArrayList<K> ke=(ArrayList<K>) node.getKeys();
			ArrayList<K> va=(ArrayList<K>) node.getValues();
			ArrayList<K> ch=(ArrayList<K>) node.getChildren();
			int i=0;
			for(i=0;i<ke.size();i++) {
				int comp=key.compareTo(ke.get(i));
				if(comp==0) {
					found=(V) va.get(i);
					return node;
				}else if(comp<0) {
					node=(BNode) ch.get(i);
					break;
				}
			}
			if(found==null && i==ke.size())node=(BNode) ch.get(i);
			if(found!=null||node == null) {
				break;
			}

		}
		return null;

	}
	public void print(IBTreeNode<K,V>  n , boolean printIt){
	    if(printIt)
            System.out.println(n.getKeys());
        if(!n.isLeaf()){
            for(int j = 0; j < n.getChildren().size()  ; j++)
                    System.out.print(n.getChildren().get(j).getKeys()+" ");
            System.out.println();
            for(int j = 0; j < n.getChildren().size()  ; j++)
                print(n.getChildren().get(j) , false);
        }
	}

	public BNode predesessor(BNode x){
		if(x.isLeaf())return x;
		List<IBTreeNode<K, V>> childs = x.getChildren();
		int i=0;
		while(!x.isLeaf()){
			childs=x.getChildren();
			x= (BNode) childs.get(childs.size()-1);
		}
		return x;
	}
	public BNode inordersucssessor(BNode x){
		if(x.isLeaf())return x;
		List<IBTreeNode<K, V>> childs = x.getChildren();
		while(!x.isLeaf()){
			childs=x.getChildren();
			x= (BNode) childs.get(0);
		}
		return x;
	}

}
