package aurelienribon.utils.notifications;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import javax.swing.event.EventListenerList;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com
 */
public class ObservableList<T> extends ArrayList<T> {
	private final Object source;
	private final List<T> evtList1 = new ArrayList<T>();
	private final List<T> evtList2 = new ArrayList<T>();

	public ObservableList() {
		this.source = null;
	}

	public ObservableList(Object source) {
		this.source = source;
	}

	// -------------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------------

	@Override
	public boolean add(T e) {
		boolean ret = super.add(e);
		evtList1.clear();
		evtList1.add(e);
		fireElementsAdded(evtList1);
		return ret;
	}

	@Override
	public void add(int index, T element) {
		super.add(index, element);
		evtList1.clear();
		evtList1.add(element);
		fireElementsAdded(evtList1);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean ret = super.addAll(c);
		evtList1.clear();
		evtList1.addAll(c);
		fireElementsAdded(evtList1);
		return ret;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		boolean ret = super.addAll(index, c);
		evtList1.clear();
		evtList1.addAll(c);
		fireElementsAdded(evtList1);
		return ret;
	}

	@Override
	public boolean remove(Object o) {
		boolean ret = super.remove(o);
		if (ret == true) {
			evtList1.clear();
			evtList1.add((T)o);
			fireElementsRemoved(evtList1);
		}
		return ret;
	}

	@Override
	public T remove(int index) {
		T e = super.remove(index);
		if (e != null) {
			evtList1.clear();
			evtList1.add(e);
			fireElementsRemoved(evtList1);
		}
		return e;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		evtList1.clear();
		for (Object o : c) if (contains(o)) evtList1.add((T)o);
		boolean ret = super.removeAll(c);
		if (ret == true) fireElementsRemoved(evtList1);
		return ret;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		evtList1.clear();
		for (T e : this) if (!c.contains(e)) evtList1.add(e);
		boolean ret = super.retainAll(c);
		if (ret == true) fireElementsRemoved(evtList1);
		return ret;
	}

	@Override
	public void clear() {
		evtList1.clear();
		evtList1.addAll(this);
		super.clear();
		fireElementsRemoved(evtList1);
	}

	@Override
	public T set(int index, T element) {
		T e = super.set(index, element);
		evtList1.clear();
		evtList1.add(e);
		fireElementsRemoved(evtList1);
		evtList1.clear();
		evtList1.add(element);
		fireElementsAdded(evtList1);
		return e;
	}

	public void replaceBy(T element) {
		evtList1.clear();
		evtList2.clear();
		if (!contains(element)) evtList1.add(element);
		for (T e : this) if (e != element) evtList2.add(e);
		super.clear();
		super.add(element);
		if (!evtList1.isEmpty() || !evtList2.isEmpty()) fireChanged(evtList1, evtList2);
	}

	public void replaceBy(Collection<T> c) {
		evtList1.clear();
		evtList2.clear();
		for (T e : c) if (!contains(e)) evtList1.add(e);
		for (T e : this) if (!c.contains(e)) evtList2.add(e);
		super.clear();
		super.addAll(c);
		if (!evtList1.isEmpty() || !evtList2.isEmpty()) fireChanged(evtList1, evtList2);
	}

	// -------------------------------------------------------------------------
	// Events
	// -------------------------------------------------------------------------

	private final EventListenerList listeners = new EventListenerList();

	public static interface ListChangeListener<T> extends EventListener {
		public void changed(Object source, List<T> added, List<T> removed);
	}

	public void addListChangedListener(ListChangeListener<T> listener) {
		listeners.add(ListChangeListener.class, listener);
	}

	public void removeListChangedListener(ListChangeListener<T> listener) {
		listeners.remove(ListChangeListener.class, listener);
	}

	private void fireElementsAdded(List<T> elems) {
		if (elems.isEmpty()) return;
		for (ListChangeListener<T> listener : listeners.getListeners(ListChangeListener.class))
			listener.changed(source != null ? source : this, elems, new ArrayList<T>());
	}

	private void fireElementsRemoved(List<T> elems) {
		if (elems.isEmpty()) return;
		for (ListChangeListener<T> listener : listeners.getListeners(ListChangeListener.class))
			listener.changed(source != null ? source : this, new ArrayList<T>(), elems);
	}

	private void fireChanged(List<T> added, List<T> removed) {
		if (added.isEmpty() && removed.isEmpty()) return;
		for (ListChangeListener<T> listener : listeners.getListeners(ListChangeListener.class))
			listener.changed(source != null ? source : this, added, removed);
	}
}
