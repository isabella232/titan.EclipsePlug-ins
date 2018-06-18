/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.slicing;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;

/**
 * @author Bianka Bekefi
 * */
public class FunctionData implements Comparable<FunctionData> {
	private Def_Function definiton;
	private Module module;
	private String functionBody;
	private boolean toBeMoved;
	private List<Destination> destinations;
	private List<Module> usedModules;
	private Destination finalDestination;
	private SlicingMethod method;
	
	public FunctionData(Def_Function definiton, String functionBody) {
		this.definiton = definiton;
		this.functionBody = functionBody;
		toBeMoved = true;
		destinations = new ArrayList<Destination>();
		usedModules = new ArrayList<Module>();
	}
	
	public void setToBeMoved(boolean toBeMoved) {
		this.toBeMoved = toBeMoved;
	}
	
	public void setModule(Module module) {
		this.module = module;
	}
	
	public void addDestination(Module destination, Integer rating, int newImports) {
		destinations.add(new Destination(destination, rating, this, newImports));
	}
	
	public void addUsedModule(Module module) {
		usedModules.add(module);
	}
	
	public void addUsedModules(List<Module> modules) {
		usedModules.addAll(modules);
	}
	
	public void setUsedModules(List<Module> modules) {
		usedModules = modules;
	}
	
	public void setFinalDestination(Destination destination) {
		this.finalDestination = destination;
		if(destination != null) {
			destination.setFunctionData(this);
		}
	}
	
	public void setRefactoringMethod(SlicingMethod rMethod) {
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
	
	public Destination getFinalDestination() {
		return finalDestination;
	}
	
	public SlicingMethod getRefactoringMethod() {
		return method;
	}

	@Override
	public int compareTo(FunctionData arg0) {
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
	
	public Destination(Module module, int rating, FunctionData functionData, int newImports) {
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
	
	public void setModule(Module module) {
		this.module = module;
	}
	
	public void setRating(int rating) {
		this.rating = rating;
	}
	
	public void setFunctionData(FunctionData functionData) {
		this.functionData = functionData;
	}
	
	public void setNewImports(int newImports) {
		this.newImports = newImports;
	}

	@Override
	public int compareTo(Destination arg0) {
		return arg0.rating - this.rating;
	}
}
