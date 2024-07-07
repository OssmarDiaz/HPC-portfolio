public interface Queue<T> {

public void enq(T item);
public T deq() throws Exception;
}
