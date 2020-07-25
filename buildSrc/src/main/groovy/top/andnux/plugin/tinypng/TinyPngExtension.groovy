package top.andnux.plugin.tinypng

/**
 * Create On 16/12/2016
 * @author Wayne
 */
 class TinyPngExtension {

     public String apiKey
     public ArrayList<String> whiteList;
     public ArrayList<String> resourceDir;
     public ArrayList<String> resourcePattern;

     TinyPngExtension() {
        apiKey = ""
        whiteList = []
        resourceDir = []
        resourcePattern = []
    }

    @Override
     String toString() {
        return "TinyPngExtension{" +
                "apiKey='" + apiKey + '\'' +
                ", whiteList=" + whiteList +
                ", resourceDir=" + resourceDir +
                ", resourcePattern=" + resourcePattern +
                '}';
    }
}
