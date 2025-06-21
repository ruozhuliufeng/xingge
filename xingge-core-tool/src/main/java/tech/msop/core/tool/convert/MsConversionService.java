package tech.msop.core.tool.convert;

import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.lang.Nullable;
import org.springframework.util.StringValueResolver;

/**
 * 类型转换服务，添加了ResultEnum依赖
 */
public class MsConversionService extends ApplicationConversionService {
    @Nullable
    private static volatile MsConversionService SHARED_INSTANCE;

    public MsConversionService(){
        this(null);
    }

    public MsConversionService(@Nullable StringValueResolver embeddedValueResolber){
        super(embeddedValueResolber);
        super.addConverter(new EnumToStringConverter());
        super.addConverter(new StringToEnumConverter());
    }

    /**
     * Return a shared default application {@code ConversionService} instance, lazily
     * building it once needed.
     * <p>
     * Note: This method actually returns an {@link MsConversionService}
     * instance. However, the {@code ConversionService} signature has been preserved for
     * binary compatibility.
     * @return the shared {@code MsConversionService} instance (never{@code null})
     */
    public static GenericConversionService getInstance(){
        MsConversionService sharedInstance = MsConversionService.SHARED_INSTANCE;
        if (sharedInstance == null){
            synchronized (MsConversionService.class){
                sharedInstance = MsConversionService.SHARED_INSTANCE;
                if (sharedInstance == null){
                    sharedInstance = new MsConversionService();
                    MsConversionService.SHARED_INSTANCE = sharedInstance;
                }
            }
        }
        return sharedInstance;
    }

}
