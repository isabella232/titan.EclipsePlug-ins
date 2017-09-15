/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.insertfield;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Sequence_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Seq_Choice_BaseType;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.refactoring.insertfield.InsertFieldRefactoring.Settings;

/**
 * @author Bianka Bekefi
 * */
public class InsertFieldWizardInputPage extends UserInputWizardPage {

	private Settings settings;

	private static final String INVALIDNAME = "Name is not valid. ";
	private static final String NAMEEXISTS = "Name already exists. ";
	private static final String NEGATIVEPOSITION = "Position must be at least 0. ";
	private static final String NOTNUMBERPOSITION = "Position must be a number. ";
	private static final String INVALIDUPPERBOUNDPOSITION = "Position must be between 0 and %d. ";
	private static final String TYPEMANDATORY = "Type is mandatory. ";
	private static final String VALUEMANDATORY = "Value is mandatory. ";

	private Text positionField;
	private Text typeField;
	private Text nameField;
	private Text valueField;

	private Definition selection;

	private String positionErrorMessage = "";
	private String nameErrorMessage = "";
	private String typeErrorMessage = "";
	private String valueErrorMessage = "";

	private boolean posDone = false;
	private boolean nameDone = false;
	private boolean typeDone = false;
	private boolean valueDone = false;

	InsertFieldWizardInputPage(final String name, final Settings settings, final Definition selection) {
		super(name);
		this.settings = settings;
		this.selection = selection;
	}

	@Override
	public void createControl(final Composite parent) {
		final Composite top = new Composite(parent, SWT.NONE);
		initializeDialogUnits(top);
		setControl(top);
		top.setLayout(new GridLayout(2, false));

		final Label positionLabel = new Label(top, SWT.NONE);
		positionLabel.setText("Position (0, 1, ...): ");
		positionField = new Text(top, SWT.NONE);
		positionField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				checkPosition();
			}
		});

		final Label typeLabel = new Label(top, SWT.NONE);
		typeLabel.setText("Type: ");
		typeField = new Text(top, SWT.NONE);
		typeField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				checkType();
			}
		});

		final Label nameLabel = new Label(top, SWT.NONE);
		nameLabel.setText("Name: ");
		nameField = new Text(top, SWT.NONE);
		nameField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				checkName();
			}
		});

		final Label valueLabel = new Label(top, SWT.NONE);
		valueLabel.setText("Value: ");
		valueField = new Text(top, SWT.NONE);
		valueField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				checkValue();
			}
		});

		setPageComplete(false);
		setErrorMessage(null);
	}

	public void checkPosition() {
		try {
			settings.setPosition(Integer.parseInt(positionField.getText().trim()));
			final IType type = selection.getType(CompilationTimeStamp.getBaseTimestamp());
			if (type instanceof TTCN3_Sequence_Type || type instanceof TTCN3_Set_Type) {
				final TTCN3_Set_Seq_Choice_BaseType ss = (TTCN3_Set_Seq_Choice_BaseType) type;
				final int noc = ss.getNofComponents();
				if (settings.getPosition() < 0) {
					positionErrorMessage = NEGATIVEPOSITION;
					posDone = false;
				} else if (settings.getPosition() > noc) {
					positionErrorMessage = String.format(INVALIDUPPERBOUNDPOSITION, noc);
					posDone = false;
				} else {
					positionErrorMessage = "";
					posDone = true;
				}
			}
		} catch (NumberFormatException ex) {
			positionErrorMessage = NOTNUMBERPOSITION;
			posDone = false;
		}
		setErrorMessage(positionErrorMessage + typeErrorMessage + nameErrorMessage + valueErrorMessage);
		setPageComplete(posDone && typeDone && nameDone && valueDone);
	}

	public void checkName() {
		final Identifier id = new Identifier(Identifier_type.ID_TTCN, nameField.getText().trim());
		settings.setId(id);
		if (!Identifier.isValidInTtcn(settings.getId().getName())) {
			nameErrorMessage = INVALIDNAME;
			nameDone = false;
		} else {
			final IType type = selection.getType(CompilationTimeStamp.getBaseTimestamp());
			if (type instanceof TTCN3_Sequence_Type || type instanceof TTCN3_Set_Type) {
				final TTCN3_Set_Seq_Choice_BaseType ss = (TTCN3_Set_Seq_Choice_BaseType) type;
				if (ss.hasComponentWithName(settings.getId().getName())) {
					nameErrorMessage = NAMEEXISTS;
					nameDone = false;
				} else {
					nameErrorMessage = "";
					nameDone = true;
				}
			}
		}
		setErrorMessage(positionErrorMessage + typeErrorMessage + nameErrorMessage + valueErrorMessage);
		setPageComplete(posDone && typeDone && nameDone && valueDone);
	}

	public void checkType() {
		settings.setType(typeField.getText().trim());
		if (settings.getType() == null || settings.getType().equals("")) {
			typeErrorMessage = TYPEMANDATORY;
			typeDone = false;
		} else {
			typeErrorMessage = "";
			typeDone = true;
		}
		setErrorMessage(positionErrorMessage + typeErrorMessage + nameErrorMessage + valueErrorMessage);
		setPageComplete(posDone && typeDone && nameDone && valueDone);
	}

	public void checkValue() {
		settings.setValue(valueField.getText().trim());
		if (settings.getValue() == null || settings.getValue().equals("")) {
			valueErrorMessage = VALUEMANDATORY;
			valueDone = false;
		} else {
			valueErrorMessage = "";
			valueDone = true;
		}
		setErrorMessage(positionErrorMessage + typeErrorMessage + nameErrorMessage + valueErrorMessage);
		setPageComplete(posDone && typeDone && nameDone && valueDone);
	}
}
