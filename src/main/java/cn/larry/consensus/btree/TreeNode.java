package cn.larry.consensus.btree;

public class TreeNode {


    boolean root;


    private static int T = 64;
    private static int MAX_KEY = 2 * T - 1;
    private static int MIN_KEY = T - 1;
    private static int MAX_CHILD = 2 * T;
    TreeNode parent;
    int size;
    long[] keys;
    TreeNode[] subTreeNodes;
    TreeNode next;
    TreeNode pre;
    String[] values;

    boolean leafNode;

    public TreeNode(boolean leaf) {
        this.leafNode = leaf;
        size = 0;
        keys = new long[MAX_KEY];
        subTreeNodes = new TreeNode[MAX_CHILD];
        if (leaf) {
            values = new String[MAX_KEY];
        }
    }

    public TreeNode getkeyLeft(int keyindex) {
        if (keyindex < 0 || keyindex > size) {
            return null;
        }
        return subTreeNodes[keyindex];
    }

    public TreeNode getKeyRight(int keyindex) {
        if (keyindex < 0 || keyindex > size) {
            return null;
        }
        return subTreeNodes[keyindex + 1];
    }

    public TreeNode findKey(long key) {
        for (int i = 0; i < size; i++) {
            if (keys[i] >= key) {
                return getkeyLeft(i);
            }
        }
        return getKeyRight(size - 1);
    }

    public String findValue(long key) {
        if (leafNode) {
            for (int i = 0; i < size; i++) {
                if (keys[i] == key) {
                    return values[i];
                }
            }
        }
        return null;
    }

    public TreeNode insert(long key, String value) {
        int index = addKeyValue(key, value);
        return nodeReBalance(this);
    }


    private TreeNode cloneNode(int start, int end) {
        TreeNode treeNode = new TreeNode(leafNode);
        for (int i = start; i <= end; i++) {
            treeNode.keys[i - start] = keys[start];
            treeNode.values[i - start] = values[start];
            treeNode.subTreeNodes[i - start] = subTreeNodes[start];
        }
        treeNode.subTreeNodes[treeNode.size] = subTreeNodes[end + 1];
        return treeNode;
    }


    private int getIndex(long key) {
        for (int i = 0; i < size; i++) {
            if (keys[i] == key) {
                return i;
            }
        }
        return -1;
    }

    private void remove(long key) {
        int index = getIndex(key);
        removeByIndex(index);

    }

    private void removeByIndex(int index) {
        if (index < 0 || index >= size) {
            return;
        }
        for (int j = index; j < size - 1; j++) {
            keys[j] = keys[j + 1];
            values[j] = values[j + 1];
            subTreeNodes[j] = subTreeNodes[j + 1];
        }
        size--;
    }

    public TreeNode deleteByKey(long key) {
        int index = getIndex(key);
        if (index < 0) {
            throw new IllegalArgumentException("not find");
        }
        if (leafNode) {
            removeByIndex(index);
            //删除后节点key数少于T-1,需要再均衡
            return nodeReBalance(this);
        } else {
            //将左子节点的key提升，递归删除左子节点的key
            if (index >= T) {
                TreeNode child = subTreeNodes[index];
                keys[index] = child.keys[child.size - 1];
                child.deleteByKey(keys[index]);
            } else if (size - index > T) {
                TreeNode child = subTreeNodes[index + 1];
                keys[index] = child.keys[0];
                child.deleteByKey(keys[index]);
            } else { // 合并左右子节点,size减小
                TreeNode node = mergeNode(subTreeNodes[index], subTreeNodes[index + 1], keys[index],
                        leafNode ? values[index] : null);
                subTreeNodes[index] = node;
                removeByIndex(index);
                node.deleteByKey(key);
                return nodeReBalance(this);
            }
        }
        return null;

    }

    public TreeNode nodeReBalance(TreeNode node) {
        if (node.size < MIN_KEY) {
            return reBalanceSmallNode(node);
        } else if (node.size > MAX_KEY) {
            return reBalanceBigNode(node);
        }
        return null;
    }

    int getSubIndex(TreeNode node) {
        if (leafNode) {
            return -1;
        }
        for (int i = 0; i <= size; i++) {
            if (subTreeNodes[i] == node) {
                return i;
            }
        }
        return -1;
    }

    public TreeNode reBalanceSmallNode(TreeNode node) {
        if (node.root && node.size == 1) {
            if (node.subTreeNodes[0].size <= MIN_KEY
                    && node.subTreeNodes[1].size < MIN_KEY) {
                TreeNode node1 = mergeNode(node.subTreeNodes[0], node.subTreeNodes[1], node.keys[0], node.values[0]);
                node1.root = true;
                return node1;
            }
        }

        if (node.size < MIN_KEY && !node.root) {
            int parentindex = node.parent.getSubIndex(node);

            TreeNode leftBro = node.parent.getkeyLeft(parentindex - 1);
            TreeNode rightBro = node.parent.getKeyRight(parentindex);
            //左边节点key数超过T，从左边节点借key
            if (leftBro != null && leftBro.size >= T) {
                insert(leftBro.keys[leftBro.size - 1], leftBro.values[leftBro.size - 1]);
                leftBro.deleteByKey(leftBro.keys[leftBro.size - 1]);

            } else if (rightBro != null && rightBro.size >= T) {
                insert(rightBro.keys[0], rightBro.values[0]);
                rightBro.deleteByKey(rightBro.keys[0]);
                //左右节点都小于T，合并左右节点
            } else {
                TreeNode mergeNode = null;
                if (leftBro != null) {
                    mergeNode = leftBro;
                } else if (rightBro != null) {
                    mergeNode = rightBro;
                }
                TreeNode newNode = mergeNode(mergeNode, this, node.parent.keys[parentindex], node.parent.values[parentindex]);
                node.parent.subTreeNodes[parentindex] = newNode;
                node.parent.removeByIndex(parentindex);
                return nodeReBalance(node.parent);
            }
        }
        return null;
    }

    private int addKeyValue(long key, String value) {
        int index = size;
        for (int i = 0; i < size - 1; i++) {
            if (keys[i + 1] > key) {
                index = i;
                break;
            }
        }
        for (int j = size; j > index; j--) {
            keys[j] = keys[j - 1];
            values[j] = values[j - 1];
        }
        keys[index] = key;
        values[index] = value;
        size++;
        return index;
    }

    void destroy() {
        this.keys = null;
        this.values = null;
        this.subTreeNodes = null;
    }

    public TreeNode reBalanceBigNode(TreeNode node) {
        int promote = MAX_KEY / 2;
        TreeNode left = cloneNode(0, promote - 1);
        TreeNode right = cloneNode(promote + 1, size - 1);
        left.root = false;
        right.root = false;
        if (node.root) {
            node.parent = new TreeNode(false);
            node.parent.root = true;
            node.root = false;
        }
        TreeNode parent = node.parent;
        int index = parent.addKeyValue(keys[promote], values[promote]);
        parent.subTreeNodes[index] = left;
        parent.subTreeNodes[index + 1] = right;
        node.destroy();

        TreeNode root = nodeReBalance(parent);
        if (root != null) {
            return root;
        }
        if (parent.root) {
            return parent;
        }
        return null;
    }


    private TreeNode mergeNode(TreeNode left, TreeNode right, long key, String value) {
        TreeNode node = new TreeNode(left.leafNode);
        node.parent = left.parent;
        TreeNode small = left.keys[0] < right.keys[0] ? left : right;
        TreeNode bigger = left.keys[0] < right.keys[0] ? right : left;
        for (int i = 0; i < small.size; i++) {
            node.keys[i] = small.keys[i];
            if (left.leafNode) {
                node.values[i] = small.values[i];
            } else {
                node.subTreeNodes[i] = small.subTreeNodes[i];
            }
            node.size++;
        }
        node.keys[small.size] = key;
        if (node.leafNode) {
            node.values[small.size] = value;
        }

        int bigstart = small.size + 1;
        for (int i = 0; i < bigger.size; i++) {
            node.keys[i + bigstart] = bigger.keys[i];
            if (left.leafNode) {
                node.values[i + bigstart] = bigger.values[i];
            } else {
                node.subTreeNodes[i + bigstart] = bigger.subTreeNodes[i];
            }
            node.size++;
        }
        if (!leafNode) {
            node.subTreeNodes[node.size] = bigger.subTreeNodes[bigger.size];
        }
        return node;
    }
}
