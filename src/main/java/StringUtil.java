import java.util.*;

public class StringUtil {

    /**
     * 替换字符串并让它的下一个字母为大写
     */
    public static String replaceUnderlineAndfirstToUpper(String srcStr, String org, String ob) {
        String newString = "";
        int first = 0;
        while (srcStr.indexOf(org) != -1) {
            first = srcStr.indexOf(org);
            if (first != srcStr.length()) {
                newString = newString + srcStr.substring(0, first) + ob;
                srcStr = srcStr.substring(first + org.length(), srcStr.length());
                srcStr = srcStr.substring(0, 1).toUpperCase() + srcStr.substring(1);
            }
        }
        newString = newString + srcStr;
        return newString;
    }


    public static List<Map<String,Object>> normalizeKey(List<Map<String,Object>> mapList) {
        List<Map<String,Object>> new_list=new ArrayList<Map<String, Object>>();
        for(Map<String,Object> map :mapList){
            Map<String,Object> nmap=new HashMap<String, Object>();
            Set<String> keys=map.keySet();
            for (String key :keys) {
                String nkey=replaceUnderlineAndfirstToUpper(key, "_", "");
                nmap.put(nkey,map.get(key));
            }
            new_list.add(nmap);
        }
        return new_list;
    }
}
