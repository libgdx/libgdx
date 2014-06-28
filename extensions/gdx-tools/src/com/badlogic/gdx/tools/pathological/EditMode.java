
package com.badlogic.gdx.tools.pathological;

enum EditMode {
	ADD() {
		@Override
		public String toString () {
			return "Add Nodes";
		}
	},
	SELECT() {
		@Override
		public String toString () {
			return "Select Nodes";
		}
	}
}
