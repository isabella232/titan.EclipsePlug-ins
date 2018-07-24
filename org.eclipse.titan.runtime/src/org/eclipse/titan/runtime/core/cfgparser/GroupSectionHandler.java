/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kristof Szabados
 * @author Arpad Lovassy
 * */
public final class GroupSectionHandler {

	public static class Group {
		private String groupName = null;
		private List<GroupItem> groupItems = new ArrayList<GroupItem>();

		public String getGroupName() {
			return groupName;
		}

		public void setGroupName(final String groupName) {
			this.groupName = groupName;
		}

		public List<GroupItem> getGroupItems() {
			return groupItems;
		}

		public void setGroupItems(final List<GroupItem> groupItems) {
			this.groupItems = groupItems;
		}
	}

	public static class GroupItem {
		private String item = null;

		public GroupItem(final String item) {
			this.item = item;
		}

		public String getItem() {
			return item;
		}

		public void setItem(final String item) {
			this.item = item;
		}
	}

	private List<Group> groups = new ArrayList<Group>();

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(final List<Group> groups) {
		this.groups = groups;
	}

}
