import by.bsuir.datastructures.SynchronizedLinkedList;

public class SynchronizedLinkedListTest {

    public static void main(String[] args) {
        SynchronizedLinkedList<String> list = new SynchronizedLinkedList<>();
        SynchronizedLinkedList<String>.Node hello = list.add("Hello, ");
//        SynchronizedLinkedList<String>.Node world = list.add("world!");
//        SynchronizedLinkedList<String>.Node im = list.add("I'm ");
//        SynchronizedLinkedList<String>.Node anton = list.add("Anton!");

//        list.remove(anton);

        for (String s : list) {
            System.out.println(s);
        }
    }
}
