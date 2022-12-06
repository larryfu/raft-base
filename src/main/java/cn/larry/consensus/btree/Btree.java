package cn.larry.consensus.btree;

public class Btree {

    public String findNode(TreeNode head, long key) {
        TreeNode node = head.findKey(key);
        while (node != null && !node.leafNode) {
            node = node.findKey(key);
        }

        if (node.leafNode) {
            return node.findValue(key);
        }
        return null;
    }

    public void insertNode(TreeNode head,long key, String value){
        TreeNode node = head.findKey(key);
        while (node != null && !node.leafNode) {
            node = node.findKey(key);
        }
        if(node.leafNode){
            String node1 = node.findValue(key);
            if(node1 !=null){
                throw new IllegalStateException("key already exists");
            }
        }
        node.insert(key,value);
    }

    public void deleteNode(TreeNode head,long key){
        TreeNode node = head.findKey(key);
        while (node != null && !node.leafNode) {
            node = node.findKey(key);
        }
        if(node.leafNode){
            String node1 = node.findValue(key);
            if(node1==null){
                throw new IllegalStateException("key not exists");
            }
        }
        node.deleteByKey(key);
    }
}
