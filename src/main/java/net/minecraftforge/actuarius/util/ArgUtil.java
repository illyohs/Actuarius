package net.minecraftforge.actuarius.util;


public class ArgUtil {
    
    public static String[] withoutFirst(String[] in) {
        if (in.length == 0) {
            return in;
        }
        String[] out = new String[in.length - 1];
        System.arraycopy(in, 1, out, 0, out.length);
        return out;
    }

}
