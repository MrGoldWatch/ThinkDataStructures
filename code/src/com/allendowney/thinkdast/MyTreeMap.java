/**
 *
 */
// package com.allendowney.thinkdast;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of a Map using a binary search tree.
 *
 * @param <K>
 * @param <V>
 *
 */
public class MyTreeMap<K, V> implements Map<K, V> {

	private int size = 0;
	private Node root = null;

	/**
	 * Represents a node in the tree.
	 *
	 */
	protected class Node {
		public K key;
		public V value;
		public Node left = null;
		public Node right = null;

		/**
		 * @param key
		 * @param value
		 * @param left
		 * @param right
		 */
		public Node(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}

	@Override
	public void clear() {
		size = 0;
		root = null;
	}

	@Override
	public boolean containsKey(Object target) {
		return findNode(target) != null;
	}

	/**
	 * Returns the entry that contains the target key, or null if there is none.
	 *
	 * @param target
	 */
	private Node findNode(Object target) {
		// some implementations can handle null as a key, but not this one
		if (target == null) {
			throw new IllegalArgumentException();
		}

		// something to make the compiler happy
        @SuppressWarnings("unchecked")
        
        // Comparable enables usage of compareTo mathod
		Comparable<? super K> k = (Comparable<? super K>) target; 

        // ****************** first try
        // TODO: FILL THIS IN!
        // if (equals(k, root.key)) {
        //     return ((Node) target);
        // }
        // if (k<root.key) {
        //     root = root.left;
        //     return findNode(target);
        // } else if (k>root.key) {
        //     root = root.right;
        //     return findNode(target);
        // } else {
        //     return null;
        // }


        // ****************** second try
        // int test = k.compareTo(root.key);
        // switch (test) {
        //     case -1:    
        //         root = root.left;
        //         return findNode(target);
        //     case 0:     
        //         return ((Node) target);
        //     case 1:
        //         root = root.right;
        //         return findNode(target);
        // }

        // ****************** third try - with help
        // ****************** works, but for some reason junit takes a long time
        // Node node = root;
        // while (node != null) {
        //     int test = k.compareTo(node.key);
        //     switch (test) {
        //         case -1:    
        //             root = root.left;
        //             break;
        //         case 0:     
        //             return node;
        //         case 1:
        //             root = root.right;
        //             break;
        //     }
        // }
        // return null;

        // ****************** from solution
        // forgot to create a Node object to set to root for comparable and return.
        Node node = root;
    	while (node != null) {
            int cmp = k.compareTo(node.key);
            if (cmp < 0)
                node = node.left;
            else if (cmp > 0)
                node = node.right;
            else
                return node;
    	}
        return null;
	}

	/**
	 * Compares two keys or two values, handling null correctly.
	 *
	 * @param target
	 * @param obj
	 * @return
	 */
	private boolean equals(Object target, Object obj) {
		if (target == null) {
			return obj == null;
		}
		return target.equals(obj);
	}

	@Override
	public boolean containsValue(Object target) {
		return containsValueHelper(root, target);
	}

	private boolean containsValueHelper(Node node, Object target) {
        // TODO: FILL THIS IN! 
        // forgot to check for null
        if (node == null) {
            return false;
        }
        return (equals(node.value, target) || containsValueHelper(node.left, target) || containsValueHelper(node.right, target));
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public V get(Object key) {
		Node node = findNode(key);
		if (node == null) {
			return null;
		}
		return node.value;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public Set<K> keySet() {
		Set<K> set = new LinkedHashSet<K>();
        addInOrder(root, set);
		return set;
	}

    /* Walks the tree and adds the keys to `set`.
	 *
     * node: root of the tree
     * set: set to add the nodes to
     */
	private void addInOrder(Node node, Set<K> set) {
        if (node == null) return;
        addInOrder(node.left, set);
        set.add(node.key);
        addInOrder(node.right, set);
    }


	@Override
	public V put(K key, V value) {
		if (key == null) {
			throw new NullPointerException();
		}
		if (root == null) {
			root = new Node(key, value);
			size++;
			return null;
		}
		return putHelper(root, key, value);
	}

	private V putHelper(Node node, K key, V value) {

        // ****************** first try 
        // @SuppressWarnings("unchecked")
		// Comparable<? super K> k = (Comparable<? super K>) key; 
        // while (node != null) {
        //     int cmp = k.compareTo(node.key);
        //     if (cmp < 0)
        //         node = node.left;
        //     else if (cmp > 0)
        //         node = node.right;
        //     else {
        //         V v = node.value;
        //         node.value = value;
        //         return v;
        //     }
        // }
        // node = new Node(key, value);
        // size++;
        // return null;
        
        // ****************** second try - with help
        @SuppressWarnings("unchecked")
		Comparable<? super K> k = (Comparable<? super K>) key; 
        int cmp = k.compareTo(node.key);
        if (cmp < 0) {
            if (node.left == null) {
                node.left = new Node(key, value);
                size++;
                return null;
            }
            node = node.left;
            return putHelper(node, key, value);
        }
            
        else if (cmp > 0) {
            if (node.right == null) {
                node.right = new Node(key, value);
                size++;
                return null;
            }
            node = node.right;
            return putHelper(node, key, value);
        }
        V v = node.value;
        node.value = value;
        return v;
    }

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Map.Entry<? extends K, ? extends V> entry: map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public V remove(Object key) {
		// OPTIONAL TODO: FILL THIS IN!
        // throw new UnsupportedOperationException();
        if (! containsKey(key)) return null;
        root = deleteNode(root, key);
        size--;
        System.out.println("node with key: "+key+" and value: "+root.value+" has been removed.");
        return root.value;
    }
    
    private Node deleteNode (Node root, Object key){
        if (root == null) return root;

        @SuppressWarnings("unchecked")
		Comparable<? super K> k = (Comparable<? super K>) key; 
        int cmp = k.compareTo(root.key);
        /* Otherwise, recur down the tree */
        if (cmp < 0) 
            root.left = deleteNode(root.left, key); 
        else if (cmp > 0) 
            root.right = deleteNode(root.right, key); 
  
        // if key is same as root's key, then This is the node 
        // to be deleted 
        else
        { 
            // node with only one child or no child 
            if (root.left == null) 
                return root.right; 
            else if (root.right == null) 
                return root.left; 
  
            // node with two children: Get the inorder successor (smallest 
            // in the right subtree) 
            root = minimumElement(root.right); 
  
            // Delete the inorder successor 
            root.right = deleteNode(root.right, root.key); 
        }
        return root; 
    }

    private Node minimumElement(Node node) {
        if (node.left == null) {
            return node;
        } else {
            return minimumElement(node.left);
        }
    }

    // private Node deleteNode (Node root, Object key){
    //     Node node = findNode(key);
    //     if (node == null) {
    //         return null;
    //     }

    //     // Node to be deleted has both child
    //     if (node.left != null && node.right != null) {
            
    //         // Get smallest child from right subtree
    //         Node minNode = minimumElement(node.right);
    //         // Replace the Node to be deleted with minNode
    //         // node.key = minNode.key;
    //         // node.value = minNode.value;
    //         // node = minNode;
    //         // remove the minNode
    //         Node temp = minNode;
    //         remove(minNode.key);
    //         node = temp;
            
    //     } 
    //     // Node to be deleted has only left child
    //     else if (node.left != null) {
    //         node = node.left;
    //     }
    //     // Node to be deleted has only right child
    //     else if (node.right != null) {
    //         node = node.right;
    //     }
    //     // Node to be deleted has no child
    //     else {
    //         node = null;
    //     }
    //     return node;
    // }

    

	@Override
	public int size() {
		return size;
	}

	@Override
	public Collection<V> values() {
		Set<V> set = new HashSet<V>();
		Deque<Node> stack = new LinkedList<Node>();
		stack.push(root);
		while (!stack.isEmpty()) {
			Node node = stack.pop();
			if (node == null) continue;
			set.add(node.value);
			stack.push(node.left);
			stack.push(node.right);
		}
		return set;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, Integer> map = new MyTreeMap<String, Integer>();
		map.put("Word1", 1);
        map.put("Word2", 2);
        System.out.println(map.containsKey("Word1"));
		Integer value = map.get("Word1");
        System.out.println(value);
        System.out.println(map.containsValue(3));
        System.out.println(map.containsValue(2));
        map.put("Word3", 4);
        map.put("Word0", 3);
        System.out.println(map.size());
        map.remove("Word3");
        System.out.println(map.size());

		for (String key: map.keySet()) {
            System.out.println("inside");
			System.out.println(key + ", " + map.get(key));
		}
	}

	/**
	 * Makes a node.
	 *
	 * This is only here for testing purposes.  Should not be used otherwise.
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public MyTreeMap<K, V>.Node makeNode(K key, V value) {
		return new Node(key, value);
	}

	/**
	 * Sets the instance variables.
	 *
	 * This is only here for testing purposes.  Should not be used otherwise.
	 *
	 * @param node
	 * @param size
	 */
	public void setTree(Node node, int size ) {
		this.root = node;
		this.size = size;
	}

	/**
	 * Returns the height of the tree.
	 *
	 * This is only here for testing purposes.  Should not be used otherwise.
	 *
	 * @return
	 */
	public int height() {
		return heightHelper(root);
	}

	private int heightHelper(Node node) {
		if (node == null) {
			return 0;
		}
		int left = heightHelper(node.left);
		int right = heightHelper(node.right);
		return Math.max(left, right) + 1;
	}
}
