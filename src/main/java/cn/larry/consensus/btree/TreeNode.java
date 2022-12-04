package cn.larry.consensus.btree;

public class TreeNode {


    boolean root;


    private static int T = 64;
    private static int MAX_KEY = 2*T-1;
    private static int MAX_CHILD = 2*T;
    TreeNode parent;
    int size;
    long[] keys;
    TreeNode[] subTreeNodes;
    TreeNode next;
    TreeNode pre;
    String[] values;

    boolean leafNode;

    public TreeNode() {
        keys = new long[MAX_KEY];
        subTreeNodes = new TreeNode[MAX_CHILD];
    }

    public TreeNode getkeyLeft(int keyindex) {
        return subTreeNodes[keyindex];
    }

    public TreeNode getKeyRight(int keyindex) {
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

    public String findValue(long key){
        if(leafNode){
            for(int i =0;i<size;i++){
                if(keys[i]== key){
                    return values[i];
                }
            }
        }
        return null;
    }

    public int inset(long key,String value){

        int index = size;
        for(int i=0;i<size-1;i++){
            if(keys[i+1]> key){
                index = i;
                break;
            }
        }
        for(int j = size;j>index;j--){
            keys[j] = keys[j-1];
            values[j] = values[j-1];
        }

        keys[index] = key;
        values[index] = value;
        size++;
        if(size > MAX_KEY){
            splitNode(key,value);
        }
        return index;
    }

    private void splitNode(long key, String value) {
        int promote = MAX_KEY /2;
        TreeNode left = cloneNode(0,promote);
        TreeNode right = cloneNode(promote+1,size-1);

        if(parent == null){
            parent = new TreeNode();
        }
        int index =  parent.inset(keys[promote],values[promote]);
        parent.subTreeNodes[index] = left;
        parent.subTreeNodes[index+1] = right;
        this.destroy();

    }

    private TreeNode cloneNode(int start, int end) {
        TreeNode treeNode = new TreeNode();
        treeNode.leafNode = leafNode;
        for(int i=start;i<=end;i++){
            treeNode.keys[i-start] = keys[start];
            if(leafNode){
                treeNode.values[i-start] = values[start];
            }else{
                treeNode.subTreeNodes[i-start] = subTreeNodes[start];
            }
        }
        //TODO subnode
        return treeNode;
    }

    public void removeKey(long key){

    }
    public void removeKeyByIndex(int  index){

    }

    public TreeNode deleteNode(long key){

        int index = -1;
        for(int i=0;i< size;i++){
            if(keys[i] == key){
                index = i;
                break;
            }
        }
        if(index<0){
            throw new IllegalArgumentException("not find");
        }

        if(leafNode){
            for(int j = index;j<size;j++){
                keys[j] = keys[j+1];
                values[j] = values[j+1];
            }
            size --;
            //删除后节点key数少于T-1,需要再均衡
            if(size < T-1 && !root ){
                TreeNode leftBro =  parent.subTreeNodes[];
                TreeNode rightBro = parent.subTreeNodes[];
                //左边节点key数超过T，从左边节点借key
                if(leftBro != null && leftBro.size >= T ){
                    inset(leftBro.keys[leftBro.size-1],leftBro.values[leftBro.size-1);
                    leftBro.removeKey(size-1);

                }else if (rightBro !=null && rightBro.size >= T){
                    inset(rightBro.keys[0],rightBro.values[0]);
                    rightBro.removeKey(0);
                    //左右节点都小于T，合并左右节点
                }else{
                    int parentindex = getindex();
                   TreeNode newNode =  mergeNode(leftBro,rightBro);
                   newNode.inset(parent.keys[parentindex],parent.values[parentindex]);
                   parent.removeKeyByIndex(parentindex);
                   parent.subTreeNodes[parentindex] = newNode;

                }

            }
        }else{
            //将左子节点的key提升，递归删除左子节点的key
            if(index >= T){
                TreeNode child = subTreeNodes[index];
                keys[index] = child.keys[child.size-1];
                child.deleteNode(keys[index]);
            }else if(size-index > T){
                TreeNode child = subTreeNodes[index+1];
                keys[index] = child.keys[0];
                child.deleteNode(keys[index]);
            }else{ // 合并左右子节点,size减小

                if(size < T-1){
                    //从兄弟节点借，如果借不到，则合并兄弟
                }
            }

        }


    }
}
