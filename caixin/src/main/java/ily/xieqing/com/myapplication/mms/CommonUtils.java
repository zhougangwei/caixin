package ily.xieqing.com.myapplication.mms;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * @ 创建者   zhou
 * @ 创建时间   2019/8/26 0026 22:23
 * @ 描述    ${TODO}
 * @ 更新者  $AUTHOR$
 * @ 更新时间    2019/8/26 0026$
 * @ 更新描述  ${TODO}
 */
public class CommonUtils {
    public Map<String, RequestBody> mapToRequestBodyMap(Map<String, String> paramsMap) {
        Map<String, RequestBody> requestBodyMap = new HashMap<>();
        RequestBody requestBody;
        for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
            requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), entry.getValue());
            requestBodyMap.put(entry.getKey(), requestBody);
        }
        return requestBodyMap;
    }
}
