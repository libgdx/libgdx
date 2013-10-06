package com.badlogic.gdx.scenes.scene2d.ui;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/** {@inheritDoc}
 * Filterable List Actor implementation.  
 * @author Cord
 */
public class ListFilterable extends List {

  
  
	private String[] unfiltered_items;  // the unfiltered list items backing. this is the List's true item backing.
	private ArrayList<String> new_list; // used in the filtering process 
	
	
	
	public ListFilterable(Object[] items, Skin skin) {
		super(items, skin);
		
		new_list = new ArrayList<String>();
	}

	public ListFilterable(Object[] items, ListStyle style) {
		super(items, style);
		
		new_list = new ArrayList<String>();
	}

	public ListFilterable(Object[] items, Skin skin, String styleName) {
		super(items, skin, styleName);
		
		new_list = new ArrayList<String>();
	}

	
	
	/**
	 * This is the true List's items backing array. This always contains {@link #getItems()}.
	 * @return This List's unfiltered items list.
	 */
	public String[] getItemsUnfiltered() {
		return unfiltered_items;
	}

	
	
	@Override
	public void setItems(Object[] items) {
		super.setItems(items);
		
		unfiltered_items = getItems();
	}
	
	
	
	/** {@link #filterList(String, boolean)} defaulted to be case-insensitive. */
	public void filterList(String filter) {
		filterList(filter, false);
	}
	
	
	
	/**
	 * Filters this list's displayed items based on the filter and whether to filter case sensitive. 
	 * @param filter String displayed items must contain to be displayed in the List
	 * @param case_sensitive Whether the filter and items are compared case sensitive
	 */
	public void filterList(String filter, boolean case_sensitive) {
		if (filter.isEmpty() && getItems().length == getItemsUnfiltered().length) // don't need to filter
			return;
		
		if (!case_sensitive)
			filter = filter.toLowerCase();
		
		for (String item : unfiltered_items)
			if (item.toLowerCase().contains(filter))
				new_list.add(item);
		
		super.setItems(new_list.toArray(new String[new_list.size()]));
	
		new_list.clear();
	}
}
