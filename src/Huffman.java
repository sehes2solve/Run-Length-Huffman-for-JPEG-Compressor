import java.util.*;

class Node
{
    float freq;
    String ch;
    Node left,right;
    public Node()
    {
        freq = 0;
        ch = "*";
        left = null;
        right = null;
    }
    public Node(float f,Node l,Node r)
    {
        freq = f;
        ch = "-";
        left = l;
        right = r;
    }
    public Node(float f,String c)
    {
        freq = f;
        ch = c;
        left = null;
        right = null;
    }
};

public class Huffman
{
    public static int dict_idx = 0;
    public static void binary_code_generator(int n,String s,TreeMap<String,String> fixed_dict)
    {
        /** in case number of fixed is not divisible by 2 **/
        if(dict_idx >= fixed_dict.size())
            return;
        if(s.length() == Math.ceil(Math.log(n)/Math.log(2)))
        {
            fixed_dict.put((String)fixed_dict.keySet().toArray()[dict_idx],s);
            dict_idx++;
            return;
        }
        binary_code_generator(n,s + '0',fixed_dict);
        binary_code_generator(n,s + '1',fixed_dict);
    }
    public static void set_fixed_length_codes(TreeMap<String,String> fixed_dict)
    {
        dict_idx = 0;
        binary_code_generator(fixed_dict.size(),"",fixed_dict);
    }
    public static HashMap<String,Float> build_freq_table(ArrayList<String> msg,TreeMap<String,String> fixed_dict)
    {
        /*Comparator<HashMap.Entry<String,Float>> freq_compr = new Comparator<HashMap.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<Character, Float> o1, Map.Entry<Character, Float> o2)
            {
                if(o1.getValue() < o2.getValue()) return -1;
                else if(o1.getValue() > o2.getValue()) return 1;
                else return 0;
            }
        };*/
        /*Comparator<HashMap.Entry<String,Float>> char_compr = new Comparator<HashMap.Entry<String, Float>>() {
            @Override
            public int compare(Map.Entry<Character, Float> o1, Map.Entry<Character, Float> o2)
            {
                if(o1.getKey() < o2.getKey()) return -1;
                else if(o1.getKey() > o2.getKey()) return 1;
                else return 0;
            }
        };*/
        HashMap<String,Float> freq_table = new HashMap<>();
        for(String ch : msg)
        {
            if(freq_table.containsKey(ch)) freq_table.put(ch,(1f/msg.size()) + freq_table.get(ch));
            else                           freq_table.put(ch,1f/msg.size());
            fixed_dict.put(ch,"");
        }
        set_fixed_length_codes(fixed_dict);
        List<HashMap.Entry<String, Float>> temp = new ArrayList<>(freq_table.entrySet());
        /** Collections.sort(temp,freq_compr);**/
        float others_freq = 0;
        HashMap.Entry<String, Float> other;
        /** max num of codes 4 & max other freq 0.2**/
        while (temp.size() > 4 && temp.get(0).getValue() < 0)
        {
            other = temp.remove(0);
            others_freq += other.getValue();
        }
        /** Collections.sort(temp,char_compr); **/
        freq_table.clear();
        for(HashMap.Entry<String, Float> itr : temp)
            freq_table.put(itr.getKey(),itr.getValue());
        /** add others char in table **/
        //freq_table.put( "" + ((char)127),others_freq);
        return freq_table;
    }

    public static Node build_tree(HashMap<String,Float> freq_table,HashMap<String,String> dict)
    {
        ArrayList<Node> not_child_nodes  = new ArrayList<>();
        Comparator<Node> node_compr = new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                {
                    if     (o1.freq < o2.freq)  return 1;
                    else if(o1.freq > o2.freq)  return-1;
                    else if(o1.ch.equals ( "" + (char)127)) return 1;
                    else if(o2.ch.equals ( "" + (char)127)) return-1;
                    else return 0;
                }
            }
        };
        for(String en: freq_table.keySet())
        {
            not_child_nodes.add(new Node(freq_table.get(en),en));
            dict.put(en,"");
        }
        Collections.sort(not_child_nodes,node_compr);
        while (not_child_nodes.size() > 2)
        {
            Node r_smallest = not_child_nodes.remove(not_child_nodes.size() - 1);
            Node l_before_smallest = not_child_nodes.remove(not_child_nodes.size() - 1);
            Node n = new Node(r_smallest.freq + l_before_smallest.freq, l_before_smallest, r_smallest);
            not_child_nodes.add(n);
            Collections.sort(not_child_nodes,node_compr);
        }
        Node root = new Node();
        if(not_child_nodes.size() > 0) root.left = not_child_nodes.get(0);
        if(not_child_nodes.size() > 1) root.right = not_child_nodes.get(1);
        return root;
    }
    public static void preorder_DFT(Node curr_node,String curr_code,HashMap<String,String> dict)
    {
        if(curr_node == null) return;
        if(curr_node.left == null && curr_node.right == null) { dict.put(curr_node.ch,curr_code); return; }
        preorder_DFT(curr_node.left ,curr_code + '0',dict);
        preorder_DFT(curr_node.right,curr_code + '1',dict);
    }
    public static HashMap<String,String> build_dict(HashMap<String,Float> freq_table)
    {
        HashMap<String,String> dict = new HashMap<>();
        preorder_DFT(build_tree(freq_table,dict),"",dict);
        return dict;
    }

    public static ArrayList<String> compress(ArrayList<String> msg,HashMap<String,String> dict,TreeMap<String,String> fixed_length_dict)
    {
        ArrayList<String> code = new ArrayList<>();
        for(int i = 0;i < msg.size();i++)
            if(dict.containsKey(msg.get(i)))
                code.add(dict.get(msg.get(i)));
            else
                code.add(dict.get( "" + ((char)127)));
        return code;
    }
    public static ArrayList<String> decompress(ArrayList<String> code,HashMap<String,String> dict,TreeMap<String,String> fixed_length_dict)
    {
        ArrayList<String> msg = new ArrayList<>();
        for(int i = 0;i < code.size();i++)
        {
            for(HashMap.Entry<String,String> pair : dict.entrySet())
            {
                if(pair.getValue().equals(code.get(i)))
                    msg.add(pair.getKey());

            }
            /*curr_sub_code += code.charAt(i);
            for(HashMap.Entry<String,String> pair : dict.entrySet())
                if(pair.getValue().equals(curr_sub_code))
                {
                    if(pair.getKey().equals( "" + ((char)127)) )
                    {
                        String temp = code.substring(i + 1,i + 5);
                        for(String ch : fixed_length_dict.keySet())
                            if(fixed_length_dict.get(ch).equals(temp))
                            {
                                msg.add(ch);
                                break;
                            }
                        i += 4;
                    }
                    else
                        msg.add(pair.getKey());
                    curr_sub_code = "";
                }*/
        }
        return msg;
    }
    /*public static void main(String[] args)
    {
        ArrayList<String> msg = new ArrayList<String>(List.of("a","b","c","a","z","d","a","f","c","q","d","a","d","c","u","a","b","a","p","d"));
        TreeMap<String,String> fixed_length_dict = new TreeMap<>();
        HashMap<String,Float> freq_table = build_freq_table(msg,fixed_length_dict);
        HashMap<String,String> dict = build_dict(freq_table);
        ArrayList<String> code = compress(msg,dict,fixed_length_dict);
        System.out.println(code);
        System.out.println(decompress(code,dict,fixed_length_dict));
    }*/
}
