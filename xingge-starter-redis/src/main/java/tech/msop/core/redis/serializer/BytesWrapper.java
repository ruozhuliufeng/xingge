package tech.msop.core.redis.serializer;

/**
 * Redis 序列化辅助类，单纯的泛型无法定义通用schema，原因是无法通过泛型T得到Class
 *
 * @author ruozhuliufeng
 */
public class BytesWrapper<T> implements Cloneable {
    private T value;
    public BytesWrapper(){}
    public BytesWrapper(T value){
        this.value = value;
    }
    public void setValue(T value){
        this.value = value;
    }

    public T getValue() {
        return this.value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BytesWrapper<T> clone(){
        try {
            return (BytesWrapper<T>) super.clone();
        }catch (CloneNotSupportedException e){
            return new BytesWrapper<>();
        }
    }
}
