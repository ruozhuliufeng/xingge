package tech.msop.core.tool.function;

/**
 * 受检的 Comparato
 *
 * @author ruozhuliufeng
 */
@FunctionalInterface
public interface CheckedComparator<T> {


    /**
     * 比较两个数
     *
     * @param o1 o1
     * @param o2 o2
     * @return int
     * @throws Throwable 受检异常
     */
    int compare(T o1, T o2) throws Throwable;
}
