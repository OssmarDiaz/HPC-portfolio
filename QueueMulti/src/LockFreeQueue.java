import java.util.concurrent.atomic.AtomicReference; // usamos esta biblioteca para hacer referencias de tipo atomicas y considerarlas puntuales

/**
* @param T
*/


public class LockFreeQueue<T> implements Queue<T> { // implementa la interfaz Queue porque necesariamente necesitamos enqueue y dequeue
    private AtomicReference<Node<T>> head;
    private AtomicReference<Node<T>> tail;

//////////////////////////////////////////////////////////////

public LockFreeQueue(){ // constructor de la Cola
    Node<T> centiNode = new Node<>(null);
    this.head = new AtomicReference<>(centiNode);
    this.tail = new AtomicReference<>(centiNode);// head y tail apuntan al mismo elemento inicialmente
}


/**
 * Agregar elemento a la Cola
 * @param item
 */

 //@Override  
 public void enq(T item){
    if (item== null) throw new NullPointerException();
    Node<T> node = new Node<>(item); // se crea el nodo con el valor a agregar
    while(true){
        Node<T> last = tail.get(); //se realiza la lectura de forma atomica del ultimo elemento
        Node<T> next = last.siguiente.get(); // se realiza la lectura de forma atomica del siguiente elemento

        if(last == tail.get()){
            if(next==null){
                if(last.siguiente.compareAndSet(next, node)){
                    tail.compareAndSet(last, node);
                    return;
                }
            }
        }else{
            tail.compareAndSet(last, next);

        }
    }
 }

/////////////////////////////////////////////////////////////

/**
 * Remover y regresar la cabeza de la Cola
 * @return  remueve el primer elemento de la Cola
 * @throws  Exception
 */


 public T deq() throws Exception{
    while(true){
        Node<T> first = head.get();
        Node<T> last = tail.get();
        Node<T> next = first.siguiente.get();

        if(first == head.get()){
            if (first == last){
                if(next==null){
                    throw new Exception();

                }

                tail.compareAndSet(last, next);

            } else{
                T value = next.valor; // lee el valor a devolver
                if(head.compareAndSet(first,next)) 
                return value;
            }
        }

    }
 }

}

