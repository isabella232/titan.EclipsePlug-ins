/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.movefunction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;

/**
 * @author Bianka Bekefi
 * */
public class FunctionData implements Comparable<FunctionData> {
	private final Def_Function definiton;
	private Module module;
	private final String functionBody;
	private boolean toBeMoved;
	private final List<Destination> destinations;
	private List<Module> usedModules;
	private Destination finalDestination;
	private MoveFunctionMethod method;
	private final List<Module> usedBy;

	public FunctionData(final Def_Function definiton, final String functionBody) {
		this.definiton = definiton;
		this.functionBody = functionBody;
		toBeMoved = true;
		destinations = new ArrayList<Destination>();
		usedModules = new ArrayList<Module>();
		usedBy = new ArrayList<Module>();
	}

	public void setToBeMoved(final boolean toBeMoved) {
		this.toBeMoved = toBeMoved;
	}

	public void setModule(final Module module) {
		this.module = module;
	}

	public void addDestination(final Module destination, final Integer rating, final int newImports) {
		destinations.add(new Destination(destination, rating, this, newImports));
	}

	public void addUsedModule(final Module module) {
		usedModules.add(module);
	}

	public void addUsedModules(final List<Module> modules) {
		usedModules.addAll(modules);
	}

	public void addUsedBy(final Module m) {
		usedBy.add(m);
	}

	public void setUsedModules(final List<Module> modules) {
		usedModules = modules;
	}

	public void setFinalDestination(final Destination destination) {
		this.finalDestination = destination;
		if(destination != null) {
			destination.setFunctionData(this);
		}
	}

	public void setRefactoringMethod(final MoveFunctionMethod rMethod) {
		this.method = rMethod;
	}

	public Def_Function getDefiniton() {
		return definiton;
	}

	public String getFunctionBody() {
		return functionBody;
	}

	public boolean isToBeMoved() {
		return toBeMoved;
	}

	public List<Destination> getDestinations() {
		return destinations;
	}

	public Module getModule() {
		return module;
	}

	public List<Module> getUsedModules() {
		return usedModules;
	}

	public List<Module> getUsedBy() {
		return usedBy;
	}

	public Destination getFinalDestination() {
		return finalDestination;
	}

	public MoveFunctionMethod getRefactoringMethod() {
		return method;
	}

	@Override
	public int compareTo(final FunctionData arg0) {
		return this.getDefiniton().getIdentifier().getDisplayName().compareToIgnoreCase(arg0.getDefiniton().getIdentifier().getDisplayName());
	}

	public void clearDestinations() {
		destinations.clear();
	}

}

class Destination implements Comparable<Destination> {
	private Module module;
	private int rating;
	private FunctionData functionData;
	private int newImports;

	public Destination() {

	}

	public Destination(final Module module, final int rating, final FunctionData functionData, final int newImports) {
		this.module = module;
		this.rating = rating;
		this.functionData = functionData;
		this.newImports = newImports;
	}

	public Module getModule() {
		return module;
	}

	public int getRating() {
		return rating;
	}


	public FunctionData getFunctionData() {
		return functionData;
	}

	public int getNewImports() {
		return newImports;
	}

	public void setModule(final Module module) {
		this.module = module;
	}

	public void setRating(final int rating) {
		this.rating = rating;
	}

	public void setFunctionData(final FunctionData functionData) {
		this.functionData = functionData;
	}

	public void setNewImports(final int newImports) {
		this.newImports = newImports;
	}

	@Override
	public int compareTo(final Destination arg0) {
		return arg0.rating - this.rating;
	}
}
