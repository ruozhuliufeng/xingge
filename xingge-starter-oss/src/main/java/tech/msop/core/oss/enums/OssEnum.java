package tech.msop.core.oss.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OSS 枚举类
 *
 * @author ruozhuliufeng
 */
@Getter
@AllArgsConstructor
public enum OssEnum {
    /**
     * minio
     */
    MINIO("minio", 1),

    /**
     * 七牛
     */
    QINIU("qiniu", 2),

    /**
     * 阿里
     */
    ALI("ali", 3),
    /**
     * 腾讯
     */
    TENCENT("tenccent", 4),
    /**
     * 华为
     */
    HUAWEI("huawei", 5),
    ;
    /**
     * 名称
     */
    final String name;
    /**
     * 类型
     */
    final Integer category;

    public static OssEnum of(String name) {
        if (name == null) {
            return null;
        }
        OssEnum[] values = OssEnum.values();
        for (OssEnum ossEnum : values) {
            if (ossEnum.name.equals(name)) {
                return ossEnum;
            }
        }
        return null;
    }
}
