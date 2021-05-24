package by.bsuir.datastructures;

import java.util.Iterator;

public class SynchronizedLinkedList<T> implements Iterable<T> {
    private volatile Node head;
    private volatile Node tail;
    private final Object mutex;

    public SynchronizedLinkedList() {
        mutex = this;
    }

    public Node add(T value) {
        synchronized (mutex) {
            if (isEmpty()) {
                head = new Node(null, value, null);
                tail = head;
            } else {
                Node node = new Node(tail, value, null);
                tail.next = node;
                tail = node;
            }
            return tail;
        }
    }

    public void set(Node node, T value) {
        synchronized (mutex) {
            node.value = value;
        }
    }

    public void remove(Node node) {
        synchronized (mutex) {
            if (node.owner != this) {
                throw new RuntimeException("The node doesn't belong to this list!");
            }
            // Node в списке
            if (node == head) {
                if (head == tail) {
                    head = null;
                    tail = null;
                } else {
                    head = head.next;
                    head.prev = null;
                }
            } else if (node == tail) {
                tail = tail.prev;
                tail.next = null;
            } else {
                node.prev.next = node.next;
                node.next.prev = node.prev;
            }
            // Node больше не в списке
            node.owner = null;
        }
    }

    public T removeFirst() {
        synchronized (mutex) {
            if (isEmpty()) {
                return null;
            } else {
                T value = head.value;
                remove(head);
                return value;
            }
        }
    }

    public T peekFirst() {
        synchronized (mutex) {
            if (isEmpty()) {
                return null;
            } else {
                return head.value;
            }
        }
    }

    public boolean isEmpty() {
        synchronized (mutex) {
            return head == null;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new IteratorImpl();
    }

    public class Node {
        private volatile Object owner = SynchronizedLinkedList.this;

        private volatile Node prev;
        private volatile T value;
        private volatile Node next;

        public Node(Node prev, T value, Node next) {
            this.prev = prev;
            this.value = value;
            this.next = next;
        }

        public T getValue() {
            return value;
        }
    }

    private class IteratorImpl implements Iterator<T> {
        Node current = head;

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public T next() {
            T value = current.value;
            current = current.next;
            return value;
        }
    }
}
