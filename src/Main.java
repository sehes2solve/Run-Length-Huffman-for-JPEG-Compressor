import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class Main
{
    public static HashMap<String,String> huffman_table;
    public static String get_adt_bits(int AC)
    {
        char[] code;
        boolean is_negative = AC < 0;
        AC = Math.abs(AC);
        code = Integer.toBinaryString(AC).toCharArray();
        if(is_negative)
        {
            for(int i = 0;i < code.length;i++)
                code[i] = (code[i] == '0') ? '1' : '0';
        }
        return new String(code);
    }
    public static ArrayList<String> get_descriptors(String AC_stream,ArrayList<String> adt_bits)
    {
        String AC;
        boolean is_stream_end = false;
        int AC_value,zeros_num;
        ArrayList<String> descriptors = new ArrayList<>();
        for(int i = 0;!is_stream_end;)
        {
            zeros_num = 0;
            do
            {
                zeros_num++;
                AC = "";
                for (;AC_stream.charAt(i) != ','; i++) {
                    AC += AC_stream.charAt(i);
                    if (i == AC_stream.length() - 1)
                    {
                        is_stream_end = true;
                        break;
                    }
                }
                i++;
            }
            while(AC.equals("0") && !is_stream_end);
            zeros_num--;
            if(!AC.equals("EOB"))
            {
                AC_value = Integer.parseInt(AC);
                AC = get_adt_bits(AC_value);
                adt_bits.add(AC);
                descriptors.add(zeros_num + "/" + AC.length());
            }
        }
        descriptors.add("EOB");
        return descriptors;
    }
    public static String compress(String AC_stream)
    {
        String encoding = "";
        ArrayList<String> adt_bits = new ArrayList<>();
        ArrayList<String> descirptors = get_descriptors(AC_stream,adt_bits);
        /** huffman encoding **/
        TreeMap<String,String> fixed_length_dict = new TreeMap<>();
        HashMap<String,Float> freq_table = Huffman.build_freq_table(descirptors,fixed_length_dict);
        huffman_table = Huffman.build_dict(freq_table);
        ArrayList<String> huffman_codes = Huffman.compress(descirptors,huffman_table,fixed_length_dict);
        /** concatenate code **/
        for(int i = 0;i < huffman_codes.size() - 1;i++)
            encoding += huffman_codes.get(i) + adt_bits.get(i);
        encoding += huffman_codes.get(huffman_codes.size()-1);
        return encoding;
    }
    public static String get_value_of_adt_bits_len(String adt_bits)
    {
        char[] code;
        boolean is_negative = (adt_bits.charAt(0) == '0') ? true: false;
        if(is_negative)
        {
            code = adt_bits.toCharArray();
            for(int i = 0;i < code.length;i++)
                code[i] = (code[i] == '0') ? '1' : '0';
            adt_bits = new String(code);
        }
        return (is_negative) ? Integer.toString(-1 * Integer.parseInt(adt_bits,2)): Integer.toString(Integer.parseInt(adt_bits,2));
    }
    public static String decompress(String code)
    {
        HashMap<String,String> table_huffman = new HashMap<>();
        for(String descriptor_itr : huffman_table.keySet())
            table_huffman.put(table_huffman.get(descriptor_itr),descriptor_itr);
        String[] desc_arr;
        int zeros_num, adt_bits_len;
        String curr_code = "", descriptor, AC_stream = "";
        for(int i = 0;i < code.length();i++)
        {
            curr_code += code.charAt(i);
            descriptor = table_huffman.get(curr_code);
            if(descriptor != null)
            {
                desc_arr = descriptor.split("/");
                zeros_num = Integer.parseInt(desc_arr[0]);
                adt_bits_len = Integer.parseInt(desc_arr[1]);
                for(int j = 0;j < zeros_num;j++)
                    AC_stream += "0,";
                curr_code = "";
                for(i++;i < zeros_num;i++)
                AC_stream += get_value_of_adt_bits_len(adt_bits_len) + ",";
                i--;
                curr_code = "";
            }
        }
    }
    public static void main(String[] args)
    {
        /*ArrayList<String> ad_bits = new ArrayList<>();
        ArrayList<String> descriptors = get_descriptors("-2,0,0,2,0,0,3,2,0,1,0,0,-2,0,-1,0,0,1,0,0,-1,EOB",ad_bits);
        System.out.println(descriptors);
        System.out.println(ad_bits);*/
        String code = compress("-2,0,0,2,0,0,3,2,0,1,0,0,-2,0,-1,0,0,1,0,0,-1,EOB");

    }
}
