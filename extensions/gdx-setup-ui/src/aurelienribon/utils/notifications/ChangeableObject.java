/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package aurelienribon.utils.notifications;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com
 */
public class ChangeableObject implements Changeable {
	private final List<ChangeListener> changeListeners = new ArrayList<ChangeListener>(3);

	@Override
	public void addChangeListener(ChangeListener l) {
		changeListeners.add(l);
	}

	@Override
	public void removeChangeListener(ChangeListener l) {
		changeListeners.remove(l);
	}

	protected void firePropertyChanged(String propertyName) {
		for (ChangeListener listener : changeListeners)
			listener.propertyChanged(this, propertyName);
	}
}