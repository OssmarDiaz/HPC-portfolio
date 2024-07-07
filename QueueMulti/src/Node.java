
import java.util.concurrent.atomic.AtomicReference;
public class Node<T> {
public T valor;
public AtomicReference<Node<T>> siguiente;

public Node(T valor){
    this.valor = valor;
    this.siguiente = new AtomicReference<>(null);
    
}
}
