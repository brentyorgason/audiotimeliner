package ui.timeliner;

/*
 * BubbleTreeNode class
 * BubbleTreeNode is an adaptation of a DefaultMutableTreeNode, both of which implement MutableTreeNode.
 * I've added new methods at the end
 *
 * @author Brent Yorgason
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Vector;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class BubbleTreeNode extends Object implements Cloneable,
       MutableTreeNode, Serializable
{

   private static final long serialVersionUID = 1L;

	/**
     * An enumeration that is always empty. This is used when an enumeration
     * of a leaf node's children is requested.
     */
    static public final Enumeration EMPTY_ENUMERATION
        = new Enumeration() {
            public boolean hasMoreElements() { return false; }
            public Object nextElement() {
                throw new NoSuchElementException("No more elements");
            }
    };

    /** this node's parent, or null if this node has no parent */
    protected MutableTreeNode   parent;

    /** array of children, may be null if this node has no children */
    protected Vector<MutableTreeNode>		children;

    /** optional user object */
    transient protected Object	userObject;

    /** true if the node is able to have children */
    protected boolean		allowsChildren;


    /**
     * Creates a tree node that has no parent and no children, but which
     * allows children.
     */
    public BubbleTreeNode() {
        this(null);
    }

    /**
     * Creates a tree node with no parent, no children, but which allows
     * children, and initializes it with the specified user object.
     *
     * @param userObject an Object provided by the user that constitutes
     *                   the node's data
     */
    public BubbleTreeNode(Object userObject) {
        this(userObject, true);
    }

    /**
     * Creates a tree node with no parent, no children, initialized with
     * the specified user object, and that allows children only if
     * specified.
     *
     * @param userObject an Object provided by the user that constitutes
     *        the node's data
     * @param allowsChildren if true, the node is allowed to have child
     *        nodes -- otherwise, it is always a leaf node
     */
    public BubbleTreeNode(Object userObject, boolean allowsChildren) {
        super();
        parent = null;
        this.allowsChildren = allowsChildren;
        this.userObject = userObject;
    }


    //
    //  Primitives
    //

    /**
     * Removes <code>newChild</code> from its present parent (if it has a
     * parent), sets the child's parent to this node, and then adds the child
     * to this node's child array at index <code>childIndex</code>.
     * <code>newChild</code> must not be null and must not be an ancestor of
     * this node.
     *
     * @param	newChild	the MutableTreeNode to insert under this node
     * @param	childIndex	the index in this node's child array
     *				where this node is to be inserted
     * @exception	ArrayIndexOutOfBoundsException	if
     *				<code>childIndex</code> is out of bounds
     * @exception	IllegalArgumentException	if
     *				<code>newChild</code> is null or is an
     *				ancestor of this node
     * @exception	IllegalStateException	if this node does not allow
     *						children
     * @see	#isNodeDescendant
     */
    public void insert(MutableTreeNode newChild, int childIndex) {
        if (!allowsChildren) {
            throw new IllegalStateException("node does not allow children");
        } else if (newChild == null) {
            throw new IllegalArgumentException("new child is null");
        } else if (isNodeAncestor(newChild)) {
            throw new IllegalArgumentException("new child is an ancestor");
        }

            MutableTreeNode oldParent = (MutableTreeNode)newChild.getParent();

            if (oldParent != null) {
                oldParent.remove(newChild);
            }
            newChild.setParent(this);
            if (children == null) {
                children = new Vector<>();
            }
            children.insertElementAt(newChild, childIndex);
    }

    /**
     * Removes the child at the specified index from this node's children
     * and sets that node's parent to null. The child node to remove
     * must be a <code>MutableTreeNode</code>.
     *
     * @param	childIndex	the index in this node's child array
     *				of the child to remove
     * @exception	ArrayIndexOutOfBoundsException	if
     *				<code>childIndex</code> is out of bounds
     */
    public void remove(int childIndex) {
        MutableTreeNode child = (MutableTreeNode)getChildAt(childIndex);
        children.removeElementAt(childIndex);
        child.setParent(null);
    }

    /**
     * Sets this node's parent to <code>newParent</code> but does not
     * change the parent's child array.  This method is called from
     * <code>insert()</code> and <code>remove()</code> to
     * reassign a child's parent, it should not be messaged from anywhere
     * else.
     *
     * @param	newParent	this node's new parent
     */
    public void setParent(MutableTreeNode newParent) {
        parent = newParent;
    }

    /**
     * Returns this node's parent or null if this node has no parent.
     *
     * @return	this node's parent TreeNode, or null if this node has no parent
     */
    public TreeNode getParent() {
        return parent;
    }

    /**
     * Returns the child at the specified index in this node's child array.
     *
     * @param	index	an index into this node's child array
     * @exception	ArrayIndexOutOfBoundsException	if <code>index</code>
     *						is out of bounds
     * @return	the TreeNode in this node's child array at  the specified index
     */
    public TreeNode getChildAt(int index) {
        if (children == null) {
            throw new ArrayIndexOutOfBoundsException("node has no children");
        }
        return (TreeNode)children.elementAt(index);
    }

    /**
     * Returns the number of children of this node.
     *
     * @return	an int giving the number of children of this node
     */
    public int getChildCount() {
        if (children == null) {
            return 0;
        } else {
            return children.size();
        }
    }

    /**
     * Returns the index of the specified child in this node's child array.
     * If the specified node is not a child of this node, returns
     * <code>-1</code>.  This method performs a linear search and is O(n)
     * where n is the number of children.
     *
     * @param	aChild	the TreeNode to search for among this node's children
     * @exception	IllegalArgumentException	if <code>aChild</code>
     *							is null
     * @return	an int giving the index of the node in this node's child
     *          array, or <code>-1</code> if the specified node is a not
     *          a child of this node
     */
    public int getIndex(TreeNode aChild) {
        if (aChild == null) {
            throw new IllegalArgumentException("argument is null");
        }

        if (!isNodeChild(aChild)) {
            return -1;
        }
        return children.indexOf(aChild);	// linear search
    }

    /**
     * Creates and returns a forward-order enumeration of this node's
     * children.  Modifying this node's child array invalidates any child
     * enumerations created before the modification.
     *
     * @return	an Enumeration of this node's children
     */
    public Enumeration<MutableTreeNode> children() {
        if (children == null) {
            return EMPTY_ENUMERATION;
        } else {
            return children.elements();
        }
    }

    /**
     * Determines whether or not this node is allowed to have children.
     * If <code>allows</code> is false, all of this node's children are
     * removed.
     * <p>
     * Note: By default, a node allows children.
     *
     * @param	allows	true if this node is allowed to have children
     */
    public void setAllowsChildren(boolean allows) {
        if (allows != allowsChildren) {
            allowsChildren = allows;
            if (!allowsChildren) {
                removeAllChildren();
            }
        }
    }

    /**
     * Returns true if this node is allowed to have children.
     *
     * @return	true if this node allows children, else false
     */
    public boolean getAllowsChildren() {
        return allowsChildren;
    }

    /**
     * Sets the user object for this node to <code>userObject</code>.
     *
     * @param	userObject	the Object that constitutes this node's
     *                          user-specified data
     * @see	#getUserObject
     * @see	#toString
     */
    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }

    /**
     * Returns this node's user object.
     *
     * @return	the Object stored at this node by the user
     * @see	#setUserObject
     * @see	#toString
     */
    public Object getUserObject() {
        return userObject;
    }


    //
    //  Derived methods
    //

    /**
     * Removes the subtree rooted at this node from the tree, giving this
     * node a null parent.  Does nothing if this node is the root of its
     * tree.
     */
    public void removeFromParent() {
        MutableTreeNode parent = (MutableTreeNode)getParent();
        if (parent != null) {
            parent.remove(this);
        }
    }

    /**
     * Removes <code>aChild</code> from this node's child array, giving it a
     * null parent.
     *
     * @param	aChild	a child of this node to remove
     * @exception	IllegalArgumentException	if <code>aChild</code>
     *					is null or is not a child of this node
     */
    public void remove(MutableTreeNode aChild) {
        if (aChild == null) {
            throw new IllegalArgumentException("argument is null");
        }

        if (!isNodeChild(aChild)) {
            throw new IllegalArgumentException("argument is not a child");
        }
        remove(getIndex(aChild));	// linear search
    }

    /**
     * Removes all of this node's children, setting their parents to null.
     * If this node has no children, this method does nothing.
     */
    public void removeAllChildren() {
        for (int i = getChildCount()-1; i >= 0; i--) {
            remove(i);
        }
    }

    /**
     * Removes <code>newChild</code> from its parent and makes it a child of
     * this node by adding it to the end of this node's child array.
     *
     * @see		#insert
     * @param	newChild	node to add as a child of this node
     * @exception	IllegalArgumentException    if <code>newChild</code>
     *						is null
     * @exception	IllegalStateException	if this node does not allow
     *						children
     */
    public void add(MutableTreeNode newChild) {
        if(newChild != null && newChild.getParent() == this)
            insert(newChild, getChildCount() - 1);
        else
            insert(newChild, getChildCount());
    }



    //
    //  Tree Queries
    //

    /**
     * Returns true if <code>anotherNode</code> is an ancestor of this node
     * -- if it is this node, this node's parent, or an ancestor of this
     * node's parent.  (Note that a node is considered an ancestor of itself.)
     * If <code>anotherNode</code> is null, this method returns false.  This
     * operation is at worst O(h) where h is the distance from the root to
     * this node.
     *
     * @see		#isNodeDescendant
     * @see		#getSharedAncestor
     * @param	anotherNode	node to test as an ancestor of this node
     * @return	true if this node is a descendant of <code>anotherNode</code>
     */
    public boolean isNodeAncestor(TreeNode anotherNode) {
        if (anotherNode == null) {
            return false;
        }

        TreeNode ancestor = this;

        do {
            if (ancestor == anotherNode) {
                return true;
            }
        } while((ancestor = ancestor.getParent()) != null);

        return false;
    }

    /**
     * Returns true if <code>anotherNode</code> is a descendant of this node
     * -- if it is this node, one of this node's children, or a descendant of
     * one of this node's children.  Note that a node is considered a
     * descendant of itself.  If <code>anotherNode</code> is null, returns
     * false.  This operation is at worst O(h) where h is the distance from the
     * root to <code>anotherNode</code>.
     *
     * @see	#isNodeAncestor
     * @see	#getSharedAncestor
     * @param	anotherNode	node to test as descendant of this node
     * @return	true if this node is an ancestor of <code>anotherNode</code>
     */
    public boolean isNodeDescendant(BubbleTreeNode anotherNode) {
        if (anotherNode == null)
            return false;

        return anotherNode.isNodeAncestor(this);
    }

    /**
     * Returns the nearest common ancestor to this node and <code>aNode</code>.
     * Returns null, if no such ancestor exists -- if this node and
     * <code>aNode</code> are in different trees or if <code>aNode</code> is
     * null.  A node is considered an ancestor of itself.
     *
     * @see	#isNodeAncestor
     * @see	#isNodeDescendant
     * @param	aNode	node to find common ancestor with
     * @return	nearest ancestor common to this node and <code>aNode</code>,
     *		or null if none
     */
    public TreeNode getSharedAncestor(BubbleTreeNode aNode) {
        if (aNode == this) {
            return this;
        } else if (aNode == null) {
            return null;
        }

        int		level1, level2, diff;
        TreeNode	node1, node2;

        level1 = getLevel();
        level2 = aNode.getLevel();

        if (level2 > level1) {
            diff = level2 - level1;
            node1 = aNode;
            node2 = this;
        } else {
            diff = level1 - level2;
            node1 = this;
            node2 = aNode;
        }

        // Go up the tree until the nodes are at the same level
        while (diff > 0) {
            node1 = node1.getParent();
            diff--;
        }

        // Move up the tree until we find a common ancestor.  Since we know
        // that both nodes are at the same level, we won't cross paths
        // unknowingly (if there is a common ancestor, both nodes hit it in
        // the same iteration).

        do {
            if (node1 == node2) {
                return node1;
            }
            node1 = node1.getParent();
            node2 = node2.getParent();
        } while (node1 != null);// only need to check one -- they're at the
        // same level so if one is null, the other is

        if (node1 != null || node2 != null) {
            throw new Error ("nodes should be null");
        }

        return null;
    }


    /**
     * Returns true if and only if <code>aNode</code> is in the same tree
     * as this node.  Returns false if <code>aNode</code> is null.
     *
     * @see	#getSharedAncestor
     * @see	#getRoot
     * @return	true if <code>aNode</code> is in the same tree as this node;
     *		false if <code>aNode</code> is null
     */
    public boolean isNodeRelated(BubbleTreeNode aNode) {
        return (aNode != null) && (getRoot() == aNode.getRoot());
    }


    /**
     * Returns the depth of the tree rooted at this node -- the longest
     * distance from this node to a leaf.  If this node has no children,
     * returns 0.  This operation is much more expensive than
     * <code>getLevel()</code> because it must effectively traverse the entire
     * tree rooted at this node.
     *
     * @see	#getLevel
     * @return	the depth of the tree whose root is this node
     */
    public int getDepth() {
        Object	last = null;
        Enumeration	enumer = breadthFirstEnumeration();

        while (enumer.hasMoreElements()) {
            last = enumer.nextElement();
        }

        if (last == null) {
            throw new Error ("nodes should be null");
        }

        return ((BubbleTreeNode)last).getLevel() - getLevel();
    }



    /**
     * Returns the number of levels above this node -- the distance from
     * the root to this node.  If this node is the root, returns 0.
     *
     * @see	#getDepth
     * @return	the number of levels above this node
     */
    public int getLevel() {
        TreeNode ancestor;
        int levels = 0;

        ancestor = this;
        while((ancestor = ancestor.getParent()) != null){
            levels++;
        }

        return levels;
    }


    /**
      * Returns the path from the root, to get to this node.  The last
      * element in the path is this node.
      *
      * @return an array of TreeNode objects giving the path, where the
      *         first element in the path is the root and the last
      *         element is this node.
      */
    public TreeNode[] getPath() {
        return getPathToRoot(this, 0);
    }

    /**
     * Builds the parents of node up to and including the root node,
     * where the original node is the last element in the returned array.
     * The length of the returned array gives the node's depth in the
     * tree.
     *
     * @param aNode  the TreeNode to get the path for
     * @param depth  an int giving the number of steps already taken towards
     *        the root (on recursive calls), used to size the returned array
     * @return an array of TreeNodes giving the path from the root to the
     *         specified node
     */
    protected TreeNode[] getPathToRoot(TreeNode aNode, int depth) {
        TreeNode[]              retNodes;

        /* Check for null, in case someone passed in a null node, or
           they passed in an element that isn't rooted at root. */
        if(aNode == null) {
            if(depth == 0)
                return null;
            else
                retNodes = new TreeNode[depth];
        }
        else {
            depth++;
            retNodes = getPathToRoot(aNode.getParent(), depth);
            retNodes[retNodes.length - depth] = aNode;
        }
        return retNodes;
    }

    /**
      * Returns the user object path, from the root, to get to this node.
      * If some of the TreeNodes in the path have null user objects, the
      * returned path will contain nulls.
      */
    public Object[] getUserObjectPath() {
        TreeNode[]          realPath = getPath();
        Object[]            retPath = new Object[realPath.length];

        for(int counter = 0; counter < realPath.length; counter++)
            retPath[counter] = ((BubbleTreeNode)realPath[counter])
                               .getUserObject();
        return retPath;
    }

    /**
     * Returns the root of the tree that contains this node.  The root is
     * the ancestor with a null parent.
     *
     * @see	#isNodeAncestor
     * @return	the root of the tree that contains this node
     */
    public TreeNode getRoot() {
        TreeNode ancestor = this;
        TreeNode previous;

        do {
            previous = ancestor;
            ancestor = ancestor.getParent();
        } while (ancestor != null);

        return previous;
    }


    /**
     * Returns true if this node is the root of the tree.  The root is
     * the only node in the tree with a null parent; every tree has exactly
     * one root.
     *
     * @return	true if this node is the root of its tree
     */
    public boolean isRoot() {
        return getParent() == null;
    }


    /**
     * Returns the node that follows this node in a preorder traversal of this
     * node's tree.  Returns null if this node is the last node of the
     * traversal.  This is an inefficient way to traverse the entire tree; use
     * an enumeration, instead.
     *
     * @see	#preorderEnumeration
     * @return	the node that follows this node in a preorder traversal, or
     *		null if this node is last
     */
    public BubbleTreeNode getNextNode() {
        if (getChildCount() == 0) {
            // No children, so look for nextSibling
            BubbleTreeNode nextSibling = getNextSibling();

            if (nextSibling == null) {
                BubbleTreeNode aNode = (BubbleTreeNode)getParent();

                do {
                    if (aNode == null) {
                        return null;
                    }

                    nextSibling = aNode.getNextSibling();
                    if (nextSibling != null) {
                        return nextSibling;
                    }

                    aNode = (BubbleTreeNode)aNode.getParent();
                } while(true);
            } else {
                return nextSibling;
            }
        } else {
            return (BubbleTreeNode)getChildAt(0);
        }
    }


    /**
     * Returns the node that precedes this node in a preorder traversal of
     * this node's tree.  Returns null if this node is the first node of the
     * traveral -- the root of the tree.  This is an inefficient way to
     * traverse the entire tree; use an enumeration, instead.
     *
     * @see	#preorderEnumeration
     * @return	the node that precedes this node in a preorder traversal, or
     *		null if this node is the first
     */
    public BubbleTreeNode getPreviousNode() {
        BubbleTreeNode previousSibling;
        BubbleTreeNode myParent = (BubbleTreeNode)getParent();

        if (myParent == null) {
            return null;
        }

        previousSibling = getPreviousSibling();

        if (previousSibling != null) {
            if (previousSibling.getChildCount() == 0)
                return previousSibling;
            else
                return previousSibling.getLastLeaf();
        } else {
            return myParent;
        }
    }

    /**
     * Creates and returns an enumeration that traverses the subtree rooted at
     * this node in preorder.  The first node returned by the enumeration's
     * <code>nextElement()</code> method is this node.<P>
     *
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     *
     * @see	#postorderEnumeration
     * @return	an enumeration for traversing the tree in preorder
     */
    public Enumeration preorderEnumeration() {
        return new PreorderEnumeration(this);
    }

    /**
     * Creates and returns an enumeration that traverses the subtree rooted at
     * this node in postorder.  The first node returned by the enumeration's
     * <code>nextElement()</code> method is the leftmost leaf.  This is the
     * same as a depth-first traversal.<P>
     *
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     *
     * @see	#depthFirstEnumeration
     * @see	#preorderEnumeration
     * @return	an enumeration for traversing the tree in postorder
     */
    public Enumeration postorderEnumeration() {
        return new PostorderEnumeration(this);
    }

    /**
     * Creates and returns an enumeration that traverses the subtree rooted at
     * this node in breadth-first order.  The first node returned by the
     * enumeration's <code>nextElement()</code> method is this node.<P>
     *
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     *
     * @see	#depthFirstEnumeration
     * @return	an enumeration for traversing the tree in breadth-first order
     */
    public Enumeration breadthFirstEnumeration() {
        return new BreadthFirstEnumeration(this);
    }

    /**
     * Creates and returns an enumeration that traverses the subtree rooted at
     * this node in depth-first order.  The first node returned by the
     * enumeration's <code>nextElement()</code> method is the leftmost leaf.
     * This is the same as a postorder traversal.<P>
     *
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     *
     * @see	#breadthFirstEnumeration
     * @see	#postorderEnumeration
     * @return	an enumeration for traversing the tree in depth-first order
     */
    public Enumeration depthFirstEnumeration() {
        return postorderEnumeration();
    }

    /**
     * Creates and returns an enumeration that follows the path from
     * <code>ancestor</code> to this node.  The enumeration's
     * <code>nextElement()</code> method first returns <code>ancestor</code>,
     * then the child of <code>ancestor</code> that is an ancestor of this
     * node, and so on, and finally returns this node.  Creation of the
     * enumeration is O(m) where m is the number of nodes between this node
     * and <code>ancestor</code>, inclusive.  Each <code>nextElement()</code>
     * message is O(1).<P>
     *
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     *
     * @see		#isNodeAncestor
     * @see		#isNodeDescendant
     * @exception	IllegalArgumentException if <code>ancestor</code> is
     *						not an ancestor of this node
     * @return	an enumeration for following the path from an ancestor of
     *		this node to this one
     */
    public Enumeration pathFromAncestorEnumeration(TreeNode ancestor){
        return new PathBetweenNodesEnumeration(ancestor, this);
    }


    //
    //  Child Queries
    //

    /**
     * Returns true if <code>aNode</code> is a child of this node.  If
     * <code>aNode</code> is null, this method returns false.
     *
     * @return	true if <code>aNode</code> is a child of this node; false if
     *  		<code>aNode</code> is null
     */
    public boolean isNodeChild(TreeNode aNode) {
        boolean retval;

        if (aNode == null) {
            retval = false;
        } else {
            if (getChildCount() == 0) {
                retval = false;
            } else {
                retval = (aNode.getParent() == this);
            }
        }

        return retval;
    }


    /**
     * Returns this node's first child.  If this node has no children,
     * throws NoSuchElementException.
     *
     * @return	the first child of this node
     * @exception	NoSuchElementException	if this node has no children
     */
    public TreeNode getFirstChild() {
        if (getChildCount() == 0) {
            throw new NoSuchElementException("node has no children");
        }
        return getChildAt(0);
    }


    /**
     * Returns this node's last child.  If this node has no children,
     * throws NoSuchElementException.
     *
     * @return	the last child of this node
     * @exception	NoSuchElementException	if this node has no children
     */
    public TreeNode getLastChild() {
        if (getChildCount() == 0) {
            throw new NoSuchElementException("node has no children");
        }
        return getChildAt(getChildCount()-1);
    }


    /**
     * Returns the child in this node's child array that immediately
     * follows <code>aChild</code>, which must be a child of this node.  If
     * <code>aChild</code> is the last child, returns null.  This method
     * performs a linear search of this node's children for
     * <code>aChild</code> and is O(n) where n is the number of children; to
     * traverse the entire array of children, use an enumeration instead.
     *
     * @see		#children
     * @exception	IllegalArgumentException if <code>aChild</code> is
     *					null or is not a child of this node
     * @return	the child of this node that immediately follows
     *		<code>aChild</code>
     */
    public TreeNode getChildAfter(TreeNode aChild) {
        if (aChild == null) {
            throw new IllegalArgumentException("argument is null");
        }

        int index = getIndex(aChild);		// linear search

        if (index == -1) {
            throw new IllegalArgumentException("node is not a child");
        }

        if (index < getChildCount() - 1) {
            return getChildAt(index + 1);
        } else {
            return null;
        }
    }


    /**
     * Returns the child in this node's child array that immediately
     * precedes <code>aChild</code>, which must be a child of this node.  If
     * <code>aChild</code> is the first child, returns null.  This method
     * performs a linear search of this node's children for <code>aChild</code>
     * and is O(n) where n is the number of children.
     *
     * @exception	IllegalArgumentException if <code>aChild</code> is null
     *						or is not a child of this node
     * @return	the child of this node that immediately precedes
     *		<code>aChild</code>
     */
    public TreeNode getChildBefore(TreeNode aChild) {
        if (aChild == null) {
            throw new IllegalArgumentException("argument is null");
        }

        int index = getIndex(aChild);		// linear search

        if (index == -1) {
            throw new IllegalArgumentException("argument is not a child");
        }

        if (index > 0) {
            return getChildAt(index - 1);
        } else {
            return null;
        }
    }


    //
    //  Sibling Queries
    //


    /**
     * Returns true if <code>anotherNode</code> is a sibling of (has the
     * same parent as) this node.  A node is its own sibling.  If
     * <code>anotherNode</code> is null, returns false.
     *
     * @param	anotherNode	node to test as sibling of this node
     * @return	true if <code>anotherNode</code> is a sibling of this node
     */
    public boolean isNodeSibling(TreeNode anotherNode) {
        boolean retval;

        if (anotherNode == null) {
            retval = false;
        } else if (anotherNode == this) {
            retval = true;
        } else {
            TreeNode  myParent = getParent();
            retval = (myParent != null && myParent == anotherNode.getParent());

            if (retval && !((BubbleTreeNode)getParent())
                           .isNodeChild(anotherNode)) {
                throw new Error("sibling has different parent");
            }
        }

        return retval;
    }


    /**
     * Returns the number of siblings of this node.  A node is its own sibling
     * (if it has no parent or no siblings, this method returns
     * <code>1</code>).
     *
     * @return	the number of siblings of this node
     */
    public int getSiblingCount() {
        TreeNode myParent = getParent();

        if (myParent == null) {
            return 1;
        } else {
            return myParent.getChildCount();
        }
    }


    /**
     * Returns the next sibling of this node in the parent's children array.
     * Returns null if this node has no parent or is the parent's last child.
     * This method performs a linear search that is O(n) where n is the number
     * of children; to traverse the entire array, use the parent's child
     * enumeration instead.
     *
     * @see	#children
     * @return	the sibling of this node that immediately follows this node
     */
    public BubbleTreeNode getNextSibling() {
        BubbleTreeNode retval;

        BubbleTreeNode myParent = (BubbleTreeNode)getParent();

        if (myParent == null) {
            retval = null;
        } else {
            retval = (BubbleTreeNode)myParent.getChildAfter(this);	// linear search
        }

        if (retval != null && !isNodeSibling(retval)) {
            throw new Error("child of parent is not a sibling");
        }

        return retval;
    }


    /**
     * Returns the previous sibling of this node in the parent's children
     * array.  Returns null if this node has no parent or is the parent's
     * first child.  This method performs a linear search that is O(n) where n
     * is the number of children.
     *
     * @return	the sibling of this node that immediately precedes this node
     */
    public BubbleTreeNode getPreviousSibling() {
        BubbleTreeNode retval;

        BubbleTreeNode myParent = (BubbleTreeNode)getParent();

        if (myParent == null) {
            retval = null;
        } else {
            retval = (BubbleTreeNode)myParent.getChildBefore(this);	// linear search
        }

        if (retval != null && !isNodeSibling(retval)) {
            throw new Error("child of parent is not a sibling");
        }

        return retval;
    }



    //
    //  Leaf Queries
    //

    /**
     * Returns true if this node has no children.  To distinguish between
     * nodes that have no children and nodes that <i>cannot</i> have
     * children (e.g. to distinguish files from empty directories), use this
     * method in conjunction with <code>getAllowsChildren</code>
     *
     * @see	#getAllowsChildren
     * @return	true if this node has no children
     */
    public boolean isLeaf() {
        return (getChildCount() == 0);
    }


    /**
     * Finds and returns the first leaf that is a descendant of this node --
     * either this node or its first child's first leaf.
     * Returns this node if it is a leaf.
     *
     * @see	#isLeaf
     * @see	#isNodeDescendant
     * @return	the first leaf in the subtree rooted at this node
     */
    public BubbleTreeNode getFirstLeaf() {
        BubbleTreeNode node = this;

        while (!node.isLeaf()) {
            node = (BubbleTreeNode)node.getFirstChild();
        }

        return node;
    }


    /**
     * Finds and returns the last leaf that is a descendant of this node --
     * either this node or its last child's last leaf.
     * Returns this node if it is a leaf.
     *
     * @see	#isLeaf
     * @see	#isNodeDescendant
     * @return	the last leaf in the subtree rooted at this node
     */
    public BubbleTreeNode getLastLeaf() {
        BubbleTreeNode node = this;

        while (!node.isLeaf()) {
            node = (BubbleTreeNode)node.getLastChild();
        }

        return node;
    }


    /**
     * Returns the leaf after this node or null if this node is the
     * last leaf in the tree.
     * <p>
     * In this implementation of the <code>MutableNode</code> interface,
     * this operation is very inefficient. In order to determine the
     * next node, this method first performs a linear search in the
     * parent's child-list in order to find the current node.
     * <p>
     * That implementation makes the operation suitable for short
     * traversals from a known position. But to traverse all of the
     * leaves in the tree, you should use <code>depthFirstEnumeration</code>
     * to enumerate the nodes in the tree and use <code>isLeaf</code>
     * on each node to determine which are leaves.
     *
     * @see	#depthFirstEnumeration
     * @see	#isLeaf
     * @return	returns the next leaf past this node
     */
    public BubbleTreeNode getNextLeaf() {
        BubbleTreeNode nextSibling;
        BubbleTreeNode myParent = (BubbleTreeNode)getParent();

        if (myParent == null)
            return null;

        nextSibling = getNextSibling();	// linear search

        if (nextSibling != null)
            return nextSibling.getFirstLeaf();

        return myParent.getNextLeaf();	// tail recursion
    }


    /**
     * Returns the leaf before this node or null if this node is the
     * first leaf in the tree.
     * <p>
     * In this implementation of the <code>MutableNode</code> interface,
     * this operation is very inefficient. In order to determine the
     * previous node, this method first performs a linear search in the
     * parent's child-list in order to find the current node.
     * <p>
     * That implementation makes the operation suitable for short
     * traversals from a known position. But to traverse all of the
     * leaves in the tree, you should use <code>depthFirstEnumeration</code>
     * to enumerate the nodes in the tree and use <code>isLeaf</code>
     * on each node to determine which are leaves.
     *
     * @see		#depthFirstEnumeration
     * @see		#isLeaf
     * @return	returns the leaf before this node
     */
    public BubbleTreeNode getPreviousLeaf() {
        BubbleTreeNode previousSibling;
        BubbleTreeNode myParent = (BubbleTreeNode)getParent();

        if (myParent == null)
            return null;

        previousSibling = getPreviousSibling();	// linear search

        if (previousSibling != null)
            return previousSibling.getLastLeaf();

        return myParent.getPreviousLeaf();		// tail recursion
    }


    /**
     * Returns the total number of leaves that are descendants of this node.
     * If this node is a leaf, returns <code>1</code>.  This method is O(n)
     * where n is the number of descendants of this node.
     *
     * @see	#isNodeAncestor
     * @return	the number of leaves beneath this node
     */
    public int getLeafCount() {
        int count = 0;

        TreeNode node;
        Enumeration enumer = breadthFirstEnumeration(); // order matters not

        while (enumer.hasMoreElements()) {
            node = (TreeNode)enumer.nextElement();
            if (node.isLeaf()) {
                count++;
            }
        }

        if (count < 1) {
            throw new Error("tree has zero leaves");
        }

        return count;
    }


    //
    //  Overrides
    //

    /**
     * Returns the result of sending <code>toString()</code> to this node's
     * user object, or null if this node has no user object.
     *
     * @see	#getUserObject
     */
    public String toString() {
        if (userObject == null) {
            return null;
        } else {
            return userObject.toString();
        }
    }

    /**
     * Overridden to make clone public.  Returns a shallow copy of this node;
     * the new node has no parent or children and has a reference to the same
     * user object, if any.
     *
     * @return	a copy of this node
     */
    public Object clone() {
        BubbleTreeNode newNode = null;

        try {
            newNode = (BubbleTreeNode)super.clone();

            // shallow copy -- the new node has no parent or children
            newNode.children = null;
            newNode.parent = null;

        } catch (CloneNotSupportedException e) {
            // Won't happen because we implement Cloneable
            throw new Error(e.toString());
        }

        return newNode;
    }


    // Serialization support.
    private void writeObject(ObjectOutputStream s) throws IOException {
        Object[]             tValues;

        s.defaultWriteObject();
        // Save the userObject, if its Serializable.
        if(userObject != null && userObject instanceof Serializable) {
            tValues = new Object[2];
            tValues[0] = "userObject";
            tValues[1] = userObject;
        }
        else
            tValues = new Object[0];
        s.writeObject(tValues);
    }

    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        Object[]      tValues;

        s.defaultReadObject();

        tValues = (Object[])s.readObject();

        if(tValues.length > 0 && tValues[0].equals("userObject"))
            userObject = tValues[1];
    }

    final class PreorderEnumeration implements Enumeration {
        protected Stack<Enumeration<?>>	stack;

        public PreorderEnumeration(TreeNode rootNode) {
            super();
            Vector v = new Vector(1);
            v.addElement(rootNode);	// PENDING: don't really need a vector
            stack = new Stack<>();
            stack.push(v.elements());
        }

        public boolean hasMoreElements() {
            return (!stack.empty() &&
                    ((Enumeration)stack.peek()).hasMoreElements());
        }

        public Object nextElement() {
            Enumeration	enumer = (Enumeration)stack.peek();
            TreeNode	node = (TreeNode)enumer.nextElement();
            Enumeration	children = node.children();

            if (!enumer.hasMoreElements()) {
                stack.pop();
            }
            if (children.hasMoreElements()) {
                stack.push(children);
            }
            return node;
        }

    }  // End of class PreorderEnumeration



    final class PostorderEnumeration implements Enumeration {
        protected TreeNode root;
        protected Enumeration children;
        protected Enumeration subtree;

        public PostorderEnumeration(TreeNode rootNode) {
            super();
            root = rootNode;
            children = root.children();
            subtree = EMPTY_ENUMERATION;
        }

        public boolean hasMoreElements() {
            return root != null;
        }

        public Object nextElement() {
            Object retval;

            if (subtree.hasMoreElements()) {
                retval = subtree.nextElement();
            } else if (children.hasMoreElements()) {
                subtree = new PostorderEnumeration(
                                (TreeNode)children.nextElement());
                retval = subtree.nextElement();
            } else {
                retval = root;
                root = null;
            }

            return retval;
        }

    }  // End of class PostorderEnumeration



    final class BreadthFirstEnumeration implements Enumeration {
        protected Queue	queue;

        public BreadthFirstEnumeration(TreeNode rootNode) {
            super();
            Vector<TreeNode> v = new Vector<>(1);
            v.addElement(rootNode);	// PENDING: don't really need a vector
            queue = new Queue();
            queue.enqueue(v.elements());
        }

        public boolean hasMoreElements() {
            return (!queue.isEmpty() &&
                    ((Enumeration)queue.firstObject()).hasMoreElements());
        }

        public Object nextElement() {
            Enumeration	enumer = (Enumeration)queue.firstObject();
            TreeNode	node = (TreeNode)enumer.nextElement();
            Enumeration	children = node.children();

            if (!enumer.hasMoreElements()) {
                queue.dequeue();
            }
            if (children.hasMoreElements()) {
                queue.enqueue(children);
            }
            return node;
        }


        // A simple queue with a linked list data structure.
        final class Queue {
            QNode head;	// null if empty
            QNode tail;

            final class QNode {
                public Object	object;
                public QNode	next;	// null if end
                public QNode(Object object, QNode next) {
                    this.object = object;
                    this.next = next;
                }
            }

            public void enqueue(Object anObject) {
                if (head == null) {
                    head = tail = new QNode(anObject, null);
                } else {
                    tail.next = new QNode(anObject, null);
                    tail = tail.next;
                }
            }

            public Object dequeue() {
                if (head == null) {
                    throw new NoSuchElementException("No more elements");
                }

                Object retval = head.object;
                QNode oldHead = head;
                head = head.next;
                if (head == null) {
                    tail = null;
                } else {
                    oldHead.next = null;
                }
                return retval;
            }

            public Object firstObject() {
                if (head == null) {
                    throw new NoSuchElementException("No more elements");
                }

                return head.object;
            }

            public boolean isEmpty() {
                return head == null;
            }

        } // End of class Queue

    }  // End of class BreadthFirstEnumeration



    final class PathBetweenNodesEnumeration implements Enumeration<Object> {
        protected Stack<TreeNode> stack;

        public PathBetweenNodesEnumeration(TreeNode ancestor,
                                           TreeNode descendant)
        {
            super();

            if (ancestor == null || descendant == null) {
                throw new IllegalArgumentException("argument is null");
            }

            TreeNode current;

            stack = new Stack<>();
            stack.push(descendant);

            current = descendant;
            while (current != ancestor) {
                current = current.getParent();
                if (current == null && descendant != ancestor) {
                    throw new IllegalArgumentException("node " + ancestor +
                                " is not an ancestor of " + descendant);
                }
                stack.push(current);
            }
        }

        public boolean hasMoreElements() {
            return stack.size() > 0;
        }

        public Object nextElement() {
            try {
                return stack.pop();
            } catch (EmptyStackException e) {
                throw new NoSuchElementException("No more elements");
            }
        }
    }



  ///////////////////////////////////////
  // methods I have added
  ///////////////////////////////////////

  // overloaded methods

  /**
   * getBubble: returns the Bubble object for this node
   */
  public Bubble getBubble() {
    return (Bubble)userObject;
  }

  /**
   * getFirstChildBubble: returns the first child as a Bubble
   */
  public Bubble getFirstChildBubble() {
    if (getChildCount() == 0) {
      throw new NoSuchElementException("node has no children");
    }
    return (Bubble)(((BubbleTreeNode)getChildAt(0)).getUserObject());
  }

  /**
   * getFirstChildNOde: returns the first child as a BubbleTreeNode
   */
  public BubbleTreeNode getFirstChildNode() {
    if (getChildCount() == 0) {
      throw new NoSuchElementException("node has no children");
    }
    return (BubbleTreeNode)getChildAt(0);
  }

  /**
   * getFirstLeafBubble: returns the first leaf as a Bubble
   */
  public Bubble getFirstLeafBubble() {
    BubbleTreeNode node = this;

    while (!node.isLeaf()) {
      node = (BubbleTreeNode)node.getFirstChild();
    }

    return (Bubble)node.getUserObject();
  }

  /**
   * getLastChildBubble: returns the last child as a Bubble
   */
  public Bubble getLastChildBubble() {
    if (getChildCount() == 0) {
      throw new NoSuchElementException("node has no children");
    }
    return (Bubble)(((BubbleTreeNode)getChildAt(getChildCount()-1)).getUserObject());
  }

  /**
   * getLastChildNode: returns the last child as a BubbleTreeNode
   */
  public BubbleTreeNode getLastChildNode() {
    if (getChildCount() == 0) {
      throw new NoSuchElementException("node has no children");
    }
    return (BubbleTreeNode)getChildAt(getChildCount()-1);
  }

 /**
  * getLastLeafBubble: returns the last leaf as a Bubble
  */
  public Bubble getLastLeafBubble() {
    BubbleTreeNode node = this;

    while (!node.isLeaf()) {
      node = (BubbleTreeNode)node.getLastChild();
    }

    return (Bubble)node.getUserObject();
  }

  /**
   * getLastLeafNode: returns the last leaf as a BubbleTreeNode
   */
   public BubbleTreeNode getLastLeafNode() {
     BubbleTreeNode node = this;

     while (!node.isLeaf()) {
       node = (BubbleTreeNode)node.getLastChild();
     }

     return node;
   }

  /**
   * getParentBubble: returns the parent as a Bubble
   */
  public Bubble getParentBubble() {
    return (Bubble)(((BubbleTreeNode)parent).getUserObject());
  }

  /**
   * getParentNode: returns the parent as a BubbleTreeNode
   */
  public BubbleTreeNode getParentNode() {
    return (BubbleTreeNode)parent;
  }

  /**
   * getLeafIndex: returns the "leaf index" of the current leaf node, counting from left to right
   */
  public int getLeafIndex(BubbleTreeNode leafNode) {
    BubbleTreeNode node = this;
    int leafNum = 0;
    node = this.getFirstLeaf();

    while(node != null) {
      if (node.equals(leafNode)) {
        return leafNum;
      }
      leafNum++;
      node = node.getNextLeaf();
    }
    return -1; // if leaf not found
  }

  /**
   * getPreOrderIndex: given a node, this method returns the index (or position) of that node in the
   * subtree of this current node. The index returned is that obtained by a pre-order traversal
   */
  public int getPreOrderIndex(BubbleTreeNode givenNode) {
    Enumeration enumer = this.preorderEnumeration();
    BubbleTreeNode currNode = (BubbleTreeNode)enumer.nextElement();
    int nodeIndex = 1;

    while(currNode != null) {
      currNode = (BubbleTreeNode)enumer.nextElement();
      if (currNode.equals(givenNode)) {
        return nodeIndex;
      }
      nodeIndex++;
    }
    return -1; // if node not found
  }

  /**
   * delete: removes this node from its parent and inserts all of its children (if any) into the parent in its place.
   * If the result is that the parent has only one child, the parent is then deleted recursively until a parent is found
   * with more than one child. This operation is based on the assumption (perhaps specific to the bubble tree implementation)
   * that a node may not have only one child.
   */
  public int delete() {
    BubbleTreeNode parent = (BubbleTreeNode)this.getParent();
    BubbleTreeNode child;
    int index = parent.getIndex(this);
    int numChildren = this.getChildCount();
    int numDeleted = 1;
    int numParentsDeleted = 0;

    // first move the node's children up to its parent
    for (int i = 0; i < numChildren; i++) {
      child = (BubbleTreeNode)this.getChildAt(0);
      parent.insert(child, index + i);
    }

    // now remove the node
    parent.remove(this);

    // do not allow a parent with one child only
    // this is a recursive call, eliminating all parents with one child
    // while moving existing children up the tree
    if (parent.getChildCount() == 1 && !parent.isRoot()) {
      numParentsDeleted = parent.delete();
    }

    numDeleted = numDeleted + numParentsDeleted;

    return numDeleted;
  }

  /**
   * getDescendentCount: returns the number of descendent bubbles in the tree given this root
   */
  public int getDescendentCount() {
    Enumeration enumer = this.preorderEnumeration();
    enumer.nextElement();
    int i = 0;
    while (enumer.hasMoreElements()) {
      enumer.nextElement();
      i++;
    }
    return i;
  }

  /**
   * getPreviousSiblingAtLevel: returns the bubbletreenode of the previous sibling at the given
   * bubble level
   */
  public BubbleTreeNode getPreviousSiblingAtLevel(int level) {
    BubbleTreeNode currLeaf = this.getFirstLeaf();
    BubbleTreeNode potentialSibling;
    while (currLeaf.getPreviousLeaf() != null) {
      currLeaf = currLeaf.getPreviousLeaf();
      potentialSibling = currLeaf;
      if (potentialSibling.getBubble().getLevel() == level) {
        return potentialSibling;
      }
      while (potentialSibling.getParentNode() != null) {
        potentialSibling = potentialSibling.getParentNode();
        if (potentialSibling.getBubble().getLevel() == level) {
          return potentialSibling;
        }
      }
    }
    return null;
  }

  /**
   * getNextSiblingAtLevel: returns the bubbletreenode of the next sibling at the given
   * bubble level
   */
  public BubbleTreeNode getNextSiblingAtLevel(int level) {
    BubbleTreeNode currLeaf = this.getLastLeaf();
    BubbleTreeNode potentialSibling;
    while (currLeaf.getNextLeaf() != null) {
      currLeaf = currLeaf.getNextLeaf();
      potentialSibling = currLeaf;
      if (potentialSibling.getBubble().getLevel() == level) {
        return potentialSibling;
      }
      while (potentialSibling.getParentNode() != null) {
        potentialSibling = potentialSibling.getParentNode();
        if (potentialSibling.getBubble().getLevel() == level) {
          return potentialSibling;
        }
      }
    }
    return null;
  }

}

