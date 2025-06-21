package tech.msop.core.tool.convert;

import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.lang.Nullable;
import org.springframework.util.StringValueResolver;

/**
 * 类型转换服务，添加了ResultEnum依赖
 */
public class XingGeConversionService extends ApplicationConversionService {
    @Nullable
    private static volatile XingGeConversionService SHARED_INSTANCE;

    public XingGeConversionService(){
        this(null);
    }

    public XingGeConversionService(@Nullable StringValueResolver embeddedValueResolber){
        super(embeddedValueResolber);
        super.addConverter(new EnumToStringConverter());
        super.addConverter(new StringToEnumConverter());
    }

    /**
     * Return a shared default application {@code ConversionService} instance, lazily
     * building it once needed.
     * <p>
     * Note: This method actually returns an {@link XingGeConversionService}
     * instance. However, the {@code ConversionService} signature has been preserved for
     * binary compatibility.
     * @return the shared {@code MsConversionService} instance (never{@code null})
     */
    public static GenericConversionService getInstance(){
        XingGeConversionService sharedInstance = XingGeConversionService.SHARED_INSTANCE;
        if (sharedInstance == null){
            synchronized (XingGeConversionService.class){
                sharedInstance = XingGeConversionService.SHARED_INSTANCE;
                if (sharedInstance == null){
                    sharedInstance = new XingGeConversionService();
                    XingGeConversionService.SHARED_INSTANCE = sharedInstance;
                }
            }
        }
        return sharedInstance;
    }

}
