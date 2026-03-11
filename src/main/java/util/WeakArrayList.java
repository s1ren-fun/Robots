package util;

import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Список, содержащий слабые ссылки на свои элементы
 *
 * @param <T> Элемент
 */
public class WeakArrayList<T> extends AbstractList<T> {
    private final List<WeakReference<T>> items;

    public WeakArrayList(){
        items = new ArrayList<>();
    }

    public WeakArrayList(List<T> otherList) {
        items = new ArrayList<>(otherList.size());
        for(T t : otherList){
            items.add(new WeakReference<>(t));
        }
    }

    @Override
    public T get(int index) {
        return items.get(index).get();
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean add(T t){
        cleanUp();
        return items.add(new WeakReference<>(t));
    }

    /**
     * Очищает items от null элементов
     */
    private void cleanUp(){
        items.removeIf(ref->ref.get() == null);
    }

    @Override
    public boolean remove(Object o){
        cleanUp();
        for(WeakReference<T> item : items){
           if(item.get()==o){
               items.remove(item);
               return true;
           }
        }
        return false;
    }
}
